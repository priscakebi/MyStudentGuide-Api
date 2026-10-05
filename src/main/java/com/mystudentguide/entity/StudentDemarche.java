package com.mystudentguide.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mystudentguide.enums.DemarcheStatus;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Entité représentant le suivi d'une démarche pour un étudiant.
 *
 * Une démarche générale est définie dans l'entité Demarche.
 * Cette entité permet de suivre cette démarche pour un étudiant précis.
 *
 * IMPORTANT :
 * Un même étudiant ne peut suivre qu'une seule fois
 * une même démarche.
 *
 * Cette règle est garantie :
 * - au niveau de l'application par le service ;
 * - au niveau de la base de données par une contrainte d'unicité.
 */
@Entity
@Table(
    name = "student_demarches",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_student_demarche",
            columnNames = {
                "student_profile_id",
                "demarche_id"
            }
        )
    }
)
public class StudentDemarche {

    /**
     * Identifiant unique du suivi.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Profil étudiant concerné.
     *
     * Un étudiant peut suivre plusieurs démarches différentes.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_profile_id", nullable = false)
    private StudentProfile studentProfile;

    /**
     * Démarche générale suivie.
     *
     * Une même démarche peut être suivie par plusieurs étudiants.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demarche_id", nullable = false)
    private Demarche demarche;

    /**
     * Statut de la démarche pour cet étudiant.
     *
     * Par défaut, une nouvelle démarche est "A_FAIRE".
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DemarcheStatus statut = DemarcheStatus.A_FAIRE;

    /**
     * Date à laquelle la démarche a été attribuée
     * à l'étudiant.
     */
    @Column(name = "date_attribution")
    private LocalDate dateAttribution;

    /**
     * Date à laquelle la démarche a été terminée.
     *
     * Peut rester null tant que la démarche n'est pas terminée.
     */
    @Column(name = "date_terminaison")
    private LocalDate dateTerminaison;

    /**
     * Date du dernier rappel envoyé.
     *
     * Cette information pourra être utilisée par le système
     * de notification pour éviter des rappels trop fréquents.
     */
    @Column(name = "dernier_rappel")
    private LocalDate dernierRappel;

    /**
     * Commentaire associé au suivi.
     */
    @Column(columnDefinition = "TEXT")
    private String commentaire;


    /**
     * Constructeur vide obligatoire pour JPA.
     */
    public StudentDemarche() {
    }


    /**
     * Initialise automatiquement les valeurs par défaut
     * avant l'enregistrement en base.
     */
    @PrePersist
    protected void onCreate() {

        /*
         * Si aucune date d'attribution n'est définie,
         * on utilise la date du jour.
         */
        if (dateAttribution == null) {
            dateAttribution = LocalDate.now();
        }

        /*
         * Si aucun statut n'est défini,
         * la démarche commence avec A_FAIRE.
         */
        if (statut == null) {
            statut = DemarcheStatus.A_FAIRE;
        }
    }


    // ============================================================
    // GETTERS ET SETTERS
    // ============================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StudentProfile getStudentProfile() {
        return studentProfile;
    }

    public void setStudentProfile(StudentProfile studentProfile) {
        this.studentProfile = studentProfile;
    }

    public Demarche getDemarche() {
        return demarche;
    }

    public void setDemarche(Demarche demarche) {
        this.demarche = demarche;
    }

    public DemarcheStatus getStatut() {
        return statut;
    }

    public void setStatut(DemarcheStatus statut) {
        this.statut = statut;
    }

    public LocalDate getDateAttribution() {
        return dateAttribution;
    }

    public void setDateAttribution(LocalDate dateAttribution) {
        this.dateAttribution = dateAttribution;
    }

    public LocalDate getDateTerminaison() {
        return dateTerminaison;
    }

    public void setDateTerminaison(LocalDate dateTerminaison) {
        this.dateTerminaison = dateTerminaison;
    }

    public LocalDate getDernierRappel() {
        return dernierRappel;
    }

    public void setDernierRappel(LocalDate dernierRappel) {
        this.dernierRappel = dernierRappel;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}