package com.mystudentguide.enums;

/**
 * Sujet d'une demande d'aide envoyée par un étudiant
 * (page « Besoin d'aide ? » de son espace).
 */
public enum SujetAide {

    /** Question sur une démarche (étapes, délais, documents). */
    DEMARCHE("Une démarche"),

    /** Question sur un document à fournir. */
    DOCUMENT("Un document"),

    /** Connexion, profil, mot de passe. */
    COMPTE("Mon compte"),

    /** Bug, page qui ne s'affiche pas. */
    TECHNIQUE("Problème technique"),

    AUTRE("Autre");

    private final String libelle;

    SujetAide(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
