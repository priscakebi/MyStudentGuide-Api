package com.mystudentguide.service;

import com.mystudentguide.entity.EmailVerificationToken;
import com.mystudentguide.entity.User;
import com.mystudentguide.repository.EmailVerificationTokenRepository;
import com.mystudentguide.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Confirmation de l'adresse e-mail d'un étudiant.
 *
 * - à l'inscription et après un changement d'adresse, un lien valable
 *   48 h est envoyé (bienvenue + confirmation) ;
 * - tant que l'adresse n'est pas confirmée, l'étudiant peut se connecter
 *   et utiliser le site (un bandeau l'invite à confirmer), mais il ne
 *   reçoit pas les rappels d'échéance par e-mail : une adresse mal saisie
 *   ne recevrait que des messages destinés à quelqu'un d'autre ;
 * - les comptes créés avant cette fonctionnalité restent confirmés.
 */
@Service
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    public static final String LIEN_INVALIDE =
            "Ce lien de confirmation est invalide ou a expiré. Demandez-en un nouveau depuis votre espace.";

    public static final String TROP_TOT =
            "Un lien vient d'être envoyé. Patientez une minute avant d'en demander un autre.";

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final String frontendUrl;
    private final long expirationHeures;
    private final long cooldownSecondes;

    public EmailVerificationService(
            UserRepository userRepository,
            EmailVerificationTokenRepository tokenRepository,
            EmailService emailService,
            @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl,
            @Value("${app.email-verification.expiration-hours:48}") long expirationHeures,
            @Value("${app.email-verification.cooldown-seconds:60}") long cooldownSecondes
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
        this.expirationHeures = expirationHeures;
        this.cooldownSecondes = cooldownSecondes;
    }

    /**
     * Envoie un nouveau lien de confirmation (les précédents sont annulés).
     *
     * @param bienvenue vrai à l'inscription (e-mail de bienvenue), faux après
     *                  un changement d'adresse ou une nouvelle demande
     */
    @Transactional
    public void envoyerLien(User user, boolean bienvenue) {
        tokenRepository.deleteByUserId(user.getId());

        String jeton = genererJeton();
        EmailVerificationToken token = new EmailVerificationToken();
        token.setTokenHash(PasswordResetService.sha256Hex(jeton));
        token.setUser(user);
        token.setEmail(user.getEmail());
        token.setExpiresAt(LocalDateTime.now().plusHours(expirationHeures));
        tokenRepository.save(token);

        emailService.envoyerLienConfirmationEmail(
                user.getEmail(), user.getPrenom(), lien(jeton), expirationHeures, bienvenue);
    }

    /** Nouvelle demande depuis l'espace étudiant (une par minute au plus). */
    @Transactional
    public void renvoyer(String emailConnecte) {
        User user = userRepository.findByEmail(emailConnecte)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return;
        }
        if (tokenRepository.existsByUser_IdAndCreatedAtAfter(
                user.getId(), LocalDateTime.now().minusSeconds(cooldownSecondes))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, TROP_TOT);
        }
        envoyerLien(user, false);
    }

    /** Lien cliqué dans l'e-mail : l'adresse est confirmée. */
    @Transactional
    public void confirmer(String jeton) {
        EmailVerificationToken token = tokenRepository
                .findByTokenHash(PasswordResetService.sha256Hex(jeton.trim()))
                .orElseThrow(() -> new IllegalArgumentException(LIEN_INVALIDE));

        User user = token.getUser();

        // Lien expiré, ou envoyé à une adresse remplacée depuis : refusé.
        if (token.getExpiresAt().isBefore(LocalDateTime.now())
                || !token.getEmail().equalsIgnoreCase(user.getEmail())) {
            tokenRepository.delete(token);
            throw new IllegalArgumentException(LIEN_INVALIDE);
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        tokenRepository.deleteByUserId(user.getId());
    }

    @Scheduled(cron = "0 40 3 * * *")
    @Transactional
    public void purgerJetonsExpires() {
        int supprimes = tokenRepository.deleteExpired(LocalDateTime.now());
        if (supprimes > 0) {
            log.info("{} jeton(s) de confirmation d'e-mail expiré(s) supprimé(s)", supprimes);
        }
    }

    private String lien(String jeton) {
        String base = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
        return base + "/verifier-email?token=" + jeton;
    }

    private static String genererJeton() {
        byte[] octets = new byte[32];
        RANDOM.nextBytes(octets);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(octets);
    }
}
