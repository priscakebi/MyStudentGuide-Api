package com.mystudentguide.service;

import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.StudentDemarche;
import com.mystudentguide.enums.DemarcheStatus;
import com.mystudentguide.enums.NotificationType;
import com.mystudentguide.repository.StudentDemarcheRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service chargé de gérer les rappels automatiques
 * concernant les démarches des étudiants.
 *
 * ------------------------------------------------------------
 * FONCTIONNEMENT
 * ------------------------------------------------------------
 *
 * Une tâche planifiée est exécutée automatiquement une fois
 * par jour à 09h00.
 *
 * Elle recherche les démarches qui sont :
 *
 * - A_FAIRE
 * - EN_COURS
 *
 * Pour chaque démarche, le service vérifie deux types
 * de rappels :
 *
 * 1. Rappel de progression tous les 10 jours.
 *
 * 2. Rappel d'échéance 7 jours avant la date limite.
 *
 * Les démarches TERMINEE ne sont jamais concernées.
 */
@Service
public class RappelDemarcheService {

    /**
     * Repository permettant de récupérer les démarches
     * attribuées aux étudiants.
     */
    private final StudentDemarcheRepository studentDemarcheRepository;

    /**
     * Service permettant de créer les notifications.
     */
    private final NotificationService notificationService;


    /**
     * Constructeur du service.
     *
     * @param studentDemarcheRepository repository des démarches étudiants
     * @param notificationService service de gestion des notifications
     */
    public RappelDemarcheService(
            StudentDemarcheRepository studentDemarcheRepository,
            NotificationService notificationService
    ) {
        this.studentDemarcheRepository = studentDemarcheRepository;
        this.notificationService = notificationService;
    }


    // ============================================================
    // RAPPELS AUTOMATIQUES
    // ============================================================

