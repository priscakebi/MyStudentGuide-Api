package com.mystudentguide.service;

import com.mystudentguide.dto.auth.AuthResponse;
import com.mystudentguide.dto.auth.LoginRequest;
import com.mystudentguide.dto.auth.RegisterRequest;
import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.NotificationType;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.UserRepository;
import com.mystudentguide.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsable de l'authentification des utilisateurs.
 *
 * Ce service gère principalement :
 *
 * - l'inscription d'un nouvel utilisateur ;
 * - la connexion d'un utilisateur ;
 * - la génération du JWT ;
 * - l'envoi des emails liés à l'inscription.
 */
@Service
public class AuthService {

    /**
     * Repository permettant d'accéder aux utilisateurs
     * enregistrés dans la base de données.
     */
    private final UserRepository userRepository;

    /**
     * Encodeur utilisé pour sécuriser les mots de passe
     * avant leur enregistrement en base de données.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Gestionnaire utilisé par Spring Security
     * pour authentifier les utilisateurs lors de la connexion.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Service responsable de la génération et de la gestion
     * des tokens JWT.
     */
    private final JwtService jwtService;

    /**
     * Service responsable de la création des notifications
     * internes à l'application.
     */
    private final NotificationService notificationService;

    /**
     * Service responsable de l'envoi des emails.
     */
    private final EmailService emailService;

    /**
     * Constructeur du service.
     *
     * Toutes les dépendances nécessaires au fonctionnement
     * de l'authentification sont injectées par Spring.
     *
     * @param userRepository repository des utilisateurs
     * @param passwordEncoder encodeur des mots de passe
     * @param authenticationManager gestionnaire d'authentification
     * @param jwtService service de gestion des JWT
     * @param notificationService service des notifications internes
     * @param emailService service d'envoi des emails
     */
    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        NotificationService notificationService,
        EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    /**
     * Inscription d'un nouvel étudiant.
     *
     * Lors de l'inscription :
     *
     * 1. On vérifie que l'adresse email n'est pas déjà utilisée.
     * 2. On crée l'utilisateur.
     * 3. On encode son mot de passe.
     * 4. On lui attribue le rôle ETUDIANT.
     * 5. On sauvegarde l'utilisateur.
     * 6. On crée une notification interne pour les administrateurs.
     * 7. On envoie un email de confirmation à l'étudiant.
     * 8. On envoie un email à chaque administrateur.
     * 9. On génère le JWT.
     *
     * @param request données d'inscription envoyées par le frontend
     * @return réponse contenant le JWT et les informations du compte
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Normalisation : "  Prisca@Email.COM " et "prisca@email.com" sont le même compte.
        request.setEmail(request.getEmail().trim().toLowerCase());
        request.setNom(request.getNom().trim());
        request.setPrenom(request.getPrenom().trim());

        // Vérifie si un compte utilisant déjà cette adresse email existe.
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                "Cette adresse email est déjà utilisée"
            );
        }

        // Création du nouvel utilisateur.
        User user = new User();

        // Enregistrement de l'adresse email.
        user.setEmail(request.getEmail());

        // Le mot de passe est toujours encodé avant d'être enregistré.
        user.setPassword(
            passwordEncoder.encode(request.getPassword())
        );

        // Enregistrement du nom et du prénom.
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());

        // Tous les nouveaux comptes créés depuis l'inscription
        // sont automatiquement des étudiants.
        user.setRole(RoleType.ETUDIANT);

        // Sauvegarde du nouvel utilisateur en base de données.
        User savedUser = userRepository.save(user);

        // ==========================================================
        // NOTIFICATION INTERNE AUX ADMINISTRATEURS
        // ==========================================================

        // Recherche de tous les utilisateurs possédant le rôle ADMIN.
        List<User> admins = userRepository.findByRole(RoleType.ADMIN);

        // Création d'une notification interne pour chaque administrateur.
        for (User admin : admins) {

            Notification notification = Notification.builder()
                .titre("Nouvel étudiant inscrit")
                .message(
                    "Un nouvel étudiant vient de créer un compte "
                    + "sur MyStudentGuide : "
                    + savedUser.getPrenom()
                    + " "
                    + savedUser.getNom()
                )
                .type(NotificationType.SYSTEME)
                .lu(false)
                .user(admin)
                .build();

            // Enregistrement de la notification dans la base de données.
            notificationService.saveNotification(notification);

            // ======================================================
            // EMAIL À L'ADMINISTRATEUR
            // ======================================================

            // Envoie également un email à l'administrateur.
            emailService.envoyerNouvelleInscriptionAdmin(
                admin.getEmail(),
                savedUser.getPrenom(),
                savedUser.getNom(),
                savedUser.getEmail()
            );
        }

        // ==========================================================
        // EMAIL DE CONFIRMATION À L'ÉTUDIANT
        // ==========================================================

        // Envoie un email confirmant à l'étudiant
        // que son compte vient d'être créé.
        emailService.envoyerConfirmationInscription(
            savedUser.getEmail(),
            savedUser.getPrenom()
        );

        // ==========================================================
        // GÉNÉRATION DU JWT
        // ==========================================================

        // Génération du token permettant à l'étudiant
        // d'être authentifié immédiatement après son inscription.
        String token = jwtService.generateToken(savedUser);

        // Retourne les informations nécessaires au frontend.
        return new AuthResponse(
            token,
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getRole().toString()
        );
    }

    /**
     * Authentifie un utilisateur et génère son JWT.
     *
     * @param request données de connexion
     * @return réponse contenant le JWT et les informations du compte
     */
    public AuthResponse login(LoginRequest request) {

        request.setEmail(request.getEmail().trim());

        // Vérifie les identifiants fournis par l'utilisateur.
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        // Recherche l'utilisateur correspondant à l'adresse email.
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Utilisateur introuvable"
                )
            );

        // Génère le JWT après authentification réussie.
        String token = jwtService.generateToken(user);

        // Retourne les informations nécessaires au frontend.
        return new AuthResponse(
            token,
            user.getId(),
            user.getEmail(),
            user.getRole().toString()
        );
    }
}
