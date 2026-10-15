package com.mystudentguide.enums;

/**
 * Suivi d'une demande d'aide par l'administration.
 */
public enum StatutDemandeAide {

    /** En attente d'une réponse. */
    OUVERTE,

    /** L'administrateur a répondu : l'étudiant est prévenu par notification. */
    REPONDUE,

    /** Close sans suite (doublon, demande hors sujet...). */
    FERMEE
}