    /**
     * Vérifie quotidiennement les démarches nécessitant
     * un rappel.
     *
     * L'expression cron signifie :
     *
     * seconde = 0
     * minute = 0
     * heure = 9
     *
     * La tâche s'exécute donc tous les jours à 09h00.
     */
    // Exécution automatique tous les jours à 09h00.
@Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void envoyerRappelsAutomatiques() {

        // --------------------------------------------------------
        // 1. Récupérer les démarches A_FAIRE
        //    et EN_COURS
        // --------------------------------------------------------

        List<StudentDemarche> demarches =
                studentDemarcheRepository.findByStatut(
                        DemarcheStatus.A_FAIRE
                );

        demarches.addAll(
                studentDemarcheRepository.findByStatut(
                        DemarcheStatus.EN_COURS
                )
        );


        // Date du jour.
        LocalDate aujourdHui = LocalDate.now();


        // --------------------------------------------------------
        // 2. Vérifier chaque démarche
        // --------------------------------------------------------

        for (StudentDemarche studentDemarche : demarches) {

            // Vérification de sécurité :
            // une démarche sans étudiant ne peut pas recevoir
            // de notification.
            if (studentDemarche.getStudentProfile() == null) {
                continue;
            }

            if (studentDemarche
                    .getStudentProfile()
                    .getUser() == null) {

                continue;
            }


            // ----------------------------------------------------
            // 3. Récupérer le titre de la démarche
            // ----------------------------------------------------

            String titreDemarche =
                    studentDemarche.getDemarche() != null
                            ? studentDemarche
                                    .getDemarche()
                                    .getTitre()
                            : "votre démarche";


            // ====================================================
            // RAPPEL N°1 : TOUS LES 10 JOURS
            // ====================================================

            /*
             * Si un rappel a déjà été envoyé,
             * on utilise la date du dernier rappel.
             *
             * Sinon, on utilise la date d'attribution.
             */
            LocalDate dateReference =
                    studentDemarche.getDernierRappel();

            if (dateReference == null) {

                dateReference =
                        studentDemarche.getDateAttribution();
            }


            // ----------------------------------------------------
            // Vérifier que la date de référence existe
            // ----------------------------------------------------

            if (dateReference != null) {

                /*
                 * Calcul du nombre de jours écoulés
                 * depuis la date de référence.
                 */
                long nombreDeJours =
                        ChronoUnit.DAYS.between(
                                dateReference,
                                aujourdHui
                        );


                // ------------------------------------------------
                // Vérifier si les 10 jours sont écoulés
                // ------------------------------------------------

                if (nombreDeJours >= 10) {

                    // --------------------------------------------
                    // Créer la notification de rappel
                    // --------------------------------------------

                    Notification notification =
                            Notification.builder()
                                    .titre(
                                            "Rappel concernant votre démarche"
                                    )
                                    .message(
                                            "N'oubliez pas de commencer ou "
                                                    + "de poursuivre votre démarche : \""
                                                    + titreDemarche
                                                    + "\"."
                                    )
                                    .type(
                                            NotificationType.ECHEANCE
                                    )
                                    .lu(false)
                                    .user(
                                            studentDemarche
                                                    .getStudentProfile()
                                                    .getUser()
                                    )
                                    .build();


                    // Enregistrer la notification.
                    notificationService.saveNotification(
                            notification
                    );


                    // Enregistrer la date du rappel.
                    studentDemarche.setDernierRappel(
                            aujourdHui
                    );

                    studentDemarcheRepository.save(
                            studentDemarche
                    );
                }
            }


            // ====================================================
            // RAPPEL N°2 : 7 JOURS AVANT LA DATE LIMITE
            // ====================================================

            /*
             * On récupère la démarche générale afin d'accéder
             * à sa date limite.
             */
            if (studentDemarche.getDemarche() == null) {
                continue;
            }

            LocalDate dateLimite =
                    studentDemarche
                            .getDemarche()
                            .getDateLimite();


            // ----------------------------------------------------
            // Vérifier qu'une date limite existe
            // ----------------------------------------------------

            if (dateLimite == null) {
                continue;
            }


            /*
             * Calcul du nombre de jours restant avant
             * la date limite.
             *
             * Exemple :
             *
             * Aujourd'hui : 21 septembre
             * Date limite : 28 septembre
             *
             * Résultat : 7 jours.
             */
            long joursRestants =
                    ChronoUnit.DAYS.between(
                            aujourdHui,
                            dateLimite
                    );


            // ----------------------------------------------------
            // Vérifier si nous sommes exactement à J-7
            // ----------------------------------------------------

            if (joursRestants == 7) {

                /*
                 * Message exact qui sera utilisé pour rechercher
                 * une notification identique déjà existante.
                 *
                 * Cela évite de créer plusieurs fois le même
                 * rappel J-7.
                 */
                String messageRappel =
                        "Attention, votre démarche \""
                                + titreDemarche
                                + "\" arrive à échéance dans 7 jours.";


                // ------------------------------------------------
                // Vérifier si le rappel existe déjà
                // ------------------------------------------------

                boolean rappelDejaEnvoye =
                        notificationService
                                .existsNotification(
                                        studentDemarche
                                                .getStudentProfile()
                                                .getUser()
                                                .getId(),
                                        NotificationType.ECHEANCE,
                                        messageRappel
                                );


                // ------------------------------------------------
                // Créer la notification uniquement si elle
                // n'existe pas encore
                // ------------------------------------------------

                if (!rappelDejaEnvoye) {

                    Notification notification =
                            Notification.builder()
                                    .titre(
                                            "Échéance proche"
                                    )
                                    .message(
                                            messageRappel
                                    )
                                    .type(
                                            NotificationType.ECHEANCE
                                    )
                                    .lu(false)
                                    .user(
                                            studentDemarche
                                                    .getStudentProfile()
                                                    .getUser()
                                    )
                                    .build();


                    // Enregistrer la notification J-7.
                    notificationService.saveNotification(
                            notification
                    );
                }
            }

            /**
 * ============================================================
 * RAPPEL J-3 AVANT L'ÉCHÉANCE
 * ============================================================
 *
 * Si la démarche arrive à échéance dans exactement 3 jours,
 * une notification est envoyée à l'étudiant.
 *
 * La vérification existsNotification() permet d'éviter
 * de créer plusieurs fois la même notification.
 */
if (joursRestants == 3) {

    // Vérifie que le profil étudiant existe.
    if (studentDemarche.getStudentProfile() == null) {
        continue;
    }

    // Vérifie que le profil est bien associé à un utilisateur.
    if (studentDemarche.getStudentProfile().getUser() == null) {
        continue;
    }

    /*
     * Utilise le titre de la démarche déjà récupéré
     * précédemment dans la méthode.
     */
    String messageRappel =
            "Attention, votre démarche \""
                    + titreDemarche
                    + "\" arrive à échéance dans 3 jours.";

    /*
     * Vérifie si cette notification existe déjà
     * pour cet utilisateur.
     *
     * Cela évite les doublons si le scheduler est exécuté
     * plusieurs fois le même jour.
     */
    boolean notificationExiste =
            notificationService.existsNotification(
                    studentDemarche
                            .getStudentProfile()
                            .getUser()
                            .getId(),
                    NotificationType.ECHEANCE,
                    messageRappel
            );

    // Création de la notification uniquement si elle n'existe pas.
    if (!notificationExiste) {

        Notification notification =
                Notification.builder()
                        .titre("Échéance dans 3 jours")
                        .message(messageRappel)
                        .type(NotificationType.ECHEANCE)
                        .lu(false)
                        .user(
                                studentDemarche
                                        .getStudentProfile()
                                        .getUser()
                        )
                        .build();

        notificationService.saveNotification(notification);
    }
}
        }
    }
}