package com.mystudentguide.dto.categorie;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO utilisé pour créer ou modifier une catégorie
 * de démarches administratives.
 *
 * Exemple de catégories :
 * - Séjour
 * - Logement
 * - Santé
 * - Études
 * - Travail
 * - Finances
 */
public class CategorieRequest {

    /**
     * Nom de la catégorie.
     */
    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(
        max = 100,
        message = "Le nom de la catégorie ne doit pas dépasser 100 caractères"
    )
    private String nom;

    /**
     * Description de la catégorie.
     */
    @Size(
        max = 500,
        message = "La description ne doit pas dépasser 500 caractères"
    )
    private String description;

    /**
     * Constructeur vide nécessaire à Spring
     * pour convertir le JSON reçu en objet Java.
     */
    public CategorieRequest() {
    }

    /**
     * Retourne le nom de la catégorie.
     *
     * @return nom de la catégorie
     */
    public String getNom() {
        return nom;
    }

    /**
     * Définit le nom de la catégorie.
     *
     * @param nom nouveau nom
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Retourne la description de la catégorie.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Définit la description de la catégorie.
     *
     * @param description nouvelle description
     */
    public void setDescription(String description) {
        this.description = description;
    }
}