package com.mystudentguide.dto.document;

import com.mystudentguide.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO utilisé lors de la création ou de la modification
 * d'un document requis.
 *
 * Ce DTO permet de ne pas envoyer directement l'entité
 * DocumentRequis depuis le frontend.
 *
 * Exemple de données reçues :
 *
 * {
 *     "nom": "Passeport",
 *     "description": "Passeport en cours de validité",
 *     "type": "PASSEPORT",
 *     "demarcheId": 1
 * }
 */
public class DocumentRequisRequest {

    /**
     * Nom du document requis.
     *
     * Exemple :
     * "Passeport"
     * "Certificat de scolarité"
     */
    @NotBlank(message = "Le nom du document est obligatoire")
    private String nom;

    /**
     * Description permettant d'expliquer
     * ce qui est attendu pour ce document.
     */
    private String description;

    /**
     * Type du document.
     *
     * Exemple :
     * PASSEPORT
     * CERTIFICAT_SCOLARITE
     * JUSTIFICATIF_DOMICILE
     */
    @NotNull(message = "Le type du document est obligatoire")
    private DocumentType type;

    /**
     * Identifiant de la démarche à laquelle
     * le document requis est associé.
     */
    @NotNull(message = "La démarche est obligatoire")
    private Long demarcheId;

    /**
     * Constructeur vide nécessaire pour
     * la désérialisation JSON par Spring.
     */
    public DocumentRequisRequest() {
    }

    /**
     * Constructeur permettant de créer directement
     * une requête complète.
     *
     * @param nom nom du document
     * @param description description du document
     * @param type type du document
     * @param demarcheId identifiant de la démarche
     */
    public DocumentRequisRequest(
            String nom,
            String description,
            DocumentType type,
            Long demarcheId
    ) {
        this.nom = nom;
        this.description = description;
        this.type = type;
        this.demarcheId = demarcheId;
    }

    /**
     * Retourne le nom du document.
     *
     * @return nom du document
     */
    public String getNom() {
        return nom;
    }

    /**
     * Modifie le nom du document.
     *
     * @param nom nouveau nom
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Retourne la description du document.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Modifie la description du document.
     *
     * @param description nouvelle description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retourne le type du document.
     *
     * @return type du document
     */
    public DocumentType getType() {
        return type;
    }

    /**
     * Modifie le type du document.
     *
     * @param type nouveau type
     */
    public void setType(DocumentType type) {
        this.type = type;
    }

    /**
     * Retourne l'identifiant de la démarche.
     *
     * @return identifiant de la démarche
     */
    public Long getDemarcheId() {
        return demarcheId;
    }

    /**
     * Modifie l'identifiant de la démarche.
     *
     * @param demarcheId nouvel identifiant de la démarche
     */
    public void setDemarcheId(Long demarcheId) {
        this.demarcheId = demarcheId;
    }
}