package com.mystudentguide.config;

import com.mystudentguide.entity.User;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.UserRepository;
import com.mystudentguide.service.DeuxFacteursService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crée automatiquement le compte administrateur au démarrage de l'API.
 *
 * Les identifiants sont lus dans les variables d'environnement :
 * - ADMIN_EMAIL    : email de l'administrateur
 * - ADMIN_PASSWORD : mot de passe (chiffré en BCrypt avant l'enregistrement)
 * - ADMIN_PRENOM / ADMIN_NOM : facultatifs
 *
 * Fonctionnement :
 * - si ADMIN_EMAIL ou ADMIN_PASSWORD n'est pas défini : rien n'est fait ;
 * - si aucun compte n'existe avec cet email : l'administrateur est créé ;
 * - si un compte existe déjà avec cet email : il reçoit le rôle ADMIN
 *   (son mot de passe n'est PAS modifié).
 *
 * Le code peut donc s'exécuter à chaque démarrage sans créer de doublon.
 *
 * Secours : ADMIN_2FA_RESET=true désactive la double authentification de ce
 * compte au démarrage (téléphone ET codes de secours perdus). À remettre à
 * false juste après, puis réactiver la double authentification.
 */
@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String adminEmail;
    private final String adminPassword;
    private final String adminPrenom;
    private final String adminNom;
    private final boolean reinitialiserDeuxFacteurs;

    public AdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword,
            @Value("${app.admin.prenom:Admin}") String adminPrenom,
            @Value("${app.admin.nom:MyStudentGuide}") String adminNom,
            @Value("${app.admin.reset-2fa:false}") boolean reinitialiserDeuxFacteurs
    ) {
        this.reinitialiserDeuxFacteurs = reinitialiserDeuxFacteurs;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminPrenom = adminPrenom;
        this.adminNom = adminNom;
    }

    @Override
    @Transactional
    public void run(String... args) {

        // Aucune configuration : on ne fait rien.
        if (adminEmail == null || adminEmail.isBlank()
                || adminPassword == null || adminPassword.isBlank()) {
            log.info("ADMIN_EMAIL / ADMIN_PASSWORD non définis : aucun administrateur créé automatiquement.");
            return;
        }

        String email = adminEmail.trim().toLowerCase();

        userRepository.findByEmail(email).ifPresentOrElse(
                existant -> {
                    // Le compte existe déjà : on s'assure seulement qu'il est ADMIN.
                    if (existant.getRole() != RoleType.ADMIN) {
                        existant.setRole(RoleType.ADMIN);
                        userRepository.save(existant);
                        log.info("Le compte {} a reçu le rôle ADMIN.", email);
                    } else {
                        log.info("Administrateur {} déjà présent.", email);
                    }

                    if (reinitialiserDeuxFacteurs && existant.deuxFacteursActive()) {
                        DeuxFacteursService.reinitialiser(existant);
                        userRepository.save(existant);
                        log.warn("Double authentification désactivée pour {} (ADMIN_2FA_RESET). "
                                + "Remettez ADMIN_2FA_RESET à false.", email);
                    }
                },
                () -> {
                    // Aucun compte : on crée l'administrateur.
                    User admin = User.builder()
                            .prenom(adminPrenom)
                            .nom(adminNom)
                            .email(email)
                            .password(passwordEncoder.encode(adminPassword))
                            .role(RoleType.ADMIN)
                            .enabled(true)
                            .emailVerified(true)
                            .build();

                    userRepository.save(admin);
                    log.info("Administrateur {} créé.", email);
                }
        );
    }
}
