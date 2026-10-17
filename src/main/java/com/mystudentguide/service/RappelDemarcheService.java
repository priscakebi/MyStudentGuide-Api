package com.mystudentguide.service;

import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.StudentDemarche;
import com.mystudentguide.enums.DemarcheStatus;
import com.mystudentguide.enums.NotificationType;
import com.mystudentguide.repository.StudentDemarcheRepository;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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
 * Une tâche planifiée est exécutée automatiquement toutes
 * les heures, ainsi qu'au démarrage de l'API.
 *
 * Elle recherche les démarches EN_COURS, c'est-à-dire
 * celles que l'étudiant a commencées. Les anciens suivis
 * A_FAIRE (ancienne attribution) ne sont plus relancés :
 * l'étudiant ne les voit plus dans son espace.
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

    /** E-mail doublant les rappels d'échéance (7 et 3 jours avant la date limite). */
    private final EmailService emailService;

    /** Adresse du site, pour le lien vers la démarche dans l'e-mail. */
    private final String frontendUrl;


    /**
     * Constructeur du service.
     *
     * @param studentDemarcheRepository repository des démarches étudiants
     * @param notificationService service de gestion des notifications
     */
    public RappelDemarcheService(
            StudentDemarcheRepository studentDemarcheRepository,
            NotificationService notificationService,
            EmailService emailService,
            @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl
    ) {
        this.studentDemarcheRepository = studentDemarcheRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
    }

    /**
     * Double le rappel d'échéance par un e-mail : l'étudiant est prévenu
     * même s'il n'ouvre pas l'application. Appelé une seule fois par rappel
     * (le contrôle existsNotification évite les doublons).
     */
    private void envoyerEmailEcheance(StudentDemarche studentDemarche, String titreDemarche,
                                      long joursRestants, LocalDate dateLimite) {
        var user = studentDemarche.getStudentProfile().getUser();
        // Adresse non confirmée : la notification dans l'application suffit
        // (une adresse mal saisie recevrait les rappels de quelqu'un d'autre).
        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            return;
        }
        String lien = frontendUrl + "/demarches/" + studentDemarche.getDemarche().getId();
        emailService.envoyerRappelEcheance(
                user.getEmail(), user.getPrenom(), titreDemarche, joursRestants, dateLimite, lien);
    }


    // ============================================================
    // RAPPELS AUTOMATIQUES
    // ============================================================

    /**
     * Vérifie les démarches nécessitant un rappel.
     *
     * L'expression cron signifie :
     *
     * seconde = 0
     * minute = 0
     * heure = toutes
     *
     * La vérification tourne toutes les heures ET au démarrage de l'API.
     *
     * Pourquoi ? Sur l'offre gratuite de Render, l'API se met en veille
     * après quelques minutes sans visite : une tâche fixée à 09h00 serait
     * souvent manquée. Les rappels ne sont pas envoyés en double :
     * - rappel de 10 jours : protégé par la date du dernier rappel ;
     * - échéances 7 j / 3 j : protégées par existsNotification(...).
     */
    @Scheduled(cron = "0 0 * * * *")
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void envoyerRappelsAutomatiques() {

        // --------------------------------------------------------
        // 1. Récupérer les démarches commencées (EN_COURS)
        // --------------------------------------------------------

        List<StudentDemarche> demarches =
                studentDemarcheRepository.findByStatut(
                        DemarcheStatus.EN_COURS
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
            // ====================================================
            // RAPPEL N°1 : TOUS LES 10 JOURS
            // ====================================================

            if (studentDemarche.getStatut() == DemarcheStatus.EN_COURS) {

                /*
                 * Démarche EN COURS : 10 jours après le clic sur
                 * « Commencer » (ou après la dernière réponse
                 * « Pas encore »), on demande à l'étudiant s'il a
                 * envoyé ses documents sur la plateforme dédiée.
                 *
                 * La notification porte l'action CONFIRMER_DOCUMENTS :
                 * le front affiche alors les boutons « Oui » / « Pas encore ».
                 */
                envoyerQuestionDocuments(
                        studentDemarche,
                        titreDemarche,
                        aujourdHui
                );

            } else {

                /*
                 * Démarche À FAIRE : simple rappel tous les 10 jours
                 * (date du dernier rappel, sinon date d'attribution).
                 */
                LocalDate dateReference =
                        studentDemarche.getDernierRappel();

                if (dateReference == null) {

                    dateReference =
                            studentDemarche.getDateAttribution();
                }

                if (dateReference != null
                        && ChronoUnit.DAYS.between(dateReference, aujourdHui) >= 10) {

                    Notification notification =
                            Notification.builder()
                                    .titre("Rappel concernant votre démarche")
                                    .message(
                                            "N'oubliez pas de commencer votre démarche : \""
                                                    + titreDemarche
                                                    + "\"."
                                    )
                                    .type(NotificationType.ECHEANCE)
                                    .lu(false)
                                    .studentDemarcheId(studentDemarche.getId())
                                    .user(
                                            studentDemarche
                                                    .getStudentProfile()
                                                    .getUser()
                                    )
                                    .build();

                    notificationService.saveNotification(notification);

                    studentDemarche.setDernierRappel(aujourdHui);

                    studentDemarcheRepository.save(studentDemarche);
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

            // Date limite de CET étudiant : calculée depuis son profil si la démarche
            // a une règle (ex. 90 jours après l'arrivée), sinon la date générale.
            LocalDate dateLimite = EcheancePersonnelle.calculer(
                    studentDemarche.getDemarche(),
                    studentDemarche.getStudentProfile()
            );


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

                    envoyerEmailEcheance(studentDemarche, titreDemarche, 7, dateLimite);
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

        envoyerEmailEcheance(studentDemarche, titreDemarche, 3, dateLimite);
    }
}
        }
    }
    // ============================================================
    // QUESTION DE J+10 : « AVEZ-VOUS ENVOYÉ VOS DOCUMENTS ? »
    // ============================================================

    /**
     * Envoie la question sur l'envoi des documents lorsque :
     *
     * - la démarche est EN_COURS ;
     * - les documents ne sont pas encore confirmés ;
     * - 10 jours se sont écoulés depuis la date de début
     *   (ou depuis la dernière réponse « Pas encore ») ;
     * - aucune question identique n'attend déjà une réponse.
     */
    private void envoyerQuestionDocuments(
            StudentDemarche studentDemarche,
            String titreDemarche,
            LocalDate aujourdHui
    ) {

        if (studentDemarche.isDocumentsEnvoyes()) {
            return;
        }

        LocalDate dateReference = studentDemarche.getDernierRappel();

        if (dateReference == null) {
            dateReference = studentDemarche.getDateDebut();
        }

        if (dateReference == null) {
            dateReference = studentDemarche.getDateAttribution();
        }

        if (dateReference == null
                || ChronoUnit.DAYS.between(dateReference, aujourdHui) < 10) {
            return;
        }

        if (notificationService.hasPendingAction(
                studentDemarche.getId(),
                Notification.ACTION_CONFIRMER_DOCUMENTS)) {
            return;
        }

        Notification notification =
                Notification.builder()
                        .titre("Avez-vous envoyé vos documents ?")
                        .message(
                                "Vous avez commencé la démarche \""
                                        + titreDemarche
                                        + "\". Avez-vous envoyé vos documents "
                                        + "sur la plateforme dédiée à cette démarche ?"
                        )
                        .type(NotificationType.DEMARCHE)
                        .lu(false)
                        .studentDemarcheId(studentDemarche.getId())
                        .action(Notification.ACTION_CONFIRMER_DOCUMENTS)
                        .actionTraitee(false)
                        .user(
                                studentDemarche
                                        .getStudentProfile()
                                        .getUser()
                        )
                        .build();

        notificationService.saveNotification(notification);

        studentDemarche.setDernierRappel(aujourdHui);

        studentDemarcheRepository.save(studentDemarche);
    }
}
