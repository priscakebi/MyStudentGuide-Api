package com.mystudentguide.dto.account;

/**
 * DTO utilisé pour recevoir la demande de suppression
 * d'un compte depuis le frontend.
 *
 * Ce DTO contient uniquement les informations nécessaires
 * pour traiter la demande de suppression.
 *
 * Il ne s'agit pas encore de supprimer directement le compte.
 * Le service backend vérifiera ensuite le mot de passe,
 * enregistrera le motif et effectuera les actions nécessaires.
 */
public class DeleteAccountRequest {

    /**
     * Motif choisi par l'étudiant pour expliquer
     * pourquoi il souhaite supprimer son compte.
     *
     * Exemple :
     * "JE_N_UTILISE_PLUS_L_APPLICATION"
     */
    private String deletionReason;

    /**
     * Précisions supplémentaires données par l'étudiant.
     *
     * Ce champ est facultatif.
     */
    private String deletionDetails;

    /**
     * Indique si l'étudiant a coché la case
     * de confirmation proposée dans le formulaire.
     *
     * Cette confirmation est facultative dans notre formulaire.
     */
    private boolean confirmation;

    /**
     * Mot de passe actuel du compte.
     *
     * Il sera vérifié côté backend avant d'autoriser
     * la suppression du compte.
     */
    private String deletionPassword;

    /**
     * Constructeur vide nécessaire à Spring
     * pour désérialiser la requête JSON reçue.
     */
    public DeleteAccountRequest() {
    }

    /**
     * Constructeur complet.
     *
     * @param deletionReason motif de suppression
     * @param deletionDetails détails supplémentaires
     * @param confirmation confirmation de l'utilisateur
     * @param deletionPassword mot de passe du compte
     */
    public DeleteAccountRequest(
            String deletionReason,
            String deletionDetails,
            boolean confirmation,
            String deletionPassword
    ) {
        this.deletionReason = deletionReason;
        this.deletionDetails = deletionDetails;
        this.confirmation = confirmation;
        this.deletionPassword = deletionPassword;
    }

    /**
     * Retourne le motif choisi.
     */
    public String getDeletionReason() {
        return deletionReason;
    }

    /**
     * Définit le motif choisi.
     */
    public void setDeletionReason(String deletionReason) {
        this.deletionReason = deletionReason;
    }

    /**
     * Retourne les détails supplémentaires.
     */
    public String getDeletionDetails() {
        return deletionDetails;
    }

    /**
     * Définit les détails supplémentaires.
     */
    public void setDeletionDetails(String deletionDetails) {
        this.deletionDetails = deletionDetails;
    }

    /**
     * Retourne l'état de la confirmation.
     */
    public boolean isConfirmation() {
        return confirmation;
    }

    /**
     * Définit l'état de la confirmation.
     */
    public void setConfirmation(boolean confirmation) {
        this.confirmation = confirmation;
    }

    /**
     * Retourne le mot de passe fourni
     * pour confirmer la suppression.
     */
    public String getDeletionPassword() {
        return deletionPassword;
    }

    /**
     * Définit le mot de passe de suppression.
     */
    public void setDeletionPassword(String deletionPassword) {
        this.deletionPassword = deletionPassword;
    }
}