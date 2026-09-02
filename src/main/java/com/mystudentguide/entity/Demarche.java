package com.mystudentguide.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mystudentguide.enums.DemarcheStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une démarche administrative qu'un étudiant
 * international peut avoir à effectuer en France.
 *
 * Une démarche est associée :
 * - à un profil étudiant
 * - à une catégorie
 * - éventuellement à plusieurs documents
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
     * Généré automatiquement par MySQL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Titre de la démarche.
     *
     * Exemple :
     * "Faire sa demande de Sécurité sociale"
     */
    @Column(nullable = false, length = 150)
    private String titre;

    /**
     * Description détaillée de la démarche.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Statut actuel de la démarche.
     *
     * Exemple :
     * A_FAIRE
     * EN_COURS
     * NON_CONCERNEE
     * TERMINEE
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private DemarcheStatus statut = DemarcheStatus.A_FAIRE;

    /**
     * Date de création ou d'attribution de la démarche.
     *
     * Cette date est automatiquement définie par le backend.
     */
    @Column(nullable = false)
    private LocalDate dateCreation;

    /**
     * Date limite éventuelle de la démarche.
     *
     * Certaines démarches peuvent ne pas avoir
     * de date limite.
     */
    private LocalDate dateLimite;

    /**
     * Profil étudiant auquel appartient la démarche.
     *
     * Un profil peut avoir plusieurs démarches.
     *
     * Relation :
     *
     * StudentProfile 1 ---- * Demarche
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_profile_id", nullable = false)
    @JsonIgnore
    private StudentProfile studentProfile;

    /**
     * Catégorie de la démarche.
     *
     * Plusieurs démarches peuvent appartenir
     * à la même catégorie.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    @JsonIgnore
    private Categorie categorie;

    /**
     * Documents nécessaires pour réaliser cette démarche.
     *
     * Une démarche peut avoir plusieurs documents.
     */
    @OneToMany(mappedBy = "demarche")
    @Builder.Default
    @JsonIgnore
    private List<Document> documents = new ArrayList<>();

    /**
     * Initialise automatiquement certaines valeurs
     * avant l'insertion en base de données.
     */
    @PrePersist
    public void prePersist() {

        /*
         * Si aucune date de création n'est fournie,
         * on utilise automatiquement la date actuelle.
         */
        if (dateCreation == null) {
            dateCreation = LocalDate.now();
        }

        /*
         * Par défaut, une nouvelle démarche
         * est à faire.
         */
        if (statut == null) {
            statut = DemarcheStatus.A_FAIRE;
        }
    }
}