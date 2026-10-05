package com.mystudentguide.dto.studentprofile;

import com.mystudentguide.enums.VisaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO utilisé pour créer ou modifier le profil administratif
 * d'un étudiant.
 *
 * Le profil étudiant contient les informations nécessaires
 * à MyStudentGuide pour déterminer les démarches administratives
 * qui correspondent à la situation de l'étudiant.
 *
 * IMPORTANT :
 * On ne met pas ici :
 *
 * - l'id du profil ;
 * - l'utilisateur associé ;
 * - les démarches ;
 *
 * Ces informations sont gérées par le backend.
 */
public class StudentProfileRequest {

    /**
     * Nationalité de l'étudiant.
     *
     * Cette information peut être utilisée pour déterminer
     * certaines démarches administratives.
     */
    @NotBlank(message = "La nationalité est obligatoire")
    private String nationalite;

    /**
     * Type de visa ou de titre de séjour de l'étudiant.
     */
    @NotNull(message = "Le type de visa est obligatoire")
    private VisaType visaType;

    /**
     * Date d'arrivée de l'étudiant en France.
     *
     * Elle sera utile pour certaines démarches dépendant
     * de la date d'arrivée sur le territoire.
     */
    @NotBlank(message = "La date d'arrivée est obligatoire")
    private String dateArrivee;

    /**
     * Situation de logement de l'étudiant.
     *
     * Exemple :
     * - RESIDENCE_UNIVERSITAIRE
     * - LOCATION
     * - HEBERGE
     */
    @NotBlank(message = "La situation de logement est obligatoire")
    private String situationLogement;

    /**
     * Situation universitaire de l'étudiant.
     *
     * Exemple :
     * - ETUDIANT
     * - ALTERNANT
     * - STAGIAIRE
     */
    @NotBlank(message = "La situation universitaire est obligatoire")
    private String situationUniversitaire;

    /**
     * Constructeur vide nécessaire à Spring
     * pour convertir automatiquement le JSON reçu
     * en objet Java.
     */
    public StudentProfileRequest() {
    }

    // Getters et Setters

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