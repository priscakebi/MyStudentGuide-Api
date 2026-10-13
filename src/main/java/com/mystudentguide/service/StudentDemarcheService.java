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
 * Lorsqu'une démarche est attribuée à un étudiant,
 * une notification est envoyée à cet étudiant.
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
    // ATTRIBUER UNE DÉMARCHE À UN ÉTUDIANT
    // ============================================================

    /**
     * Ajoute une démarche générale au suivi d'un étudiant.
     *
     * Lorsqu'une démarche est ajoutée :
     *
     * 1. Le profil étudiant est recherché.
     * 2. La démarche générale est recherchée.
     * 3. Le service vérifie que le couple étudiant/démarche
     *    n'existe pas déjà.
     * 4. Une StudentDemarche est créée.
     * 5. Une notification est envoyée à l'étudiant.
     *
     * IMPORTANT :
     *
     * Aucun StudentDocument n'est créé.
     *
     * Les documents requis restent associés à la démarche
     * via DocumentRequis et sont simplement consultables
     * par l'étudiant.
     *
     * @param studentProfileId identifiant du profil étudiant
     * @param demarcheId identifiant de la démarche
     * @return StudentDemarche créée
     */
    @Transactional
    public StudentDemarche assignDemarcheToStudent(
            Long studentProfileId,
            Long demarcheId
    ) {

        // --------------------------------------------------------
        // 1. Vérifier que l'étudiant existe
        // --------------------------------------------------------

        StudentProfile studentProfile =
                studentProfileRepository.findById(studentProfileId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Profil étudiant introuvable avec l'id : "
                                                + studentProfileId
                                )
                        );


        // --------------------------------------------------------
        // 2. Vérifier que la démarche existe
        // --------------------------------------------------------

        Demarche demarche =
                demarcheRepository.findById(demarcheId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Démarche introuvable avec l'id : "
                                                + demarcheId
                                )
                        );


        // --------------------------------------------------------
        // 3. Vérifier que le couple étudiant/démarche
        //    n'existe pas déjà
        // --------------------------------------------------------

        boolean alreadyExists =
                studentDemarcheRepository
                        .existsByStudentProfile_IdAndDemarche_Id(
                                studentProfileId,
                                demarcheId
                        );

        /*
         * Cette vérification évite de créer un doublon.
         *
         * Une deuxième protection existe également :
         * la contrainte UNIQUE présente dans StudentDemarche.
         */
        if (alreadyExists) {

            throw new IllegalStateException(
                    "Cette démarche est déjà suivie "
                            + "par cet étudiant."
            );
        }


        // --------------------------------------------------------
        // 4. Créer le suivi étudiant/démarche
        // --------------------------------------------------------

        StudentDemarche studentDemarche =
                new StudentDemarche();

        /*
         * Association avec le profil étudiant.
         */
        studentDemarche.setStudentProfile(studentProfile);

        /*
         * Association avec la démarche générale.
         */
        studentDemarche.setDemarche(demarche);

        /*
         * Une nouvelle démarche commence avec le statut A_FAIRE.
         */
        studentDemarche.setStatut(DemarcheStatus.A_FAIRE);

        /*
         * Enregistre la date d'attribution.
         */
        studentDemarche.setDateAttribution(
                LocalDate.now()
        );


        // --------------------------------------------------------
        // 5. Sauvegarder le suivi
        // --------------------------------------------------------

        StudentDemarche savedStudentDemarche =
                studentDemarcheRepository.save(studentDemarche);


        // --------------------------------------------------------
        // 6. NOTIFICATION DE L'ÉTUDIANT
        // --------------------------------------------------------

        /*
         * Récupère le compte utilisateur associé
         * au profil étudiant.
         */
        User student = studentProfile.getUser();

        /*
         * Vérifie qu'un compte utilisateur est bien associé
         * au profil avant de créer la notification.
         */
        if (student != null) {

            Notification notification =
                    Notification.builder()
                            .titre("Nouvelle démarche attribuée")
                            .message(
                                    "Une nouvelle démarche vous a été "
                                            + "attribuée : \""
                                            + demarche.getTitre()
                                            + "\"."
                            )
                            .type(NotificationType.DEMARCHE)
                            .lu(false)
                            .user(student)
                            .build();

            /*
             * Enregistre la notification.
             */
            notificationService.saveNotification(
                    notification
            );
        }


        // --------------------------------------------------------
        // 7. Retourner le suivi créé
        // --------------------------------------------------------

        return savedStudentDemarche;
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