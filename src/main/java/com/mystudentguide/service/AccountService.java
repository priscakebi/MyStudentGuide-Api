package com.mystudentguide.service;

import com.mystudentguide.validation.FormRules;
import com.mystudentguide.dto.account.DeleteAccountRequest;
import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.NotificationType;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.AvisRepository;
import com.mystudentguide.repository.DemandeAideRepository;
import com.mystudentguide.repository.SignalementDemarcheRepository;
import com.mystudentguide.repository.SuggestionEnvoyeeRepository;
import com.mystudentguide.repository.NotificationRepository;
import com.mystudentguide.repository.UserRepository;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsable de la gestion du compte utilisateur.
 *
 * Ce service contient la logique métier liée au compte,
 * notamment la suppression définitive d'un compte étudiant.
 *
 * La suppression suit les étapes suivantes :
 *
 * 1. Récupérer l'utilisateur actuellement connecté.
 * 2. Vérifier le mot de passe fourni.
 * 3. Récupérer les administrateurs.
 * 4. Préparer la notification destinée à l'administrateur.
 * 5. Envoyer un email à l'administrateur.
 * 6. Supprimer les notifications de l'étudiant.
 * 7. Supprimer le compte utilisateur.
 * 8. Envoyer un email de confirmation à l'étudiant.
 *
 * @Transactional permet de traiter les opérations sur la base
 * dans une seule transaction.
 */
@Service
public class AccountService {

    /**
     * Repository permettant d'accéder aux utilisateurs.
     */
    private final UserRepository userRepository;

    /**
     * Repository permettant de gérer les notifications.
     */
    private final NotificationRepository notificationRepository;

    /**
     * Encodeur utilisé pour vérifier le mot de passe.
     *
     * Le mot de passe stocké en base est chiffré avec BCrypt.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Service centralisant l'envoi des emails.
     */
    private final EmailService emailService;
    private final AvisRepository avisRepository;
    private final SignalementDemarcheRepository signalementRepository;
    private final SuggestionEnvoyeeRepository suggestionEnvoyeeRepository;
    private final DemandeAideRepository demandeAideRepository;

