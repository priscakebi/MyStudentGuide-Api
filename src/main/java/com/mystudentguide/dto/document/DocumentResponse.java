package com.mystudentguide.dto.document;

import com.mystudentguide.enums.DocumentType;

/**
 * DTO utilisé pour retourner les informations d'un document
 * au frontend.
 *
 * On ne retourne pas directement l'entité Document afin
 * de contrôler les informations exposées par l'API.
 *
 * Le contenu réel du fichier n'est pas inclus dans ce DTO.
 */
public class DocumentResponse {

    /**
     * Identifiant unique du document.
     */
    private Long id;

    /**
     * Nom original du fichier envoyé par l'étudiant.
     *
     * Exemple :
     * "justificatif_domicile.pdf"
     */
    private String nomOriginal;

    /**
     * Nom utilisé par le serveur pour stocker le fichier.
     *
     * Ce nom peut être différent du nom original afin
     * d'éviter les conflits entre fichiers.
     */
    private String nomStockage;

    /**
     * Type du document.
     *
     * Exemple :
     * JUSTIFICATIF_DOMICILE
     * PIECE_IDENTITE
     * CERTIFICAT_SCOLARITE
     */
    private DocumentType type;

    /**
     * Taille du fichier en octets.
     */
    private Long taille;

    /**
     * Chemin ou URL permettant d'accéder au fichier.
     *
     * Selon la solution de stockage choisie, cette valeur
     * pourra être un chemin local ou une URL.
     */
    private String chemin;

    /**
     * Identifiant de la démarche à laquelle
     * le document est associé.
     */
    private Long demarcheId;

    /**
     * Constructeur vide nécessaire à Spring.
     */
    public DocumentResponse() {
    }

    /**
     * Constructeur complet.
     */
    public DocumentResponse(
            Long id,
            String nomOriginal,
            String nomStockage,
            DocumentType type,
            Long taille,
            String chemin,
            Long demarcheId
    ) {
        this.id = id;
        this.nomOriginal = nomOriginal;
        this.nomStockage = nomStockage;
        this.type = type;
        this.taille = taille;
        this.chemin = chemin;
        this.demarcheId = demarcheId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomOriginal() {
        return nomOriginal;
    }

    public void setNomOriginal(String nomOriginal) {
        this.nomOriginal = nomOriginal;
    }

    public String getNomStockage() {
        return nomStockage;
    }

    public void setNomStockage(String nomStockage) {
        this.nomStockage = nomStockage;
    }

    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

    public Long getTaille() {
        return taille;
    }

    public void setTaille(Long taille) {
        this.taille = taille;
    }

    public String getChemin() {
        return chemin;
    }

    public void setChemin(String chemin) {
        this.chemin = chemin;
    }

    public Long getDemarcheId() {
        return demarcheId;
    }

    public void setDemarcheId(Long demarcheId) {
        this.demarcheId = demarcheId;
    }
}