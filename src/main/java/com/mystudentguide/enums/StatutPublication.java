package com.mystudentguide.enums;

/**
 * État de publication d'une démarche.
 *
 * BROUILLON : importée (ex. depuis Service-Public) ou en préparation,
 *             visible uniquement par les administrateurs.
 * PUBLIEE   : visible par tous.
 *
 * Les démarches créées avant l'ajout de ce statut ont la valeur null
 * en base : elles sont considérées comme publiées.
 */
public enum StatutPublication {
    BROUILLON,
    PUBLIEE
}
