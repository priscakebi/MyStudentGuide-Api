package com.mystudentguide.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import com.mystudentguide.enums.LogementType;
import com.mystudentguide.enums.ReferenceEcheance;
import com.mystudentguide.enums.StatutPublication;
import com.mystudentguide.enums.VisaType;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
     * Version anglaise (facultative) du titre et de la description,
     * affichée aux étudiants qui utilisent l'interface en anglais.
     * Vide : le texte français est affiché.
     */
    @Column(name = "titre_en", length = 150)
    private String titreEn;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

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

    // ============================================================
    // DATE LIMITE PERSONNALISÉE (voir service.EcheancePersonnelle)
    // ============================================================

    /**
     * Date du profil de l'étudiant servant de point de départ à sa date
     * limite personnelle (ex. : ARRIVEE_EN_FRANCE). null = pas de règle :
     * la date limite générale s'applique.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "echeance_reference", length = 30)
    private ReferenceEcheance echeanceReference;

    /**
     * Nombre de jours ajoutés à la date de référence : positif = après
     * (ex. 90 jours après l'arrivée), négatif = avant (ex. -60 : 2 mois
     * avant la fin du titre de séjour).
     */
    @Column(name = "echeance_decalage_jours")
    private Integer echeanceDecalageJours;

    // ============================================================
    // PUBLICATION ET SOURCE OFFICIELLE (import Service-Public)
    // ============================================================

    /**
     * BROUILLON : visible uniquement par les administrateurs.
     * null (démarches créées avant ce champ) = publiée.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_publication", length = 20)
    @Builder.Default
    private StatutPublication statutPublication = StatutPublication.PUBLIEE;

    /** Identifiant de la fiche Service-Public d'origine (ex. : F2231). */
    @Column(name = "source_id", length = 20)
    private String sourceId;

    /** Adresse de la fiche officielle, affichée comme source. */
    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    /** Date de mise à jour de la fiche officielle au moment de l'import. */
    @Column(name = "source_date_maj")
    private LocalDate sourceDateMaj;

    /**
     * Date d'une version plus récente de la fiche, détectée par la
     * vérification automatique et pas encore appliquée (null sinon).
     */
    @Column(name = "source_date_disponible")
    private LocalDate sourceDateDisponible;

    // ============================================================
    // CIBLAGE : SUGGESTION AUTOMATIQUE D'APRÈS LE PROFIL
    // ============================================================
    //
    // Quand le ciblage est actif, la démarche est suggérée
    // automatiquement (notification) aux étudiants dont le profil
    // remplit TOUS les critères renseignés. Un critère vide
    // (null ou liste vide) ne filtre pas.

    /** Suggestion automatique activée (null = non). */
    @Column(name = "cible_active")
    private Boolean cibleActive;

    /** true : seulement les étudiants hors UE / EEE / Suisse. */
    @Column(name = "cible_hors_ue")
    private Boolean cibleHorsUE;

    /** true : première arrivée en France ; false : déjà installés. */
    @Column(name = "cible_premiere_arrivee")
    private Boolean ciblePremiereArrivee;

    /** Types de visa visés (vide = tous). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "demarche_cible_visas", joinColumns = @JoinColumn(name = "demarche_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "type_visa", length = 40)
    @Builder.Default
    private Set<VisaType> cibleVisas = new HashSet<>();

    /** Types de logement visés (vide = tous). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "demarche_cible_logements", joinColumns = @JoinColumn(name = "demarche_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "type_logement", length = 40)
    @Builder.Default
    private Set<LogementType> cibleLogements = new HashSet<>();

    /** Vrai si la suggestion automatique est activée. */
    public boolean isCiblageActif() {
        return Boolean.TRUE.equals(cibleActive);
    }

    /** Vrai si la démarche est visible par les étudiants. */
    @JsonIgnore
    public boolean isPubliee() {
        return statutPublication != StatutPublication.BROUILLON;
    }

    /** Vrai si une version plus récente de la fiche officielle est disponible. */
    public boolean isMiseAJourDisponible() {
        return sourceDateDisponible != null;
    }

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
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "demarches"})
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
     * Signalements d'erreurs envoyés par les étudiants
     * (supprimés avec la démarche).
     */
    @OneToMany(
        mappedBy = "demarche",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    @JsonIgnore
    private List<SignalementDemarche> signalements = new ArrayList<>();

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