package com.mystudentguide.dto.studentprofile;

import com.mystudentguide.enums.VisaType;

/**
 * DTO utilisé pour retourner les informations du profil
 * administratif d'un étudiant au frontend.
 *
 * Ce DTO permet de contrôler précisément les informations
 * exposées par l'API.
 *
 * On ne retourne pas directement l'entité StudentProfile
 * afin d'éviter d'exposer ses relations JPA internes.
 */
public class StudentProfileResponse {

    /**
     * Identifiant unique du profil étudiant.
     */
    private Long id;

    /**
     * Identifiant de l'utilisateur auquel
     * appartient le profil.
     *
     * On retourne uniquement l'identifiant et non
     * l'objet User complet.
     */
    private Long userId;

    /**
     * Nationalité de l'étudiant.
     */
    private String nationalite;

    /**
     * Type de visa ou de titre de séjour.
     */
    private VisaType visaType;

    /**
     * Date d'arrivée en France.
     */
    private String dateArrivee;

    /**
     * Situation de logement de l'étudiant.
     */
    private String situationLogement;

    /**
     * Situation universitaire de l'étudiant.
     */
    private String situationUniversitaire;

    /**
     * Constructeur vide nécessaire à Spring.
     */
    public StudentProfileResponse() {
    }

    /**
     * Constructeur permettant de créer directement
     * une réponse complète.
     *
     * @param id identifiant du profil
     * @param userId identifiant de l'utilisateur
     * @param nationalite nationalité
     * @param visaType type de visa
     * @param dateArrivee date d'arrivée
     * @param situationLogement situation de logement
     * @param situationUniversitaire situation universitaire
     */
    public StudentProfileResponse(
            Long id,
            Long userId,
            String nationalite,
            VisaType visaType,
            String dateArrivee,
            String situationLogement,
            String situationUniversitaire
    ) {
        this.id = id;
        this.userId = userId;
        this.nationalite = nationalite;
        this.visaType = visaType;
        this.dateArrivee = dateArrivee;
        this.situationLogement = situationLogement;
        this.situationUniversitaire = situationUniversitaire;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNationalite() {
        return nationalite;
    }

    public void setNationalite(String nationalite) {
        this.nationalite = nationalite;
    }

    public VisaType getVisaType() {
        return visaType;
    }

    public void setVisaType(VisaType visaType) {
        this.visaType = visaType;
    }

    public String getDateArrivee() {
        return dateArrivee;
    }

    public void setDateArrivee(String dateArrivee) {
        this.dateArrivee = dateArrivee;
    }

    public String getSituationLogement() {
        return situationLogement;
    }

    public void setSituationLogement(String situationLogement) {
        this.situationLogement = situationLogement;
    }

    public String getSituationUniversitaire() {
        return situationUniversitaire;
    }

    public void setSituationUniversitaire(String situationUniversitaire) {
        this.situationUniversitaire = situationUniversitaire;
    }
}