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
import com.mystudentguide.security.LoginAttemptService;
import com.mystudentguide.validation.ConditionsUtilisation;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
     * Limite les tentatives de connexion (force brute).
     */
    private final LoginAttemptService loginAttemptService;

    /**
     * Lien de confirmation de l'adresse e-mail envoyé à l'inscription.
     */
    private final EmailVerificationService emailVerificationService;

    /**
     * Code de l'application d'authentification (administrateurs).
     */
    private final DeuxFacteursService deuxFacteursService;

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
     * @param loginAttemptService limitation des tentatives de connexion
     * @param emailVerificationService confirmation de l'adresse e-mail
     * @param deuxFacteursService double authentification
     */
    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        NotificationService notificationService,
        EmailService emailService,
        LoginAttemptService loginAttemptService,
        EmailVerificationService emailVerificationService,
        DeuxFacteursService deuxFacteursService
    ) {
        this.deuxFacteursService = deuxFacteursService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.loginAttemptService = loginAttemptService;
        this.emailVerificationService = emailVerificationService;
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
     * 7. On envoie à l'étudiant un e-mail de bienvenue avec le lien de
     *    confirmation de son adresse (compte utilisable sans attendre).
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

        // Preuve du consentement (RGPD) : version des conditions acceptées et date.
        // La case est obligatoire (RegisterRequest.accepteConditions, validé par @Valid).
        user.setConditionsVersion(ConditionsUtilisation.VERSION);
        user.setConditionsAccepteesLe(LocalDateTime.now());

        // Point de départ du délai d'inactivité (InactiviteService).
        user.setDerniereConnexion(LocalDateTime.now());

        // Adresse à confirmer par le lien reçu par e-mail.
        user.setEmailVerified(false);

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
        emailVerificationService.envoyerLien(savedUser, true);

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

        // Compte bloqué après trop d'échecs : refus AVANT de tester le mot de passe.
        loginAttemptService.verifier(request.getEmail());

        // Vérifie les identifiants fournis par l'utilisateur.
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
        } catch (AuthenticationException e) {
            loginAttemptService.echec(request.getEmail());
            throw e;
        }

        // Recherche l'utilisateur correspondant à l'adresse email.
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Utilisateur introuvable"
                )
            );

        // Double authentification : pas encore de JWT, un code est demandé.
        // Le compteur d'échecs n'est PAS remis à zéro : sinon, avec le mot de passe,
        // on pourrait essayer des codes sans limite.
        if (user.deuxFacteursActive()) {
            return AuthResponse.deuxFacteursRequis(
                jwtService.genererDefiDeuxFacteurs(user),
                user.getEmail()
            );
        }

        return connecter(user);
    }

    /**
     * Seconde étape de la connexion : code de l'application d'authentification
     * (ou code de secours). Les codes faux comptent comme des échecs de connexion :
     * après 5 erreurs, le compte est bloqué quelques minutes.
     *
     * @param defi jeton reçu à la première étape
     * @param code code saisi
     * @return réponse contenant le JWT
     */
    public AuthResponse loginDeuxFacteurs(String defi, String code) {

        String email = jwtService.emailDuDefi(defi);

        loginAttemptService.verifier(email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Utilisateur introuvable"
                )
            );

        if (!deuxFacteursService.verifierCode(user, code)) {
            loginAttemptService.echec(email);
            throw new IllegalArgumentException("Code incorrect.");
        }

        return connecter(user);
    }

    /**
     * Connexion réussie : compteur d'échecs remis à zéro,
     * dernière connexion mise à jour, JWT remis.
     */
    private AuthResponse connecter(User user) {

        loginAttemptService.succes(user.getEmail());

        // Compte actif : on repart de zéro pour la suppression des comptes inactifs
        // (et un éventuel avertissement de suppression est annulé).
        user.setDerniereConnexion(LocalDateTime.now());
        user.setAvertissementInactiviteLe(null);
        userRepository.save(user);

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

    /**
     * Renouvelle le JWT de l'utilisateur déjà connecté.
     *
     * Appelé par le front (IdleService) toutes les 10 minutes
     * tant que l'utilisateur est actif.
     */
    public AuthResponse refresh(String email) {

        User user = userRepository.findByEmail(email)
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Utilisateur introuvable"
                )
            );

        return new AuthResponse(
            jwtService.generateToken(user),
            user.getId(),
            user.getEmail(),
            user.getRole().toString()
        );
    }
}
