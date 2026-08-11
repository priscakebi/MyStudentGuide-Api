package com.mystudentguide.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une catégorie de démarches administratives.
 *
 * Une catégorie permet de regrouper plusieurs démarches
 * ayant un même thème.
 *
 * Exemples :
 * - Santé
 * - Logement
 * - Études
 * - Banque
 * - Séjour
 * - Transport
 *
 * Une catégorie peut contenir plusieurs démarches.
 */
@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_categorie_nom", columnNames = "nom")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categorie {

    /**
     * Identifiant unique de la catégorie.
     *
     * Il est généré automatiquement par la base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom de la catégorie.
     *
     * Exemple :
     * "Santé"
     *
     * Le nom est obligatoire et doit être unique.
     */
    @Column(nullable = false, length = 100)
    private String nom;

    /**
     * Description de la catégorie.
     *
     * Elle permet d'expliquer à l'utilisateur
     * ce que regroupe cette catégorie.
     *
     * Exemple :
     * "Les démarches liées à la couverture santé
     * et à l'assurance maladie."
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Icône associée à la catégorie.
     *
     * Nous stockons uniquement une référence à l'icône
     * (par exemple un nom d'icône ou une classe CSS),
     * et non l'image elle-même.
     *
     * Exemple :
     * "health"
     * "home"
     * "school"
     */
    @Column(length = 100)
    private String icone;

    /**
     * Liste des démarches appartenant à cette catégorie.
     *
     * Relation :
     *
     * Categorie 1 -------- * Demarche
     *
     * Une catégorie peut donc contenir plusieurs démarches.
     *
     * mappedBy = "categorie" indique que c'est l'attribut
     * "categorie" de Demarche qui possède la relation.
     *
     * cascade = CascadeType.ALL signifie que certaines
     * opérations effectuées sur une catégorie pourront
     * être propagées aux démarches associées.
     *
     * orphanRemoval = true signifie qu'une démarche retirée
     * de cette collection pourra être supprimée de la base.
     *
     * ATTENTION :
     * Ces options devront être utilisées avec prudence
     * si les démarches sont réutilisables.
     */
    @OneToMany(mappedBy = "categorie")
    @Builder.Default
    private List<Demarche> demarches = new ArrayList<>();
}