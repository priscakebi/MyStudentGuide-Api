package com.mystudentguide.dto.demarche;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO utilisé pour créer ou modifier une démarche.
 *
 * Une démarche est une information générale proposée par
 * l'administrateur aux étudiants.
 *
 * Le statut de suivi n'est pas présent ici.
 * Il est géré par StudentDemarche, car le statut dépend
 * de chaque étudiant.
 */
public class DemarcheRequest {

    /**
     * Titre de la démarche.
     */
    @NotBlank(message = "Le titre de la démarche est obligatoire")
    private String titre;

    /**
     * Description détaillée de la démarche.
     */
    @NotBlank(message = "La description de la démarche est obligatoire")
    private String description;

    /**
     * Identifiant de la catégorie à laquelle appartient
     * la démarche.
     */
    @NotNull(message = "La catégorie est obligatoire")
    private Long categorieId;

    /**
     * Date limite éventuelle pour réaliser la démarche.
     */
    private LocalDate dateLimite;

    /**
     * Constructeur vide nécessaire pour Jackson
     * lors de la réception du JSON.
     */
    public DemarcheRequest() {
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(Long categorieId) {
        this.categorieId = categorieId;
    }

    public LocalDate getDateLimite() {
        return dateLimite;
    }

    public void setDateLimite(LocalDate dateLimite) {
        this.dateLimite = dateLimite;
    }
}