    /**
     * Constructeur du service.
     *
     * @param userRepository repository des utilisateurs
     * @param notificationRepository repository des notifications
     * @param passwordEncoder encodeur des mots de passe
     * @param emailService service d'envoi des emails
     */
    public AccountService(
            UserRepository userRepository,
            NotificationRepository notificationRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            AvisRepository avisRepository,
            SignalementDemarcheRepository signalementRepository,
            SuggestionEnvoyeeRepository suggestionEnvoyeeRepository,
            DemandeAideRepository demandeAideRepository
    ) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.avisRepository = avisRepository;
        this.signalementRepository = signalementRepository;
        this.suggestionEnvoyeeRepository = suggestionEnvoyeeRepository;
        this.demandeAideRepository = demandeAideRepository;
    }

    /**
     * Supprime définitivement le compte de l'utilisateur connecté.
     *
     * Le mot de passe fourni dans la demande est d'abord vérifié.
     * Si le mot de passe est incorrect, aucune suppression n'est effectuée.
     *
     * Les informations nécessaires à la notification et aux emails
     * sont récupérées avant la suppression du compte.
     *
     * @param request informations envoyées par le frontend
     * @throws IllegalArgumentException si le motif ou le mot de passe
     *                                  est absent
     * @throws BadCredentialsException si le mot de passe est incorrect
     * @throws IllegalStateException si aucun utilisateur connecté
     *                               n'est trouvé
     */
    @Transactional
    public void deleteCurrentAccount(DeleteAccountRequest request) {

        // ==========================================================
        // 1. VALIDATION DE LA DEMANDE
        // ==========================================================

        /*
         * Le motif est obligatoire.
         *
         * Même si le frontend effectue déjà cette validation,
         * le backend doit toujours effectuer ses propres vérifications.
         */
        if (request == null
                || request.getDeletionReason() == null
                || request.getDeletionReason().isBlank()) {

            throw new IllegalArgumentException(
                    "Le motif de suppression est obligatoire."
            );
        }

        /*
         * Le mot de passe est obligatoire car il sert
         * à confirmer l'identité de l'utilisateur.
         */
        if (request.getDeletionPassword() == null
                || request.getDeletionPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "Le mot de passe est obligatoire."
            );
        }

        // ==========================================================
        // 2. RÉCUPÉRATION DE L'UTILISATEUR CONNECTÉ
        // ==========================================================

        /*
         * Spring Security contient les informations
         * de l'utilisateur actuellement authentifié.
         */
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        /*
         * Si aucune authentification valide n'est disponible,
         * on ne peut pas savoir quel compte supprimer.
         */
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {

            throw new IllegalStateException(
                    "Aucun utilisateur connecté."
            );
        }

        /*
         * Dans notre application, getName() correspond à l'email
         * car User.getUsername() retourne l'email.
         */
        String email = authentication.getName();

        /*
         * Recherche de l'utilisateur dans la base.
         */
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Utilisateur connecté introuvable."
                        )
                );

        // ==========================================================
        // 3. VÉRIFICATION DU MOT DE PASSE
        // ==========================================================

        /*
         * Le mot de passe envoyé par Angular est comparé
         * au mot de passe BCrypt enregistré en base.
         *
         * On ne déchiffre jamais le mot de passe stocké.
         */
        boolean passwordCorrect =
                passwordEncoder.matches(
                        request.getDeletionPassword(),
                        user.getPassword()
                );

        /*
         * Si le mot de passe ne correspond pas,
         * on arrête immédiatement le traitement.
         */
        if (!passwordCorrect) {

            throw new BadCredentialsException(
                    "Le mot de passe est incorrect."
            );
        }

        // ==========================================================
        // 4. RÉCUPÉRATION DES INFORMATIONS AVANT SUPPRESSION
        // ==========================================================

        /*
         * Ces informations doivent être conservées en mémoire
         * avant la suppression du User.
         */
        String userPrenom = user.getPrenom();
        String userNom = user.getNom();
        String userEmail = user.getEmail();

        String motif = request.getDeletionReason();
        String details = request.getDeletionDetails();

        // ==========================================================
        // 5. RÉCUPÉRATION DES ADMINISTRATEURS
        // ==========================================================

        /*
         * Le UserRepository possède déjà findByRole().
         *
         * On récupère donc tous les utilisateurs ayant
         * le rôle ADMIN.
         */
        List<User> administrators =
                userRepository.findByRole(RoleType.ADMIN);

        /*
         * On vérifie qu'au moins un administrateur existe.
         */
        if (administrators.isEmpty()) {

            throw new IllegalStateException(
                    "Aucun administrateur n'est disponible."
            );
        }

        // ==========================================================
        // 6. CRÉATION DE LA NOTIFICATION ADMIN
        // ==========================================================

        /*
         * On informe l'administrateur qu'un étudiant
         * vient de supprimer son compte.
         */
        for (User administrator : administrators) {

            Notification notification = Notification.builder()
                    .titre("Suppression d'un compte étudiant")
                    .message(
                            "L'étudiant "
                            + userPrenom
                            + " "
                            + userNom
                            + " ("
                            + userEmail
                            + ") a supprimé son compte.\n\n"
                            + "Motif : "
                            + motif
                            + "\n"
                            + (
                                details != null && !details.isBlank()
                                    ? "Précisions : " + details
                                    : ""
                              )
                    )
                    .type(NotificationType.INFORMATION)
                    .lu(false)
                    .user(administrator)
                    .build();

            notificationRepository.save(notification);
        }

        // ==========================================================
        // 7. EMAIL À L'ADMINISTRATEUR
        // ==========================================================

        /*
         * Le système actuel possède déjà EmailService.
         *
         * On envoie un email à chaque administrateur.
         */
        for (User administrator : administrators) {

            emailService.envoyerSuppressionCompteAdmin(
                    administrator.getEmail(),
                    userPrenom,
                    userNom,
                    userEmail,
                    motif,
                    details
            );
        }

        // ==========================================================
        // 8. SUPPRESSION DES NOTIFICATIONS DE L'ÉTUDIANT
        // ==========================================================

        /*
         * Les notifications appartiennent directement au User.
         *
         * Elles doivent donc être supprimées avant le User
         * afin d'éviter une erreur de clé étrangère en base.
         *
         * La méthode deleteByUserId() devra être ajoutée
         * dans NotificationRepository.
         */
        // Les avis publics de l'étudiant sont supprimés avec son compte.
        avisRepository.deleteByUserId(user.getId());

        // Ses signalements de démarches aussi (données personnelles).
        signalementRepository.deleteByUserId(user.getId());

        // Ses demandes d'aide également.
        demandeAideRepository.deleteByUserId(user.getId());

        // Et l'historique de ses suggestions automatiques.
        suggestionEnvoyeeRepository.deleteByUserId(user.getId());

        notificationRepository.deleteByUserId(user.getId());

        // ==========================================================
        // 9. SUPPRESSION DU COMPTE
        // ==========================================================

        /*
         * User possède une relation OneToOne avec StudentProfile
         * avec cascade = CascadeType.ALL et orphanRemoval = true.
         *
         * StudentProfile possède également :
         *
         * - StudentDemarche
         * - StudentDocument
         *
         * avec cascade = CascadeType.ALL et orphanRemoval = true.
         *
         * La suppression du User entraînera donc la suppression
         * des données personnelles associées.
         */
        userRepository.delete(user);

        // ==========================================================
        // 10. EMAIL DE CONFIRMATION À L'ÉTUDIANT
        // ==========================================================

        /*
         * L'email de confirmation est envoyé après la suppression.
         *
         * On utilise les informations sauvegardées précédemment
         * car l'objet User vient d'être supprimé.
         */
        emailService.envoyerConfirmationSuppressionCompte(
                userEmail,
                userPrenom
        );
    }

    // ============================================================
    // CHANGER LE MOT DE PASSE (utilisateur connecté)
    // ============================================================

    /**
     * Change le mot de passe de l'utilisateur connecté.
     *
     * Règles (identiques au formulaire du front) :
     * - le mot de passe actuel doit être correct ;
     * - 8 caractères minimum, une minuscule, une majuscule,
     *   un chiffre et un caractère spécial (@$!%*?&) ;
     * - différent de l'ancien.
     *
     * Un e-mail de confirmation est envoyé.
     */
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "Le mot de passe actuel est obligatoire."
            );
        }

        if (!FormRules.isStrongPassword(newPassword)) {
            throw new IllegalArgumentException(FormRules.PASSWORD_MESSAGE);
        }

        User user = getCurrentUser();

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException(
                    "Le mot de passe actuel est incorrect."
            );
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException(
                    "Le nouveau mot de passe doit être différent de l'ancien."
            );
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        emailService.envoyerConfirmationModificationMotDePasse(
                user.getEmail(),
                user.getPrenom()
        );
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new IllegalStateException("Aucun utilisateur connecté.");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Utilisateur connecté introuvable.")
                );
    }
}
