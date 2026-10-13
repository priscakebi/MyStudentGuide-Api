package com.mystudentguide.dto.avis;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * POST /api/avis
 *
 * { "studentDemarcheId": 12, "note": 5, "commentaire": "..." }
 */
public class AvisRequest {

    @NotNull(message = "La démarche est obligatoire")
    private Long studentDemarcheId;

    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note doit être comprise entre 1 et 5")
    @Max(value = 5, message = "La note doit être comprise entre 1 et 5")
    private Integer note;

    @NotBlank(message = "Le commentaire est obligatoire")
    @Size(min = 10, max = 1000, message = "Le commentaire doit contenir entre 10 et 1000 caractères")
    private String commentaire;

    public AvisRequest() {
    }

    public Long getStudentDemarcheId() {
        return studentDemarcheId;
    }

    public void setStudentDemarcheId(Long studentDemarcheId) {
        this.studentDemarcheId = studentDemarcheId;
    }

    public Integer getNote() {
        return note;
    }

    public void setNote(Integer note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}
