package com.mystudentguide.enums;

/**
 * Enumération représentant les différents types
 * de documents pouvant être déposés dans MyStudentGuide.
 *
 * Chaque document associé à une démarche possède
 * un type permettant de l'identifier facilement.
 */
public enum DocumentType {

    /**
     * Pièce d'identité de l'étudiant.
     */
    PIECE_IDENTITE,

    /**
     * Passeport de l'étudiant.
     */
    PASSEPORT,

    /**
     * Visa de l'étudiant.
     */
    VISA,

    /**
     * Titre de séjour.
     */
    TITRE_SEJOUR,

    /**
     * Certificat de scolarité.
     */
    CERTIFICAT_SCOLARITE,

    /**
     * Justificatif de domicile.
     */
    JUSTIFICATIF_DOMICILE,

    /**
     * Contrat de location ou bail.
     */
    CONTRAT_LOCATION,

    /**
     * Attestation d'hébergement.
     */
    ATTESTATION_HEBERGEMENT,

    /**
     * Attestation d'assurance.
     */
    ATTESTATION_ASSURANCE,

    /**
     * Justificatif de ressources financières.
     */
    JUSTIFICATIF_RESSOURCES,

    /**
     * Relevé bancaire.
     */
    RELEVE_BANCAIRE,

    /**
     * Photo d'identité.
     */
    PHOTO_IDENTITE,

    /**
     * Document administratif divers.
     */
    AUTRE
}