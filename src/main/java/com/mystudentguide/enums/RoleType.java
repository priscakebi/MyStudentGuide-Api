package com.mystudentguide.enums;

/**
 * Définit les différents rôles disponibles
 * dans l'application MyStudentGuide.
 */
public enum RoleType {

    /**
     * Utilisateur étudiant.
     *
     * Il peut :
     * - gérer son profil ;
     * - consulter ses démarches ;
     * - suivre leur avancement ;
     * - recevoir des notifications ;
     * - gérer ses documents.
     */
    ETUDIANT,

    /**
     * Administrateur de l'application.
     *
     * Il peut notamment :
     * - gérer les utilisateurs ;
     * - gérer les catégories ;
     * - gérer les démarches ;
     * - administrer le contenu de l'application.
     */
    ADMIN
}