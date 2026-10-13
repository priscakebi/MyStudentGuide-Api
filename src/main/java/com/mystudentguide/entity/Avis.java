package com.mystudentguide.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Avis laissé par un étudiant sur le suivi d'une démarche terminée.
 *
 * Les avis visibles sont affichés publiquement sur la page d'accueil.
 *
 * Choix de conception :
 * - aucune clé étrangère vers users / student_demarches : supprimer
 *   un compte ou un suivi n'est jamais bloqué par un avis (les avis
 *   d'un compte supprimé sont effacés par AccountService) ;
 * - le prénom, l'initiale du nom et le titre de la démarche sont
 *   copiés au moment de l'avis : la page d'accueil n'expose ainsi
 *   ni l'e-mail ni le nom complet de l'étudiant.
 */
@Entity
@Table(
    name = "avis",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_avis_student_demarche",
            columnNames = {"student_demarche_id"}
        )
    }
)
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Note de 1 à 5 étoiles. */
    @Column(nullable = false)
    private Integer note;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String commentaire;

    /** Suivi de démarche concerné (un seul avis par suivi). */
    @Column(name = "student_demarche_id", nullable = false)
    private Long studentDemarcheId;

    /** Auteur de l'avis. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Nom affiché publiquement, par exemple « Awa K. ». */
    @Column(name = "auteur", nullable = false, length = 120)
    private String auteur;

    @Column(name = "demarche_titre", nullable = false, length = 255)
    private String demarcheTitre;

    /** L'administrateur peut masquer un avis sans le supprimer. */
    @Column(nullable = false)
    private Boolean visible = true;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    public Avis() {
    }

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (visible == null) {
            visible = true;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getStudentDemarcheId() {
        return studentDemarcheId;
    }

    public void setStudentDemarcheId(Long studentDemarcheId) {
        this.studentDemarcheId = studentDemarcheId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    public String getDemarcheTitre() {
        return demarcheTitre;
    }

    public void setDemarcheTitre(String demarcheTitre) {
        this.demarcheTitre = demarcheTitre;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
