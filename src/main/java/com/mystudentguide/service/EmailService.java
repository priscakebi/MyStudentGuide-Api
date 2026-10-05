package com.mystudentguide.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service responsable de l'envoi des emails de MyStudentGuide.
 *
 * Ce service centralise tous les emails automatiques envoyés
 * par l'application.
 *
 * Il ne contient pas la logique métier :
 * les autres services décident QUAND un email doit être envoyé,
 * tandis que ce service s'occupe uniquement de l'envoi.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    /**
     * Composant Spring permettant d'envoyer les emails
     * via le serveur SMTP configuré dans application.properties.
     */
    private final JavaMailSender mailSender;

    /**
     * Constructeur du service.
     *
     * @param mailSender composant permettant d'envoyer les emails
     */
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Méthode générique utilisée par les autres méthodes
     * pour envoyer un email texte.
     *
     * @param destinataire adresse email du destinataire
     * @param sujet sujet de l'email
     * @param contenu contenu de l'email
     */
    private void envoyerEmail(
            String destinataire,
            String sujet,
            String contenu
    ) {
        // Création du message email.
        SimpleMailMessage message = new SimpleMailMessage();

        // Définition du destinataire.
        message.setTo(destinataire);

        // Définition du sujet.
        message.setSubject(sujet);

        // Définition du contenu du message.
        message.setText(contenu);

        // Envoi de l'email via Gmail SMTP.
        // Une panne SMTP ne doit jamais faire échouer l'action de l'utilisateur
        // (inscription, création de démarche...) : on journalise l'erreur.
        try {
            mailSender.send(message);
        } catch (Exception exception) {
            log.warn("Échec d'envoi de l'email '{}' : {}", sujet, exception.getMessage());
        }
    }

    // ==========================================================
    // EMAILS DESTINÉS AUX ADMINISTRATEURS
    // ==========================================================

    /**
     * Informe l'administrateur qu'un nouvel étudiant
     * vient de créer un compte.
     *
     * @param adminEmail adresse email de l'administrateur
     * @param prenom prénom de l'étudiant
     * @param nom nom de l'étudiant
     * @param email email de l'étudiant
     */
    @Async
    public void envoyerNouvelleInscriptionAdmin(
            String adminEmail,
            String prenom,
            String nom,
            String email
    ) {
        String sujet = "Nouvelle inscription sur MyStudentGuide";

        String contenu =
                "Bonjour,\n\n"
                + "Un nouvel étudiant vient de créer un compte "
                + "sur MyStudentGuide.\n\n"
                + "Nom : " + nom + "\n"
                + "Prénom : " + prenom + "\n"
                + "Email : " + email + "\n\n"
                + "Vous pouvez consulter son compte depuis "
                + "le tableau de bord administrateur.\n\n"
                + "MyStudentGuide";

        envoyerEmail(adminEmail, sujet, contenu);
    }

    /**
     * Informe l'administrateur qu'un étudiant vient de créer
     * son profil.
     *
     * @param adminEmail adresse email de l'administrateur
     * @param prenom prénom de l'étudiant
     * @param nom nom de l'étudiant
     * @param email email de l'étudiant
     */
    @Async
    public void envoyerCreationProfilAdmin(
            String adminEmail,
            String prenom,
            String nom,
            String email
    ) {
        String sujet = "Nouveau profil étudiant";

        String contenu =
                "Bonjour,\n\n"
                + "L'étudiant suivant vient de créer son profil "
                + "sur MyStudentGuide :\n\n"
                + "Nom : " + nom + "\n"
                + "Prénom : " + prenom + "\n"
                + "Email : " + email + "\n\n"
                + "Le profil peut maintenant être consulté "
                + "depuis le tableau de bord administrateur.\n\n"
                + "MyStudentGuide";

        envoyerEmail(adminEmail, sujet, contenu);
    }

    /**
     * Informe l'administrateur qu'un étudiant a modifié
     * son profil.
     *
     * @param adminEmail adresse email de l'administrateur
     * @param prenom prénom de l'étudiant
     * @param nom nom de l'étudiant
     * @param email email de l'étudiant
     */
    @Async
    public void envoyerModificationProfilAdmin(
            String adminEmail,
            String prenom,
            String nom,
            String email
    ) {
        String sujet = "Profil étudiant modifié";

        String contenu =
                "Bonjour,\n\n"
                + "L'étudiant suivant vient de modifier son profil "
                + "sur MyStudentGuide :\n\n"
                + "Nom : " + nom + "\n"
                + "Prénom : " + prenom + "\n"
                + "Email : " + email + "\n\n"
                + "Vous pouvez consulter les informations mises à jour "
                + "depuis le tableau de bord administrateur.\n\n"
                + "MyStudentGuide";

        envoyerEmail(adminEmail, sujet, contenu);
    }

    /**
     * Informe l'administrateur qu'une démarche attribuée
     * à un étudiant vient de changer de statut.
     *
     * @param adminEmail adresse email de l'administrateur
     * @param prenom prénom de l'étudiant
     * @param nom nom de l'étudiant
     * @param titreDemarche titre de la démarche
     * @param ancienStatut ancien statut de la démarche
     * @param nouveauStatut nouveau statut de la démarche
     */
    @Async
    public void envoyerChangementStatutAdmin(
            String adminEmail,
            String prenom,
            String nom,
            String titreDemarche,
            String ancienStatut,
            String nouveauStatut
    ) {
        String sujet = "Changement de statut d'une démarche";

        String contenu =
                "Bonjour,\n\n"
                + "Le statut d'une démarche a été modifié "
                + "pour l'étudiant suivant :\n\n"
                + "Nom : " + nom + "\n"
                + "Prénom : " + prenom + "\n\n"
                + "Démarche : " + titreDemarche + "\n"
                + "Ancien statut : " + ancienStatut + "\n"
                + "Nouveau statut : " + nouveauStatut + "\n\n"
                + "MyStudentGuide";

        envoyerEmail(adminEmail, sujet, contenu);
    }

    /**
     * Informe l'administrateur qu'un étudiant lui a envoyé
     * un message depuis le formulaire de contact.
     *
     * @param adminEmail adresse email de l'administrateur
     * @param nom nom de l'utilisateur
     * @param email email de l'utilisateur
     * @param sujetMessage sujet du message
     * @param message message envoyé
     */
    @Async
    public void envoyerMessageContactAdmin(
            String adminEmail,
            String nom,
            String email,
            String sujetMessage,
            String message
    ) {
        String sujet = "Nouveau message depuis MyStudentGuide";

        String contenu =
                "Bonjour,\n\n"
                + "Un utilisateur vient de vous envoyer un message "
                + "depuis le formulaire de contact.\n\n"
                + "Nom : " + nom + "\n"
                + "Email : " + email + "\n"
                + "Sujet : " + sujetMessage + "\n\n"
                + "Message :\n"
                + message
                + "\n\n"
                + "MyStudentGuide";

        envoyerEmail(adminEmail, sujet, contenu);
    }

    // ==========================================================
    // EMAILS DESTINÉS AUX ÉTUDIANTS
    // ==========================================================

    /**
     * Envoie à l'étudiant une confirmation après la création
     * de son compte.
     *
     * @param email email de l'étudiant
     * @param prenom prénom de l'étudiant
     */
    @Async
    public void envoyerConfirmationInscription(
            String email,
            String prenom
    ) {
        String sujet = "Bienvenue sur MyStudentGuide";

        String contenu =
                "Bonjour " + prenom + ",\n\n"
                + "Votre compte MyStudentGuide a bien été créé.\n\n"
                + "Vous pouvez maintenant vous connecter à votre "
                + "espace étudiant et compléter votre profil.\n\n"
                + "Bienvenue sur MyStudentGuide !\n\n"
                + "L'équipe MyStudentGuide";

        envoyerEmail(email, sujet, contenu);
    }

    /**
     * Confirme à l'étudiant que son mot de passe vient
     * d'être modifié.
     *
     * @param email email de l'étudiant
     * @param prenom prénom de l'étudiant
     */
    @Async
    public void envoyerConfirmationModificationMotDePasse(
            String email,
            String prenom
    ) {
        String sujet = "Votre mot de passe a été modifié";

        String contenu =
                "Bonjour " + prenom + ",\n\n"
                + "Votre mot de passe MyStudentGuide vient d'être "
                + "modifié avec succès.\n\n"
                + "Si vous n'êtes pas à l'origine de cette modification, "
                + "contactez immédiatement l'administration.\n\n"
                + "L'équipe MyStudentGuide";

        envoyerEmail(email, sujet, contenu);
    }

    /**
     * Informe l'étudiant qu'une nouvelle démarche lui a été attribuée
     * par un administrateur.
     *
     * @param email email de l'étudiant
     * @param prenom prénom de l'étudiant
     * @param titreDemarche titre de la démarche
     * @param dateLimite date limite éventuelle de la démarche
     */
    @Async
    public void envoyerNouvelleDemarche(
            String email,
            String prenom,
            String titreDemarche,
            String dateLimite
    ) {
        String sujet = "Une nouvelle démarche vous a été attribuée";

        String contenu =
                "Bonjour " + prenom + ",\n\n"
                + "Une nouvelle démarche vient de vous être attribuée "
                + "sur MyStudentGuide.\n\n"
                + "Démarche : " + titreDemarche + "\n";

        // On ajoute la date limite uniquement lorsqu'elle existe.
        if (dateLimite != null && !dateLimite.isBlank()) {
            contenu += "Date limite : " + dateLimite + "\n";
        }

        contenu +=
                "\n"
                + "Connectez-vous à votre espace étudiant pour "
                + "consulter les détails de cette démarche.\n\n"
                + "L'équipe MyStudentGuide";

        envoyerEmail(email, sujet, contenu);
    }

    // ==========================================================
    // EMAILS LIÉS À LA SUPPRESSION D'UN COMPTE
    // ==========================================================

    /**
     * Informe l'administrateur qu'un étudiant a supprimé
     * son compte MyStudentGuide.
     *
     * Le motif et les éventuelles précisions fournies
     * par l'étudiant sont inclus dans l'email afin que
     * l'administrateur puisse comprendre la raison de la suppression.
     *
     * @param adminEmail adresse email de l'administrateur
     * @param prenom prénom de l'étudiant
     * @param nom nom de l'étudiant
     * @param email email du compte supprimé
     * @param motif motif choisi lors de la suppression
     * @param details précisions supplémentaires éventuelles
     */
    @Async
    public void envoyerSuppressionCompteAdmin(
            String adminEmail,
            String prenom,
            String nom,
            String email,
            String motif,
            String details
    ) {
        // Sujet de l'email envoyé à l'administrateur.
        String sujet = "Suppression d'un compte étudiant";

        // Construction du contenu de l'email.
        String contenu =
                "Bonjour,\n\n"
                + "Un étudiant vient de supprimer son compte "
                + "MyStudentGuide.\n\n"
                + "Informations du compte supprimé :\n"
                + "Nom : " + nom + "\n"
                + "Prénom : " + prenom + "\n"
                + "Email : " + email + "\n\n"
                + "Motif de suppression :\n"
                + motif + "\n";

        // Les détails sont ajoutés uniquement
        // lorsqu'ils ont été renseignés.
        if (details != null && !details.isBlank()) {
            contenu +=
                    "\n"
                    + "Précisions fournies par l'étudiant :\n"
                    + details + "\n";
        }

        contenu +=
                "\n"
                + "Le compte et les données associées ont été supprimés "
                + "conformément à la demande de l'étudiant.\n\n"
                + "MyStudentGuide";

        // Envoi de l'email à l'administrateur.
        envoyerEmail(adminEmail, sujet, contenu);
    }

    /**
     * Informe l'étudiant que son compte vient d'être supprimé.
     *
     * Cet email est envoyé après le traitement réussi
     * de la suppression du compte.
     *
     * @param email adresse email de l'ancien compte
     * @param prenom prénom de l'étudiant
     */
    @Async
    public void envoyerConfirmationSuppressionCompte(
            String email,
            String prenom
    ) {
        // Sujet de l'email envoyé à l'étudiant.
        String sujet = "Votre compte MyStudentGuide a été supprimé";

        // Construction du contenu de l'email.
        String contenu =
                "Bonjour " + prenom + ",\n\n"
                + "Votre compte MyStudentGuide a bien été supprimé "
                + "conformément à votre demande.\n\n"
                + "Vos informations associées à ce compte ne sont "
                + "plus accessibles depuis l'application.\n\n"
                + "Nous vous remercions d'avoir utilisé MyStudentGuide.\n\n"
                + "L'équipe MyStudentGuide";

        // Envoi de l'email à l'étudiant.
        envoyerEmail(email, sujet, contenu);
    }
}