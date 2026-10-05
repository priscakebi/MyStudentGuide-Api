package com.mystudentguide.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Entité représentant une étape d'une démarche administrative.
 *
 * Une démarche peut être composée de plusieurs étapes.
 *
 * Exemple :
 *
 * Démarche :
 * "Renouvellement du titre de séjour étudiant"
 *
 * Étapes :
 * 1. Préparer les documents
 * 2. Déposer la demande en ligne
 * 3. Suivre l'avancement de la demande
 * 4. Récupérer le titre de séjour
 *
 * Cette entité contient uniquement les informations générales
 * sur les étapes de la démarche.
 *
 * Elle ne contient aucune information propre à un étudiant.
 * Le suivi personnel de l'étudiant reste géré par StudentDemarche.
 */
@Entity
@Table(name = "etapes_demarches")
public class EtapeDemarche {

    /**
     * Identifiant unique de l'étape.
     *
     * Il est généré automatiquement par la base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Numéro permettant de définir l'ordre de l'étape
     * dans la démarche.
     *
     * Exemple :
     * 1 = première étape
     * 2 = deuxième étape
     * 3 = troisième étape
     */
    @Column(nullable = false)
    private Integer numero;

    /**
     * Titre court de l'étape.
     *
     * Exemple :
     * "Préparer les documents"
     */
    @Column(nullable = false, length = 150)
    private String titre;

    /**
     * Description détaillée permettant d'expliquer
     * à l'étudiant ce qu'il doit faire pendant cette étape.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Démarche à laquelle cette étape appartient.
     *
     * Plusieurs étapes peuvent être associées à une même démarche.
     *
     * Exemple :
     *
     * Renouvellement du titre de séjour
     *      ├── Étape 1
     *      ├── Étape 2
     *      └── Étape 3
     *
     * La relation est ignorée par Jackson afin d'éviter
     * qu'une sérialisation de l'étape recharge la démarche,
     * qui pourrait elle-même contenir la liste des étapes.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demarche_id", nullable = false)
    @JsonIgnore
    private Demarche demarche;

    /**
     * Constructeur vide obligatoire pour JPA.
     */
    public EtapeDemarche() {
    }

    /**
     * Constructeur permettant de créer directement
     * une étape associée à une démarche.
     *
     * @param numero numéro de l'étape
     * @param titre titre de l'étape
     * @param description description de l'étape
     * @param demarche démarche associée
     */
    public EtapeDemarche(
            Integer numero,
            String titre,
            String description,
            Demarche demarche
    ) {
        this.numero = numero;
        this.titre = titre;
        this.description = description;
        this.demarche = demarche;
    }

    /**
     * Retourne l'identifiant de l'étape.
     *
     * @return identifiant de l'étape
     */
    public Long getId() {
        return id;
    }

    /**
     * Modifie l'identifiant de l'étape.
     *
     * @param id nouvel identifiant
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Retourne le numéro de l'étape.
     *
     * @return numéro de l'étape
     */
    public Integer getNumero() {
        return numero;
    }

    /**
     * Modifie le numéro de l'étape.
     *
     * @param numero nouveau numéro
     */
    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    /**
     * Retourne le titre de l'étape.
     *
     * @return titre de l'étape
     */
    public String getTitre() {
        return titre;
    }

    /**
     * Modifie le titre de l'étape.
     *
     * @param titre nouveau titre
     */
    public void setTitre(String titre) {
        this.titre = titre;
    }

    /**
     * Retourne la description de l'étape.
     *
     * @return description de l'étape
     */
    public String getDescription() {
        return description;
    }

    /**
     * Modifie la description de l'étape.
     *
     * @param description nouvelle description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retourne la démarche associée à l'étape.
     *
     * @return démarche associée
     */
    public Demarche getDemarche() {
        return demarche;
    }

    /**
     * Modifie la démarche associée à l'étape.
     *
     * @param demarche nouvelle démarche associée
     */
    public void setDemarche(Demarche demarche) {
        this.demarche = demarche;
    }
}