package com.mystudentguide.dto.notification;

/**
 * DTO utilisé pour retourner une notification au frontend.
 *
 * Ce DTO permet de contrôler les informations envoyées
 * à Angular sans exposer directement l'entité Notification
 * et ses relations JPA.
 */
public class NotificationResponse {

    /**
     * Identifiant unique de la notification.
     */
    private Long id;

    /**
     * Titre de la notification.
     *
     * Exemple :
     * "Nouvelle démarche disponible"
     */
    private String titre;

    /**
     * Contenu détaillé de la notification.
     */
    private String message;

    /**
     * Indique si la notification a été lue.
     *
     * false = notification non lue
     * true  = notification déjà lue
     */
    private boolean lu;

    /**
     * Date de création de la notification.
     *
     * On la garde sous forme de String pour rester
     * compatible avec l'entité actuelle.
     */
    private String dateCreation;

    /**
     * Identifiant de l'utilisateur destinataire.
     *
     * On retourne uniquement l'identifiant et non
     * l'objet User complet.
     */
    private Long userId;

    /**
     * Constructeur vide nécessaire à Spring.
     */
    public NotificationResponse() {
    }

    /**
     * Constructeur complet.
     *
     * @param id identifiant de la notification
     * @param titre titre
     * @param message contenu de la notification
     * @param lu statut de lecture
     * @param dateCreation date de création
     * @param userId identifiant du destinataire
     */
    public NotificationResponse(
            Long id,
            String titre,
            String message,
            boolean lu,
            String dateCreation,
            Long userId
    ) {
        this.id = id;
        this.titre = titre;
        this.message = message;
        this.lu = lu;
        this.dateCreation = dateCreation;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isLu() {
        return lu;
    }

    public void setLu(boolean lu) {
        this.lu = lu;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}