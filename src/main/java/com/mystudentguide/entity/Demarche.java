package com.mystudentguide.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant une démarche administrative générale
 * proposée par MyStudentGuide.
 *
 * Une démarche est commune à plusieurs étudiants.
 *
 * Exemple :
 * "Renouvellement du titre de séjour étudiant"
 *
 * La démarche décrit les informations générales nécessaires
 * pour réaliser une procédure administrative.
 *
 * Le suivi personnel d'un étudiant n'est PAS enregistré
 * directement dans cette entité.
 *
 * Il est géré par l'entité StudentDemarche.
 *
 * Une démarche peut également posséder :
 * - plusieurs documents requis ;
 * - plusieurs étapes ;
 * - plusieurs liens vers des sources officielles.
 */
@Entity
@Table(name = "demarches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Demarche {

    /**
     * Identifiant unique de la démarche.
     *
     * La valeur est générée automatiquement
     * par la base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Titre de la démarche.
     *
     * Exemple :
     * "Renouvellement du titre de séjour étudiant"
     */
    @Column(nullable = false, length = 150)
    private String titre;

    /**
     * Description générale de la démarche.
     *
     * Cette description permet d'expliquer à l'étudiant
     * en quoi consiste la procédure administrative.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Date à laquelle la démarche a été créée
     * dans MyStudentGuide.
     *
     * Cette date est automatiquement renseignée
     * avant la première sauvegarde en base.
     */
    @Column(nullable = false)
    private LocalDate dateCreation;

    /**
     * Date limite générale de la démarche.
     *
     * Elle peut être null lorsqu'il n'existe pas
     * de date limite fixe.
     */
    private LocalDate dateLimite;

    /**
     * Catégorie de la démarche.
     *
     * Exemple :
     * - Séjour
     * - Logement
     * - Université
     * - Santé
     *
     * Plusieurs démarches peuvent appartenir
     * à une même catégorie.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    @JsonIgnore
    private Categorie categorie;

    /**
     * Liste des suivis étudiants associés à cette démarche.
     *
     * Une démarche générale peut être suivie
     * par plusieurs étudiants.
     *
     * Exemple :
     *
     * Démarche
     *    ├── Étudiant A → EN_COURS
     *    ├── Étudiant B → TERMINEE
     *    └── Étudiant C → A_FAIRE
     *
     * Le suivi individuel est géré par StudentDemarche.
     */
    @OneToMany(mappedBy = "demarche")
    @Builder.Default
    @JsonIgnore
    private List<StudentDemarche> studentDemarches = new ArrayList<>();

    /**
     * Liste des documents nécessaires pour réaliser
     * la démarche.
     *
     * IMPORTANT :
     * Ces documents sont uniquement des documents requis
     * à consulter par l'étudiant.
     *
     * MyStudentGuide ne stocke pas les fichiers personnels
     * des étudiants.
     *
     * Exemple :
     *
     * Démarche
     *    ├── Passeport
     *    ├── Certificat de scolarité
     *    └── Justificatif de domicile
     *
     * Les éléments de cette liste sont gérés
     * par l'entité DocumentRequis.
     */
    @OneToMany(
        mappedBy = "demarche",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    @JsonIgnore
    private List<DocumentRequis> documentsRequis = new ArrayList<>();

    /**
     * Liste des étapes nécessaires pour réaliser
     * la démarche.
     *
     * Exemple :
     *
     * Démarche
     *    ├── Étape 1 : Préparer les documents
     *    ├── Étape 2 : Déposer la demande
     *    └── Étape 3 : Suivre la demande
     *
     * Les étapes sont gérées par EtapeDemarche.
     */
    @OneToMany(
        mappedBy = "demarche",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    @JsonIgnore
    private List<EtapeDemarche> etapes = new ArrayList<>();

    /**
     * Liste des liens vers les sources officielles
     * permettant à l'étudiant de consulter les
     * informations de référence.
     *
     * Exemple :
     * - ANEF
     * - Service-Public.fr
     * - Préfecture
     *
     * Les liens sont gérés par LienOfficiel.
     */
    @OneToMany(
        mappedBy = "demarche",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    @JsonIgnore
    private List<LienOfficiel> liensOfficiels = new ArrayList<>();

    /**
     * Méthode exécutée automatiquement par JPA
     * juste avant la première insertion en base.
     *
     * Si aucune date de création n'a été définie,
     * la date du jour est automatiquement utilisée.
     *
     * Si une date a déjà été définie, elle est conservée.
     */
    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDate.now();
        }
    }
}