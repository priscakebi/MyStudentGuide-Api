package com.mystudentguide.enums;

/**
 * Enumération représentant les grandes catégories de documents
 * pouvant être nécessaires pour réaliser une démarche administrative.
 *
 * Les catégories sont volontairement générales afin de simplifier
 * la lecture des documents requis par l'étudiant.
 *
 * L'application ne stocke pas les fichiers personnels de l'étudiant.
 * Ces types servent uniquement à catégoriser les pièces demandées.
 */
public enum DocumentType {

    /**
     * Documents permettant de justifier l'identité ou
     * le droit au séjour de l'étudiant.
     *
     * Exemples :
     * - Passeport
     * - Carte d'identité
     * - Visa
     * - Titre de séjour
     */
    IDENTITE,

    /**
     * Documents permettant de justifier la situation scolaire
     * ou le parcours universitaire de l'étudiant.
     *
     * Exemples :
     * - Certificat de scolarité
     * - Attestation d'inscription
     * - Relevés de notes
     * - Diplôme
     * - Attestation de réussite
     */
    SCOLARITE,

    /**
     * Documents permettant de justifier le logement
     * ou l'adresse de résidence de l'étudiant.
     *
     * Exemples :
     * - Justificatif de domicile
     * - Contrat de location
     * - Attestation d'hébergement
     */
    DOMICILE,

    /**
     * Documents permettant de justifier les ressources financières
     * de l'étudiant.
     *
     * Exemples :
     * - Justificatifs de ressources
     * - Relevés bancaires
     */
    RESSOURCES,

    /**
     * Documents ou photographies nécessaires pour identifier
     * physiquement l'étudiant.
     *
     * Exemples :
     * - Photo d'identité
     * - E-photo
     */
    PHOTO,

    /**
     * Documents permettant de justifier la couverture
     * ou la souscription à une assurance.
     *
     * Exemple :
     * - Attestation d'assurance
     */
    ASSURANCE,

    /**
     * Document administratif ne correspondant pas
     * aux catégories précédentes.
     */
    AUTRE
}