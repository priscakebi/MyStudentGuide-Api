package com.mystudentguide.service;

import com.mystudentguide.entity.PasswordResetToken;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.PasswordResetTokenRepository;
import com.mystudentguide.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Mot de passe oublié (étudiant et administrateur).
 *
 * Déroulement :
 * 1. requestReset : l'utilisateur saisit son e-mail. Si un compte de l'espace
 *    demandé existe, un jeton aléatoire est créé et un lien est envoyé par e-mail.
 *    La réponse de l'API est TOUJOURS la même, que le compte existe ou non :
 *    on ne révèle pas quelles adresses sont inscrites (énumération de comptes).
 * 2. resetPassword : l'utilisateur ouvre le lien et choisit un nouveau mot de passe.
 *    Le jeton doit exister et ne pas être expiré ; il est supprimé après usage.
 *
 * Sécurité du jeton : 256 bits aléatoires (SecureRandom), stocké uniquement sous
 * forme d'empreinte SHA-256, durée de vie limitée, usage unique, anti-spam.
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Message volontairement unique pour tous les échecs (jeton inconnu, expiré, déjà utilisé). */
    public static final String LIEN_INVALIDE =
            "Ce lien est invalide ou a expiré. Veuillez refaire une demande de réinitialisation.";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final String frontendUrl;
    private final long expirationMinutes;
    private final long cooldownSeconds;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl,
            @Value("${app.password-reset.expiration-minutes:30}") long expirationMinutes,
            @Value("${app.password-reset.cooldown-seconds:60}") long cooldownSeconds
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
        this.expirationMinutes = expirationMinutes;
        this.cooldownSeconds = cooldownSeconds;
    }

    // ------------------------------------------------------------------
    // 1. Demande de réinitialisation
    // ------------------------------------------------------------------

    /**
     * Crée un jeton et envoie le lien par e-mail si le compte existe dans l'espace demandé.
     * Ne lève jamais d'exception « compte introuvable » : l'appelant répond toujours pareil.
     *
     * @param rawEmail e-mail saisi
     * @param espace   "ADMIN" ou "ETUDIANT" (null = ETUDIANT)
     */
    @Transactional
    public void requestReset(String rawEmail, String espace) {

        final boolean adminSpace = "ADMIN".equalsIgnoreCase(espace);
        final RoleType expectedRole = adminSpace ? RoleType.ADMIN : RoleType.ETUDIANT;

        Optional<User> found = userRepository.findByEmail(rawEmail.trim().toLowerCase());

        if (found.isEmpty()) {
            log.debug("Réinitialisation demandée pour une adresse inconnue");
            return;
        }

        User user = found.get();

        // Un compte étudiant ne peut pas être réinitialisé depuis l'espace admin, et inversement.
        if (user.getRole() != expectedRole || !user.isEnabled()) {
            log.debug("Réinitialisation ignorée : espace ou état du compte non concerné");
            return;
        }

        // Anti-spam : pas de nouvel e-mail si un jeton vient d'être créé.
        if (tokenRepository.existsByUser_IdAndCreatedAtAfter(
                user.getId(), LocalDateTime.now().minusSeconds(cooldownSeconds))) {
            log.debug("Réinitialisation ignorée : demande trop rapprochée");
            return;
        }

        // Un seul jeton valide à la fois : les anciens sont invalidés.
        tokenRepository.deleteByUserId(user.getId());

        String rawToken = generateRawToken();

        PasswordResetToken token = new PasswordResetToken();
        token.setTokenHash(sha256Hex(rawToken));
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
        tokenRepository.save(token);

        emailService.envoyerLienReinitialisationMotDePasse(
                user.getEmail(),
                user.getPrenom(),
                buildLink(rawToken, adminSpace),
                expirationMinutes,
                adminSpace);
    }

    // ------------------------------------------------------------------
    // 2. Nouveau mot de passe
    // ------------------------------------------------------------------

    /**
     * Change le mot de passe si le jeton est valide, puis le consomme.
     *
     * @throws IllegalArgumentException si le jeton est inconnu ou expiré (renvoyé en 400)
     */
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {

        PasswordResetToken token = tokenRepository
                .findByTokenHash(sha256Hex(rawToken.trim()))
                .orElseThrow(() -> new IllegalArgumentException(LIEN_INVALIDE));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token);
            throw new IllegalArgumentException(LIEN_INVALIDE);
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Usage unique : tous les jetons de l'utilisateur sont supprimés.
        tokenRepository.deleteByUserId(user.getId());

        emailService.envoyerConfirmationModificationMotDePasse(user.getEmail(), user.getPrenom());
    }

    // ------------------------------------------------------------------
    // Nettoyage
    // ------------------------------------------------------------------

    /** Supprime chaque nuit les jetons expirés. */
    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void purgeExpiredTokens() {
        int deleted = tokenRepository.deleteExpired(LocalDateTime.now());
        if (deleted > 0) {
            log.info("{} jeton(s) de réinitialisation expiré(s) supprimé(s)", deleted);
        }
    }

    // ------------------------------------------------------------------
    // Utilitaires
    // ------------------------------------------------------------------

    /** 32 octets aléatoires encodés en Base64 URL (43 caractères, sans padding). */
    private String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String buildLink(String rawToken, boolean adminSpace) {
        String base = frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
        String page = adminSpace ? "/admin-reset-password" : "/reset-password";
        return base + page + "?token=" + rawToken;
    }

    static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 est garanti par toute JVM
            throw new IllegalStateException(e);
        }
    }
}
