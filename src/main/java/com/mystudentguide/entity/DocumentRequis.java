package com.mystudentguide.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mystudentguide.enums.DocumentType;
import jakarta.persistence.*;

/**
 * Entité représentant un document requis pour effectuer une démarche
 * administrative.
 *
 * Cette entité ne représente PAS un fichier appartenant à un étudiant.
 *
 * Elle représente uniquement une information indiquant qu'un document
 * est nécessaire pour effectuer une démarche.
 *
 * Exemple :
 *
 * Démarche : "Renouvellement du titre de séjour étudiant"
 *
 * Documents requis :
 * - Passeport
 * - Titre de séjour ou visa
 * - Certificat d'inscription
 * - Justificatif de domicile
 *
 * Aucun fichier n'est importé, enregistré ou exporté par l'application.
 */
@Entity
@Table(name = "documents_requis")
public class DocumentRequis {

    /**
     * Identifiant unique du document requis.
     *
     * La valeur est générée automatiquement par la base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom du document requis.
     *
     * Le nom permet de préciser exactement la pièce demandée.
     *
     * Exemple :
     * "Passeport"
     * "Certificat d'inscription ou de préinscription"
     * "Justificatif de domicile"
     */
    @Column(nullable = false, length = 150)
    private String nom;

    /**
     * Description du document requis.
     *
     * Cette information permet d'apporter des précisions
     * supplémentaires à l'étudiant.
     *
     * Exemple :
     * "Document en cours de validité."
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Version anglaise (facultative) du nom et de la description,
     * affichée aux étudiants qui utilisent l'interface en anglais.
     * Vide : le texte français est affiché.
     */
    @Column(name = "nom_en", length = 150)
    private String nomEn;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    /**
     * Catégorie du document requis.
     *
     * L'énumération est enregistrée sous forme de texte
     * dans la base de données grâce à EnumType.STRING.
     *
     * Les catégories sont volontairement générales.
     *
     * Exemples :
     * - IDENTITE
     * - SCOLARITE
     * - DOMICILE
     * - RESSOURCES
     * - PHOTO
     * - ASSURANCE
     * - AUTRE
     *
     * Le nom du document permet ensuite de préciser la pièce exacte.
     * Par exemple :
     *
     * nom = "Passeport"
     * type = IDENTITE
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType type;

    /**
     * Démarche à laquelle ce document est associé.
     *
     * Une démarche peut avoir plusieurs documents requis.
     *
     * Exemple :
     *
     * Démarche :
     * "Renouvellement du titre de séjour étudiant"
     *
     * Documents :
     * - Passeport
     * - E-photo
     * - Certificat d'inscription
     * - Justificatif de domicile
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demarche_id", nullable = false)
    @JsonIgnore
    private Demarche demarche;

    /**
     * Constructeur vide obligatoire pour JPA.
     */
    public DocumentRequis() {
    }

    /**
     * Constructeur permettant de créer un document requis.
     *
     * @param nom nom du document
     * @param description description du document
     * @param type catégorie du document
     * @param demarche démarche concernée
     */
    public DocumentRequis(
            String nom,
            String description,
            DocumentType type,
            Demarche demarche
    ) {
        this.nom = nom;
        this.description = description;
        this.type = type;
        this.demarche = demarche;
    }

    /**
     * Retourne l'identifiant du document requis.
     *
     * @return identifiant du document
     */
    public Long getId() {
        return id;
    }

    /**
     * Modifie l'identifiant du document requis.
     *
     * @param id nouvel identifiant
     */
    public void setId(Long id) {
        this.id = id;
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
     * @param nom nouveau nom du document
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Retourne la description du document.
     *
     * @return description du document
     */
    public String getDescription() {
        return description;
    }

    /**
     * Modifie la description du document.
     *
     * @param description nouvelle description du document
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retourne la catégorie du document.
     *
     * @return catégorie du document
     */
    public DocumentType getType() {
        return type;
    }

    /**
     * Modifie la catégorie du document.
     *
     * @param type nouvelle catégorie du document
     */
    public void setType(DocumentType type) {
        this.type = type;
    }

    /**
     * Retourne la démarche associée au document.
     *
     * @return démarche associée
     */
    public Demarche getDemarche() {
        return demarche;
    }

    /**
     * Modifie la démarche associée au document.
     *
     * @param demarche nouvelle démarche
     */
    public void setDemarche(Demarche demarche) {
        this.demarche = demarche;
    }

    /**
     * Retourne l'identifiant de la démarche associée.
     *
     * Cette méthode permet de récupérer uniquement l'identifiant
     * de la démarche sans exposer directement l'objet Demarche complet.
     *
     * @return identifiant de la démarche ou null
     */
    public Long getDemarcheId() {

        // Vérifie qu'une démarche est bien associée.
        if (demarche == null) {
            return null;
        }

        // Retourne l'identifiant de la démarche.
        return demarche.getId();
    }

    public String getNomEn() {
        return nomEn;
    }

    public void setNomEn(String nomEn) {
        this.nomEn = nomEn;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }
}
