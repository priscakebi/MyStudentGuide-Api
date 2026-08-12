package com.mystudentguide.enums;

/**
 * Définit les différents types de visa ou de situation
 * administrative pouvant concerner un étudiant étranger.
 */
public enum VisaType {

    /**
     * Visa de long séjour valant titre de séjour
     * avec la mention étudiant.
     */
    VLS_TS_ETUDIANT,

    /**
     * Visa de court séjour.
     */
    COURT_SEJOUR,

    /**
     * Titre de séjour portant la mention étudiant.
     */
    TITRE_SEJOUR_ETUDIANT,

    /**
     * Autre situation administrative.
     */
    AUTRE,

    /**
     * Situation administrative non renseignée.
     */
    NON_RENSEIGNE
}