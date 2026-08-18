package com.mystudentguide.dto.demarche;

import com.mystudentguide.enums.DemarcheStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO utilisé pour créer ou modifier une démarche.
 *
 * Ce DTO représente les données reçues depuis le frontend.
 *
 * On n'utilise pas directement l'entité Demarche afin de
 * contrôler précisément les données que le client peut envoyer.
 */
public class DemarcheRequest {

    /**
     * Nom de la démarche.
     *
     * Exemple :
     * "Demande de titre de séjour"
     */
    @NotBlank(message = "Le nom de la démarche est obligatoire")
    private String nom;

    /**
     * Description détaillée de la démarche.
     */
    @NotBlank(message = "La description de la démarche est obligatoire")
    private String description;

    /**
     * Statut de la démarche.
     *
     * Exemple :
     * A_FAIRE
     * EN_COURS
     * TERMINEE
     */
    @NotNull(message = "Le statut de la démarche est obligatoire")
    private DemarcheStatus statut;

    /**
     * Identifiant de la catégorie à laquelle
     * appartient la démarche.
     *
     * Exemple :
     * 1 = Séjour
     * 2 = Logement
     * 3 = Santé
     */
    @NotNull(message = "La catégorie est obligatoire")
    private Long categorieId;

    /**
     * Identifiant du profil étudiant concerné
     * par la démarche.
     */
    @NotNull(message = "Le profil étudiant est obligatoire")
    private Long studentProfileId;

    /**
     * Constructeur vide.
     *
     * Nécessaire pour que Spring puisse transformer
     * automatiquement le JSON reçu en objet Java.
     */
    public DemarcheRequest() {
    }

    /**
     * Retourne le nom de la démarche.
     *
     * @return nom de la démarche
     */
    public String getNom() {
        return nom;
    }

    /**
     * Définit le nom de la démarche.
     *
     * @param nom nom de la démarche
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Retourne la description.
     *
     * @return description de la démarche
     */
    public String getDescription() {
        return description;
    }

    /**
     * Définit la description.
     *
     * @param description description de la démarche
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retourne le statut de la démarche.
     *
     * @return statut
     */
    public DemarcheStatus getStatut() {
        return statut;
    }

    /**
     * Définit le statut de la démarche.
     *
     * @param statut nouveau statut
     */
    public void setStatut(DemarcheStatus statut) {
        this.statut = statut;
    }

    /**
     * Retourne l'identifiant de la catégorie.
     *
     * @return catégorie
     */
    public Long getCategorieId() {
        return categorieId;
    }

    /**
     * Définit l'identifiant de la catégorie.
     *
     * @param categorieId identifiant de la catégorie
     */
    public void setCategorieId(Long categorieId) {
        this.categorieId = categorieId;
    }

    /**
     * Retourne l'identifiant du profil étudiant.
     *
     * @return identifiant du profil étudiant
     */
    public Long getStudentProfileId() {
        return studentProfileId;
    }

    /**
     * Définit l'identifiant du profil étudiant.
     *
     * @param studentProfileId identifiant du profil étudiant
     */
    public void setStudentProfileId(Long studentProfileId) {
        this.studentProfileId = studentProfileId;
    }
}