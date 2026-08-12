package com.mystudentguide.enums;

/**
 * Définit les différents types de logement
 * pouvant être renseignés dans le profil étudiant.
 */
public enum LogementType {

    /**
     * Logement universitaire, par exemple une résidence CROUS.
     */
    CROUS,

    /**
     * Logement loué dans le secteur privé.
     */
    LOCATION_PRIVEE,

    /**
     * Étudiant hébergé gratuitement chez une personne.
     */
    HEBERGEMENT_CHEZ_UN_PROCHE,

    /**
     * Étudiant hébergé chez un membre de sa famille.
     */
    HEBERGEMENT_FAMILLE,

    /**
     * Résidence étudiante privée.
     */
    RESIDENCE_ETUDIANTE,

    /**
     * L'étudiant ne possède pas encore de logement.
     */
    SANS_LOGEMENT,

    /**
     * Autre type de logement.
     */
    AUTRE,

    /**
     * Information non renseignée.
     */
    NON_RENSEIGNE
}