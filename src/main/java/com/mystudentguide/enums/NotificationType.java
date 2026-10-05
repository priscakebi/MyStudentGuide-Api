package com.mystudentguide.enums;

/**
 * Définit les différents types de notifications
 * pouvant être envoyées à un utilisateur.
 */
public enum NotificationType {

    /**
     * Notification concernant une échéance.
     *
     * Exemple :
     * "Votre titre de séjour arrive à expiration
     * dans 30 jours."
     */
    ECHEANCE,

    /**
     * Notification concernant une démarche.
     *
     * Exemple :
     * "Une nouvelle démarche vous a été attribuée."
     */
    DEMARCHE,

    /**
     * Notification contenant une information
     * générale ou administrative.
     */
    INFORMATION,

    /**
     * Notification générée par le système.
     *
     * Exemple :
     * "Votre profil a été complété avec succès."
     */
    SYSTEME
}