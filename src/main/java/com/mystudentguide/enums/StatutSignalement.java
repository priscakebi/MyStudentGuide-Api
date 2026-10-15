package com.mystudentguide.enums;

/**
 * Traitement d'un signalement par l'administration.
 */
public enum StatutSignalement {

    /** En attente d'examen. */
    OUVERT,

    /** La démarche a été corrigée : l'étudiant est remercié. */
    TRAITE,

    /** Le signalement n'a pas donné lieu à une correction. */
    REJETE
}
