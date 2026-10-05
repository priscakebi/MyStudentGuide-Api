package com.mystudentguide.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mystudentguide.enums.LogementType;
import com.mystudentguide.enums.VisaType;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant le profil administratif d'un étudiant.
 *
 * Le profil complète les informations du compte User.
 * Il contient les informations nécessaires pour personnaliser
 * les démarches administratives proposées à l'étudiant.
 *
 * Les informations de connexion (email, mot de passe, rôle, etc.)
 * restent dans l'entité User.
 */
@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    /**
     * Identifiant unique du profil étudiant.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nationalité de l'étudiant.
     */
    private String nationalite;

    /**
     * Type de visa ou de titre de séjour de l'étudiant.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_visa")
    private VisaType typeVisa;

    /**
     * Université ou établissement d'enseignement de l'étudiant.
     */
    private String universite;

    /**
     * Niveau d'études de l'étudiant.
     */
    @Column(name = "niveau_etudes")
    private String niveauEtudes;

    /**
     * Indique si l'étudiant est arrivé en France pour la première fois.
     */
    @Column(name = "premiere_arrivee")
    private Boolean premiereArrivee;

    /**
     * Type de logement de l'étudiant.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_logement")
    private LogementType typeLogement;

    /**
     * Ville dans laquelle l'étudiant étudie.
     */
    @Column(name = "ville_etudes")
    private String villeEtudes;

    /**
     * Adresse de résidence de l'étudiant.
     */
    private String adresse;

    /**
     * Numéro de téléphone de l'étudiant.
     */
    private String telephone;

    /**
     * Indique si l'étudiant a terminé de compléter son profil.
     *
     * Cette information peut notamment être utilisée
     * pour savoir s'il doit encore répondre au questionnaire
     * de profil après son inscription.
     */
    @Column(name = "profile_completed")
    private Boolean profileCompleted = false;

    /**
     * Compte utilisateur associé au profil étudiant.
     *
     * Relation 1-1 :
     * un compte User possède un profil StudentProfile.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    /**
     * Liste des démarches suivies par l'étudiant.
     *
     * Cette relation permet de suivre les démarches personnelles
     * de l'étudiant et leur état d'avancement.
     *
     * Exemple :
     * un étudiant peut suivre une démarche "Demande de titre de séjour"
     * avec un statut "EN_COURS".
     */
    @OneToMany(
            mappedBy = "studentProfile",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<StudentDemarche> studentDemarches = new ArrayList<>();


    // ============================================================
    // CONSTRUCTEURS
    // ============================================================

    /**
     * Constructeur vide obligatoire pour JPA.
     */
    public StudentProfile() {
    }

    /**
     * Constructeur permettant de créer rapidement un profil étudiant.
     *
     * @param nationalite nationalité de l'étudiant
     * @param typeVisa type de visa ou titre de séjour
     * @param universite université de l'étudiant
     * @param niveauEtudes niveau d'études
     * @param villeEtudes ville d'études
     */
    public StudentProfile(
            String nationalite,
            VisaType typeVisa,
            String universite,
            String niveauEtudes,
            String villeEtudes
    ) {
        this.nationalite = nationalite;
        this.typeVisa = typeVisa;
        this.universite = universite;
        this.niveauEtudes = niveauEtudes;
        this.villeEtudes = villeEtudes;
        this.profileCompleted = false;
    }


    // ============================================================
    // GETTERS ET SETTERS
    // ============================================================

    /**
     * Retourne l'identifiant du profil.
     */
    public Long getId() {
        return id;
    }

    /**
     * Définit l'identifiant du profil.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Retourne la nationalité de l'étudiant.
     */
    public String getNationalite() {
        return nationalite;
    }

    /**
     * Modifie la nationalité de l'étudiant.
     */
    public void setNationalite(String nationalite) {
        this.nationalite = nationalite;
    }

    /**
     * Retourne le type de visa.
     */
    public VisaType getTypeVisa() {
        return typeVisa;
    }

    /**
     * Modifie le type de visa.
     */
    public void setTypeVisa(VisaType typeVisa) {
        this.typeVisa = typeVisa;
    }

    /**
     * Retourne l'université de l'étudiant.
     */
    public String getUniversite() {
        return universite;
    }

    /**
     * Modifie l'université de l'étudiant.
     */
    public void setUniversite(String universite) {
        this.universite = universite;
    }

    /**
     * Retourne le niveau d'études.
     */
    public String getNiveauEtudes() {
        return niveauEtudes;
    }

    /**
     * Modifie le niveau d'études.
     */
    public void setNiveauEtudes(String niveauEtudes) {
        this.niveauEtudes = niveauEtudes;
    }

    /**
     * Retourne l'information concernant la première arrivée en France.
     */
    public Boolean getPremiereArrivee() {
        return premiereArrivee;
    }

    /**
     * Modifie l'information concernant la première arrivée en France.
     */
    public void setPremiereArrivee(Boolean premiereArrivee) {
        this.premiereArrivee = premiereArrivee;
    }

    /**
     * Retourne le type de logement.
     */
    public LogementType getTypeLogement() {
        return typeLogement;
    }

    /**
     * Modifie le type de logement.
     */
    public void setTypeLogement(LogementType typeLogement) {
        this.typeLogement = typeLogement;
    }

    /**
     * Retourne la ville d'études.
     */
    public String getVilleEtudes() {
        return villeEtudes;
    }

    /**
     * Modifie la ville d'études.
     */
    public void setVilleEtudes(String villeEtudes) {
        this.villeEtudes = villeEtudes;
    }

    /**
     * Retourne l'adresse de l'étudiant.
     */
    public String getAdresse() {
        return adresse;
    }

    /**
     * Modifie l'adresse de l'étudiant.
     */
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    /**
     * Retourne le numéro de téléphone.
     */
    public String getTelephone() {
        return telephone;
    }

    /**
     * Modifie le numéro de téléphone.
     */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /**
     * Retourne l'état de complétion du profil.
     */
    public Boolean getProfileCompleted() {
        return profileCompleted;
    }

    /**
     * Modifie l'état de complétion du profil.
     */
    public void setProfileCompleted(Boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    /**
     * Retourne le compte utilisateur associé.
     */
    public User getUser() {
        return user;
    }

    /**
     * Définit le compte utilisateur associé.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Retourne la liste des démarches suivies par l'étudiant.
     */
    public List<StudentDemarche> getStudentDemarches() {
        return studentDemarches;
    }

    /**
     * Modifie la liste des démarches suivies par l'étudiant.
     */
    public void setStudentDemarches(List<StudentDemarche> studentDemarches) {
        this.studentDemarches = studentDemarches;
    }
}