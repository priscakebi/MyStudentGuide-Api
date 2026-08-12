package com.mystudentguide.enums;

/**
 * Définit les différents états possibles
 * d'une démarche administrative.
 */
public enum DemarcheStatus {

    /**
     * La démarche a été proposée à l'étudiant
     * mais il ne l'a pas encore commencée.
     */
    A_FAIRE,

    /**
     * L'étudiant a commencé la démarche.
     */
    EN_COURS,

    /**
     * L'étudiant a terminé la démarche.
     */
    TERMINEE,

    /**
     * La démarche n'est plus applicable
     * à la situation actuelle de l'étudiant.
     *
     * Exemple :
     * une démarche générée automatiquement mais
     * finalement non nécessaire.
     */
    NON_CONCERNEE
}