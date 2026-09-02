package com.mystudentguide.dto.demarche;

import com.mystudentguide.enums.DemarcheStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO utilisé pour créer ou modifier une démarche.
 */
public class DemarcheRequest {

    @NotBlank(message = "Le titre de la démarche est obligatoire")
    private String titre;

    @NotBlank(message = "La description de la démarche est obligatoire")
    private String description;

    private DemarcheStatus statut;

    @NotNull(message = "La catégorie est obligatoire")
    private Long categorieId;

    @NotNull(message = "Le profil étudiant est obligatoire")
    private Long studentProfileId;

    private LocalDate dateLimite;

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

    public Long getStudentProfileId() {
        return studentProfileId;
    }

    public void setStudentProfileId(Long studentProfileId) {
        this.studentProfileId = studentProfileId;
    }

    public LocalDate getDateLimite() {
        return dateLimite;
    }

    public void setDateLimite(LocalDate dateLimite) {
        this.dateLimite = dateLimite;
    }
}