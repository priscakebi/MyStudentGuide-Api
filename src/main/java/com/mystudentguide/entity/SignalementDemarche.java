package com.mystudentguide.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mystudentguide.enums.MotifSignalement;
import com.mystudentguide.enums.StatutSignalement;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Signalement d'une information erronée ou obsolète sur une démarche,
 * envoyé par un étudiant (bouton « Signaler une erreur »).
 *
 * Comme pour les avis, l'auteur est enregistré par son identifiant et
 * son prénom, sans clé étrangère vers users : ses signalements sont
 * supprimés avec son compte (AccountService).
 */
@Entity
@Table(name = "signalements_demarches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignalementDemarche {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Démarche signalée (supprimée avec elle). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demarche_id", nullable = false)
    @JsonIgnore
    private Demarche demarche;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MotifSignalement motif;

    @Column(length = 1000)
    private String commentaire;

    /** Étudiant auteur du signalement. */
    @Column(name = "user_id", nullable = false)
    @JsonIgnore
    private Long userId;

    /** Prénom affiché à l'administrateur. */
    @Column(length = 100)
    private String auteur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutSignalement statut = StatutSignalement.OUVERT;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateTraitement;

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (statut == null) {
            statut = StatutSignalement.OUVERT;
        }
    }
}
