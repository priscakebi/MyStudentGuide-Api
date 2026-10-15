package com.mystudentguide.service;

import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.StudentDemarche;
import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.DemarcheStatus;
import com.mystudentguide.enums.NotificationType;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.repository.StudentDemarcheRepository;
import com.mystudentguide.repository.StudentProfileRepository;
import com.mystudentguide.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service permettant de gérer les démarches personnelles
 * suivies par les étudiants.
 *
 * ------------------------------------------------------------
 * ARCHITECTURE
 * ------------------------------------------------------------
 *
 * Demarche
 *    ↓
 * représente une démarche administrative générale.
 *
 * StudentDemarche
 *    ↓
 * représente le suivi de cette démarche
 * pour un étudiant précis.
 *
 * StudentProfile
 *    ↓
 * représente le profil de l'étudiant.
 *
 * ------------------------------------------------------------
 * DOCUMENTS
 * ------------------------------------------------------------
 *
 * Les documents personnels des étudiants ne sont PAS gérés
 * dans cette classe.
 *
 * MyStudentGuide affiche uniquement les documents requis
 * pour une démarche via l'entité DocumentRequis.
 *
 * Aucun fichier personnel n'est :
 * - importé ;
 * - stocké ;
 * - attribué ;
 * - supprimé
 * par ce service.
 *
 * ------------------------------------------------------------
 * NOTIFICATIONS
 * ------------------------------------------------------------
 *
 * Lorsqu'un étudiant modifie le statut d'une démarche,
 * une notification est envoyée aux administrateurs.
 *
 * Les démarches ne sont plus attribuées : un administrateur
 * peut suggérer une démarche (notification à l'étudiant),
 * et le suivi n'est créé que lorsque l'étudiant clique
 * sur « Commencer ».
 */
@Service
public class StudentDemarcheService {

    /**
     * Repository permettant de gérer les associations
     * entre les étudiants et les démarches.
     */
    private final StudentDemarcheRepository studentDemarcheRepository;

    /**
     * Repository permettant de rechercher les démarches
     * administratives générales.
     */
    private final DemarcheRepository demarcheRepository;

    /**
     * Repository permettant de rechercher les profils étudiants.
     */
    private final StudentProfileRepository studentProfileRepository;

    /**
     * Repository permettant de rechercher les utilisateurs,
     * notamment les administrateurs.
     */
    private final UserRepository userRepository;

    /**
     * Service permettant de créer et gérer les notifications.
     */
    private final NotificationService notificationService;


    /**
     * Constructeur du service.
     *
     * Spring injecte automatiquement les repositories
     * et services nécessaires.
     */
    public StudentDemarcheService(
            StudentDemarcheRepository studentDemarcheRepository,
            DemarcheRepository demarcheRepository,
            StudentProfileRepository studentProfileRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.studentDemarcheRepository = studentDemarcheRepository;
        this.demarcheRepository = demarcheRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }


    // ============================================================
    // RÉCUPÉRER TOUTES LES STUDENT DEMARCHES
    // ============================================================

    /**
     * Retourne toutes les démarches suivies par les étudiants.
     *
     * Cette méthode pourra notamment être utilisée par
     * l'administration pour obtenir une vue globale.
     *
     * @return liste de toutes les StudentDemarche
     */
    @Transactional(readOnly = true)
    public List<StudentDemarche> getAllStudentDemarches() {

        return studentDemarcheRepository.findAll();
    }


    // ============================================================
    // RÉCUPÉRER UNE STUDENT DEMARCHE
    // ============================================================

    /**
     * Recherche une StudentDemarche par son identifiant.
     *
     * @param id identifiant de StudentDemarche
     * @return StudentDemarche trouvée
     */
    @Transactional(readOnly = true)
    public StudentDemarche getStudentDemarcheById(Long id) {

        return studentDemarcheRepository.findByIdWithDetails(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Suivi de démarche introuvable avec l'id : "
                                        + id
                        )
                );
    }


    // ============================================================
    // RÉCUPÉRER LES DÉMARCHES D'UN ÉTUDIANT
    // ============================================================

    /**
     * Retourne toutes les démarches personnelles
     * suivies par un étudiant.
     *
     * Le repository utilise JOIN FETCH pour charger
     * la démarche générale associée.
     *
     * @param studentProfileId identifiant du profil étudiant
     * @return liste des démarches suivies
     */
    @Transactional(readOnly = true)
    public List<StudentDemarche> getDemarchesByStudent(
            Long studentProfileId
    ) {

        /*
         * Vérifie que le profil étudiant existe.
         */
        if (!studentProfileRepository.existsById(studentProfileId)) {

            throw new RuntimeException(
                    "Profil étudiant introuvable avec l'id : "
                            + studentProfileId
            );
        }

        /*
         * Retourne les démarches suivies par l'étudiant.
         */
        return studentDemarcheRepository
                .findByStudentProfileId(studentProfileId);
    }


    // ============================================================
    // RÉCUPÉRER LES DÉMARCHES D'UN ÉTUDIANT PAR STATUT
    // ============================================================

    /**
     * Retourne les démarches d'un étudiant
     * correspondant à un statut donné.
     *
     * Exemple :
     * récupérer toutes les démarches EN_COURS.
     *
     * @param studentProfileId identifiant du profil étudiant
     * @param statut statut recherché
     * @return liste des démarches correspondantes
     */
    @Transactional(readOnly = true)
    public List<StudentDemarche> getDemarchesByStudentAndStatus(
            Long studentProfileId,
            DemarcheStatus statut
    ) {

        /*
         * Vérifie que le profil étudiant existe.
         */
        if (!studentProfileRepository.existsById(studentProfileId)) {

            throw new RuntimeException(
                    "Profil étudiant introuvable avec l'id : "
                            + studentProfileId
            );
        }

        /*
         * Recherche les démarches correspondant au statut.
         */
        return studentDemarcheRepository
                .findByStudentProfileIdAndStatut(
                        studentProfileId,
                        statut
                );
    }


    // ============================================================
    // RÉCUPÉRER LES DÉMARCHES SELON UN STATUT
    // ============================================================

    /**
     * Retourne toutes les StudentDemarche
     * possédant un statut donné.
     *
     * Cette méthode est particulièrement utile
     * pour l'administration.
     *
     * @param statut statut recherché
     * @return liste des StudentDemarche
     */
    @Transactional(readOnly = true)
    public List<StudentDemarche> getDemarchesByStatus(
            DemarcheStatus statut
    ) {

        return studentDemarcheRepository.findByStatut(statut);
    }


    // ============================================================
    // RÉCUPÉRER UN SUIVI PRÉCIS
    // ============================================================

    /**
     * Recherche le suivi d'une démarche précise
     * pour un étudiant donné.
     *
     * Cette méthode utilise la contrainte logique :
     *
     * étudiant + démarche = un seul suivi.
     *
     * @param studentProfileId identifiant du profil étudiant
     * @param demarcheId identifiant de la démarche
     * @return StudentDemarche trouvée
     */
    @Transactional(readOnly = true)
    public StudentDemarche getByStudentAndDemarche(
            Long studentProfileId,
            Long demarcheId
    ) {

        return studentDemarcheRepository
                .findByStudentProfileIdAndDemarcheId(
                        studentProfileId,
                        demarcheId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cette démarche n'est pas suivie "
                                        + "par cet étudiant."
                        )
                );
    }


    // ============================================================
    // DÉMARCHES COMMENCÉES PAR L'ÉTUDIANT CONNECTÉ
    // ============================================================

    /**
     * Retourne uniquement les démarches que l'étudiant
     * a commencées (EN_COURS ou TERMINEE).
     *
     * C'est la liste affichée dans « Mes démarches »
     * du dashboard étudiant.
     *
     * Un étudiant sans profil n'a encore rien commencé :
     * la liste est alors vide.
     *
     * @param email email de l'utilisateur connecté
     * @return démarches commencées
     */
    @Transactional(readOnly = true)
    public List<StudentDemarche> getStartedDemarches(String email) {

        return studentProfileRepository.findByUserEmail(email)
                .map(profile -> studentDemarcheRepository
                        .findByStudentProfileId(profile.getId())
                        .stream()
                        .filter(sd -> sd.getStatut() == DemarcheStatus.EN_COURS
                                || sd.getStatut() == DemarcheStatus.TERMINEE)
                        .toList())
                .orElse(List.of());
    }


    // ============================================================
    // COMMENCER UNE DÉMARCHE (ÉTUDIANT)
    // ============================================================

    /**
     * L'étudiant connecté commence une démarche.
     *
     * Toutes les démarches sont visibles par tous : il n'y a plus
     * d'attribution. Le suivi (StudentDemarche) n'est créé qu'au
     * clic sur « Commencer », directement au statut EN_COURS.
     *
     * - aucun suivi        : création EN_COURS (date de début = aujourd'hui) ;
     * - suivi A_FAIRE ou
     *   NON_CONCERNEE      : repasse EN_COURS ;
     * - déjà commencé      : le suivi existant est renvoyé tel quel.
     *
     * Si l'étudiant n'a pas encore de profil, un profil vide
     * est créé : il pourra le compléter plus tard.
     *
     * Les suggestions encore ouvertes pour cette démarche
     * sont closes.
     *
     * @param email email de l'utilisateur connecté
     * @param demarcheId démarche à commencer
     * @return suivi de la démarche
     */
    @Transactional
    public StudentDemarche startDemarche(
            String email,
            Long demarcheId
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur connecté introuvable"
                        ));

        Demarche demarche =
                demarcheRepository.findById(demarcheId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Démarche introuvable avec l'id : "
                                                + demarcheId
                                ));

        StudentProfile studentProfile =
                studentProfileRepository.findByUserEmail(email)
                        .orElseGet(() -> createEmptyProfile(user));

        notificationService.closePendingSuggestions(
                user.getId(),
                demarcheId
        );

        // --------------------------------------------------------
        // Suivi déjà existant
        // --------------------------------------------------------

        StudentDemarche existing =
                studentDemarcheRepository
                        .findByStudentProfileIdAndDemarcheId(
                                studentProfile.getId(),
                                demarcheId
                        )
                        .orElse(null);

        if (existing != null) {

            if (existing.getStatut() == DemarcheStatus.A_FAIRE
                    || existing.getStatut() == DemarcheStatus.NON_CONCERNEE) {

                return updateStatus(existing.getId(), DemarcheStatus.EN_COURS);
            }

            return existing;
        }

        // --------------------------------------------------------
        // Nouveau suivi, directement EN_COURS
        // --------------------------------------------------------

        StudentDemarche studentDemarche = new StudentDemarche();
        studentDemarche.setStudentProfile(studentProfile);
        studentDemarche.setDemarche(demarche);
        studentDemarche.setStatut(DemarcheStatus.EN_COURS);
        studentDemarche.setDateAttribution(LocalDate.now());

        /*
         * La date de début sert de point de départ
         * au rappel de J+10 sur l'envoi des documents.
         */
        studentDemarche.setDateDebut(LocalDate.now());

        StudentDemarche savedStudentDemarche =
                studentDemarcheRepository.save(studentDemarche);

        // --------------------------------------------------------
        // Notification des administrateurs
        // --------------------------------------------------------

        for (User admin : userRepository.findByRole(RoleType.ADMIN)) {

            notificationService.saveNotification(
                    Notification.builder()
                            .titre("Démarche commencée")
                            .message(
                                    "L'étudiant "
                                            + user.getPrenom()
                                            + " "
                                            + user.getNom()
                                            + " a commencé la démarche \""
                                            + demarche.getTitre()
                                            + "\"."
                            )
                            .type(NotificationType.DEMARCHE)
                            .lu(false)
                            .user(admin)
                            .build()
            );
        }

        return savedStudentDemarche;
    }


    /**
     * Crée un profil étudiant vide pour un utilisateur
     * qui commence une démarche avant d'avoir rempli son profil.
     */
    private StudentProfile createEmptyProfile(User user) {

        StudentProfile profile = new StudentProfile();
        profile.setUser(user);
        user.setStudentProfile(profile);

        return studentProfileRepository.save(profile);
    }


    // ============================================================
    // SUGGÉRER UNE DÉMARCHE (ADMIN)
    // ============================================================

    /**
     * Envoie à un étudiant une notification de suggestion
     * pour une démarche.
     *
     * Remplace l'ancienne « attribution » : aucun suivi n'est
     * créé. L'étudiant reçoit une notification avec les boutons
     * « Voir plus » et « Commencer ».
     *
     * @param studentProfileId profil de l'étudiant ciblé
     * @param demarcheId démarche suggérée
     * @return notification créée
     * @throws IllegalArgumentException si la démarche est déjà
     *         commencée ou déjà suggérée sans réponse
     */
    @Transactional
    public Notification suggestDemarche(
            Long studentProfileId,
            Long demarcheId
    ) {

        StudentProfile studentProfile =
                studentProfileRepository.findById(studentProfileId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Profil étudiant introuvable avec l'id : "
                                                + studentProfileId
                                ));

        Demarche demarche =
                demarcheRepository.findById(demarcheId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Démarche introuvable avec l'id : "
                                                + demarcheId
                                ));

        User student = studentProfile.getUser();

        if (student == null) {
            throw new IllegalArgumentException(
                    "Aucun compte n'est associé à ce profil."
            );
        }

        boolean alreadyStarted =
                studentDemarcheRepository
                        .findByStudentProfileIdAndDemarcheId(
                                studentProfileId,
                                demarcheId
                        )
                        .map(sd -> sd.getStatut() == DemarcheStatus.EN_COURS
                                || sd.getStatut() == DemarcheStatus.TERMINEE)
                        .orElse(false);

        if (alreadyStarted) {
            throw new IllegalArgumentException(
                    "L'étudiant a déjà commencé cette démarche."
            );
        }

        if (notificationService.hasPendingSuggestion(student.getId(), demarcheId)) {
            throw new IllegalArgumentException(
                    "Cette démarche a déjà été suggérée à l'étudiant."
            );
        }

        return notificationService.saveNotification(
                Notification.builder()
                        .titre("Démarche suggérée pour vous")
                        .message(
                                "D'après votre profil, la démarche \""
                                        + demarche.getTitre()
                                        + "\" pourrait vous concerner. "
                                        + "Consultez-la ou commencez-la dès maintenant."
                        )
                        .type(NotificationType.DEMARCHE)
                        .lu(false)
                        .user(student)
                        .demarcheId(demarcheId)
                        .action(Notification.ACTION_SUGGESTION_DEMARCHE)
                        .actionTraitee(false)
                        .build()
        );
    }


    // ============================================================
    // MODIFIER LE STATUT
    // ============================================================

    /**
     * Modifie le statut d'une démarche personnelle.
     *
     * Lorsque la démarche passe à TERMINEE,
     * la date de terminaison est automatiquement renseignée.
     *
     * Lorsqu'elle quitte le statut TERMINEE,
     * la date de terminaison est supprimée.
     *
     * Une notification est ensuite envoyée
     * aux administrateurs.
     *
     * @param id identifiant de StudentDemarche
     * @param nouveauStatut nouveau statut
     * @return StudentDemarche mise à jour
     */
    @Transactional
    public StudentDemarche updateStatus(
            Long id,
            DemarcheStatus nouveauStatut
    ) {

        /*
         * Vérifie que le nouveau statut n'est pas null.
         */
        if (nouveauStatut == null) {

            throw new IllegalArgumentException(
                    "Le statut ne peut pas être null."
            );
        }

        /*
         * Recherche le suivi de démarche.
         */
        StudentDemarche studentDemarche =
                getStudentDemarcheById(id);


        // --------------------------------------------------------
        // Mise à jour du statut
        // --------------------------------------------------------

        DemarcheStatus ancienStatut = studentDemarche.getStatut();

        studentDemarche.setStatut(nouveauStatut);


        // --------------------------------------------------------
        // Progression : date de début et envoi des documents
        // --------------------------------------------------------

        if (nouveauStatut == DemarcheStatus.EN_COURS
                && studentDemarche.getDateDebut() == null) {

            /*
             * Clic sur « Commencer » : la démarche passe EN_COURS.
             * La date de début sert de point de départ au rappel
             * de J+10 (« Avez-vous envoyé vos documents ? »).
             */
            studentDemarche.setDateDebut(LocalDate.now());
            studentDemarche.setDernierRappel(null);
        }

        if (nouveauStatut == DemarcheStatus.A_FAIRE
                || nouveauStatut == DemarcheStatus.NON_CONCERNEE) {

            /*
             * Retour en arrière : la progression repart de zéro.
             */
            studentDemarche.setDateDebut(null);
            studentDemarche.setDocumentsEnvoyes(false);
            studentDemarche.setDateEnvoiDocuments(null);
        }

        if (nouveauStatut != DemarcheStatus.EN_COURS
                && ancienStatut != nouveauStatut) {

            /*
             * La question sur les documents n'a plus lieu d'être.
             */
            notificationService.closePendingActions(
                    studentDemarche.getId(),
                    Notification.ACTION_CONFIRMER_DOCUMENTS
            );
        }


        // --------------------------------------------------------
        // Gestion de la date de terminaison
        // --------------------------------------------------------

        if (nouveauStatut == DemarcheStatus.TERMINEE) {

            /*
             * Enregistre automatiquement la date du jour.
             */
            studentDemarche.setDateTerminaison(
                    LocalDate.now()
            );

        } else {

            /*
             * Si la démarche n'est plus terminée,
             * aucune date de terminaison ne doit être conservée.
             */
            studentDemarche.setDateTerminaison(null);
        }


        // --------------------------------------------------------
        // Sauvegarde
        // --------------------------------------------------------

        StudentDemarche savedStudentDemarche =
                studentDemarcheRepository.save(
                        studentDemarche
                );


        // --------------------------------------------------------
        // NOTIFICATION DES ADMINISTRATEURS
        // --------------------------------------------------------

        /*
         * Récupère le profil de l'étudiant.
         */
        StudentProfile studentProfile =
                savedStudentDemarche.getStudentProfile();

        /*
         * Récupère le compte utilisateur de l'étudiant.
         */
        User student = studentProfile.getUser();

        /*
         * Récupère tous les administrateurs.
         */
        List<User> admins =
                userRepository.findByRole(RoleType.ADMIN);


        /*
         * Envoie une notification à chaque administrateur.
         */
        for (User admin : admins) {

            Notification notification =
                    Notification.builder()
                            .titre("Statut d'une démarche modifié")
                            .message(
                                    "L'étudiant "
                                            + student.getPrenom()
                                            + " "
                                            + student.getNom()
                                            + " a modifié le statut de la démarche \""
                                            + savedStudentDemarche
                                                    .getDemarche()
                                                    .getTitre()
                                            + "\" : "
                                            + nouveauStatut
                                            + "."
                            )
                            .type(NotificationType.DEMARCHE)
                            .lu(false)
                            .user(admin)
                            .build();

            /*
             * Enregistre la notification.
             */
            notificationService.saveNotification(
                    notification
            );
        }


        return savedStudentDemarche;
    }


    // ============================================================
    // AJOUTER / MODIFIER UN COMMENTAIRE
    // ============================================================

    /**
     * Modifie le commentaire associé à une démarche.
     *
     * @param id identifiant de StudentDemarche
     * @param commentaire commentaire à enregistrer
     * @return StudentDemarche mise à jour
     */
    /**
     * Réponse de l'étudiant à la question de J+10 :
     * « Avez-vous envoyé vos documents sur la plateforme dédiée ? ».
     *
     * - Oui : la progression passe à 50 %.
     * - Pas encore : la question sera reposée 10 jours plus tard.
     */
    @Transactional
    public StudentDemarche confirmerEnvoiDocuments(
            Long id,
            boolean envoyes
    ) {

        StudentDemarche studentDemarche =
                getStudentDemarcheById(id);

        if (studentDemarche.getStatut() != DemarcheStatus.EN_COURS) {

            throw new IllegalArgumentException(
                    "Seule une démarche en cours peut être mise à jour."
            );
        }

        if (envoyes) {

            studentDemarche.setDocumentsEnvoyes(true);
            studentDemarche.setDateEnvoiDocuments(LocalDate.now());

        } else {

            /*
             * Le compteur de 10 jours repart d'aujourd'hui.
             */
            studentDemarche.setDocumentsEnvoyes(false);
            studentDemarche.setDernierRappel(LocalDate.now());
        }

        notificationService.closePendingActions(
                studentDemarche.getId(),
                Notification.ACTION_CONFIRMER_DOCUMENTS
        );

        return studentDemarcheRepository.save(studentDemarche);
    }


    @Transactional
    public StudentDemarche updateCommentaire(
            Long id,
            String commentaire
    ) {

        /*
         * Recherche la démarche personnelle.
         */
        StudentDemarche studentDemarche =
                getStudentDemarcheById(id);

        /*
         * Modifie le commentaire.
         */
        studentDemarche.setCommentaire(commentaire);

        /*
         * Sauvegarde les modifications.
         */
        return studentDemarcheRepository.save(
                studentDemarche
        );
    }


    // ============================================================
    // COMPTER LES DÉMARCHES PAR STATUT
    // ============================================================

    /**
     * Compte les démarches d'un étudiant
     * selon leur statut.
     *
     * @param studentProfileId identifiant du profil étudiant
     * @param statut statut recherché
     * @return nombre de démarches
     */
    @Transactional(readOnly = true)
    public long countByStudentAndStatus(
            Long studentProfileId,
            DemarcheStatus statut
    ) {

        /*
         * Vérifie que le profil étudiant existe.
         */
        if (!studentProfileRepository.existsById(studentProfileId)) {

            throw new RuntimeException(
                    "Profil étudiant introuvable avec l'id : "
                            + studentProfileId
            );
        }

        /*
         * Effectue directement le COUNT en base de données.
         */
        return studentDemarcheRepository
                .countByStudentProfile_IdAndStatut(
                        studentProfileId,
                        statut
                );
    }


    // ============================================================
    // SUPPRIMER UNE STUDENT DEMARCHE
    // ============================================================

    /**
     * Supprime le suivi d'une démarche pour un étudiant.
     *
     * IMPORTANT :
     *
     * Cette opération ne supprime PAS la démarche générale.
     *
     * La démarche reste disponible pour les autres étudiants.
     *
     * Elle supprime uniquement l'association
     * StudentProfile ↔ Demarche.
     *
     * @param id identifiant de StudentDemarche
     */
    @Transactional
    public void deleteStudentDemarche(Long id) {

        /*
         * Vérifie que l'association existe.
         */
        StudentDemarche studentDemarche =
                getStudentDemarcheById(id);

        /*
         * Supprime uniquement le suivi.
         */
        studentDemarcheRepository.delete(
                studentDemarche
        );
    }
}