package com.mystudentguide.dto.demarche;

import com.mystudentguide.enums.DemarcheStatus;

/**
 * DTO utilisé pour retourner une démarche au frontend.
 *
 * On retourne les informations nécessaires à Angular
 * sans exposer directement les relations JPA.
 */
public class DemarcheResponse {

    /**
     * Identifiant de la démarche.
     */
    private Long id;

    /**
     * Nom de la démarche.
     */
    private String nom;

    /**
     * Description de la démarche.
     */
    private String description;

    /**
     * Statut actuel de la démarche.
     */
    private DemarcheStatus statut;

    /**
     * Identifiant de la catégorie.
     */
    private Long categorieId;

    /**
     * Nom de la catégorie.
     *
     * Pratique pour éviter qu'Angular ait besoin
     * de faire une requête supplémentaire uniquement
     * pour afficher le nom de la catégorie.
     */
    private String categorieNom;

    /**
     * Identifiant du profil étudiant concerné.
     */
    private Long studentProfileId;

    /**
     * Constructeur vide.
     */
    public DemarcheResponse() {
    }

    /**
     * Constructeur complet.
     */
    public DemarcheResponse(
            Long id,
            String nom,
            String description,
            DemarcheStatus statut,
            Long categorieId,
            String categorieNom,
            Long studentProfileId
    ) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.statut = statut;
        this.categorieId = categorieId;
        this.categorieNom = categorieNom;
        this.studentProfileId = studentProfileId;
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

    public DemarcheStatus getStatut() {
        return statut;
    }

    public void setStatut(DemarcheStatus statut) {
        this.statut = statut;
    }

    public Long getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(Long categorieId) {
        this.categorieId = categorieId;
    }

    public String getCategorieNom() {
        return categorieNom;
    }

    public void setCategorieNom(String categorieNom) {
        this.categorieNom = categorieNom;
    }

    public Long getStudentProfileId() {
        return studentProfileId;
    }

    public void setStudentProfileId(Long studentProfileId) {
        this.studentProfileId = studentProfileId;
    }
}