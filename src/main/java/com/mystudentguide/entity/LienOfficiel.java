package com.mystudentguide.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Entité représentant un lien vers une source officielle
 * associée à une démarche administrative.
 *
 * Une démarche peut avoir plusieurs liens officiels.
 *
 * Exemple :
 *
 * Démarche :
 * "Renouvellement du titre de séjour étudiant"
 *
 * Liens :
 * - ANEF
 * - Service-Public.fr
 * - Préfecture concernée
 *
 * Ces liens permettent à l'étudiant de consulter directement
 * les informations disponibles sur les plateformes officielles.
 *
 * Le lien est une donnée de référence liée à la démarche.
 * Il ne contient aucune donnée personnelle de l'étudiant.
 */
@Entity
@Table(name = "liens_officiels")
public class LienOfficiel {

    /**
     * Identifiant unique du lien.
     *
     * Il est généré automatiquement par la base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom de la plateforme ou de la source officielle.
     *
     * Exemple :
     * "ANEF"
     * "Service-Public.fr"
     */
    @Column(nullable = false, length = 150)
    private String nom;

    /**
     * Adresse URL permettant d'accéder à la source officielle.
     *
     * Exemple :
     * "https://administration-etrangers-en-france.interieur.gouv.fr/"
     */
    @Column(nullable = false, length = 500)
    private String url;

    /**
     * Description permettant de préciser ce que l'étudiant
     * trouvera sur cette source.
     *
     * Exemple :
     * "Plateforme officielle permettant d'effectuer
     * certaines démarches liées au séjour."
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Démarche à laquelle ce lien officiel est associé.
     *
     * Plusieurs liens peuvent être associés à une même démarche.
     *
     * Exemple :
     *
     * Démarche
     *    ├── Lien ANEF
     *    ├── Lien Service-Public
     *    └── Lien Préfecture
     *
     * La relation est ignorée par Jackson afin d'éviter
     * les problèmes de sérialisation récursive.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demarche_id", nullable = false)
    @JsonIgnore
    private Demarche demarche;

    /**
     * Constructeur vide obligatoire pour JPA.
     */
    public LienOfficiel() {
    }

    /**
     * Constructeur permettant de créer directement
     * un lien associé à une démarche.
     *
     * @param nom nom de la source officielle
     * @param url adresse URL de la source
     * @param description description de la source
     * @param demarche démarche associée
     */
    public LienOfficiel(
            String nom,
            String url,
            String description,
            Demarche demarche
    ) {
        this.nom = nom;
        this.url = url;
        this.description = description;
        this.demarche = demarche;
    }

    /**
     * Retourne l'identifiant du lien.
     *
     * @return identifiant du lien
     */
    public Long getId() {
        return id;
    }

    /**
     * Modifie l'identifiant du lien.
     *
     * @param id nouvel identifiant
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Retourne le nom de la source officielle.
     *
     * @return nom de la source
     */
    public String getNom() {
        return nom;
    }

    /**
     * Modifie le nom de la source officielle.
     *
     * @param nom nouveau nom de la source
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Retourne l'URL de la source officielle.
     *
     * @return URL de la source
     */
    public String getUrl() {
        return url;
    }

    /**
     * Modifie l'URL de la source officielle.
     *
     * @param url nouvelle URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Retourne la description du lien.
     *
     * @return description du lien
     */
    public String getDescription() {
        return description;
    }

    /**
     * Modifie la description du lien.
     *
     * @param description nouvelle description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retourne la démarche associée au lien.
     *
     * @return démarche associée
     */
    public Demarche getDemarche() {
        return demarche;
    }

    /**
     * Modifie la démarche associée au lien.
     *
     * @param demarche nouvelle démarche associée
     */
    public void setDemarche(Demarche demarche) {
        this.demarche = demarche;
    }
}