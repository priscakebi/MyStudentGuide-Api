package com.mystudentguide.dto.categorie;

/**
 * DTO utilisé pour retourner une catégorie au frontend.
 *
 * On ne retourne pas directement l'entité Categorie
 * afin de contrôler les informations exposées par l'API.
 */
public class CategorieResponse {

    /**
     * Identifiant unique de la catégorie.
     */
    private Long id;

    /**
     * Nom de la catégorie.
     */
    private String nom;

    /**
     * Description de la catégorie.
     */
    private String description;

    /**
     * Nombre de démarches associées à cette catégorie.
     *
     * Cette information est pratique pour le frontend
     * lorsque l'on souhaite afficher le nombre de démarches
     * disponibles dans chaque catégorie.
     */
    private long nombreDemarches;

    /**
     * Constructeur vide.
     */
    public CategorieResponse() {
    }

    /**
     * Constructeur complet.
     *
     * @param id identifiant de la catégorie
     * @param nom nom de la catégorie
     * @param description description de la catégorie
     * @param nombreDemarches nombre de démarches associées
     */
    public CategorieResponse(
            Long id,
            String nom,
            String description,
            long nombreDemarches
    ) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.nombreDemarches = nombreDemarches;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getNombreDemarches() {
        return nombreDemarches;
    }

    public void setNombreDemarches(long nombreDemarches) {
        this.nombreDemarches = nombreDemarches;
    }
}