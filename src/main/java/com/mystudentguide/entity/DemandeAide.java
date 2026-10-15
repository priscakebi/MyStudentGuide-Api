package com.mystudentguide.entity;

import com.mystudentguide.enums.StatutDemandeAide;
import com.mystudentguide.enums.SujetAide;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Demande d'aide envoyée par un étudiant à l'administration
 * (page « Besoin d'aide ? »), et réponse de l'administrateur.
 *
 * Comme pour les signalements, l'auteur est enregistré par son
 * identifiant et son nom, sans clé étrangère vers users : ses
 * demandes sont supprimées avec son compte (AccountService).
 *
 * La démarche éventuellement concernée est gardée par son
 * identifiant et son titre : la demande reste lisible même si
 * la démarche est supprimée ensuite.
 */
@Entity
@Table(name = "demandes_aide")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeAide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SujetAide sujet;

    /** Démarche concernée (facultative). */
    @Column(name = "demarche_id")
    private Long demarcheId;

    @Column(name = "demarche_titre", length = 150)
    private String demarcheTitre;

    @Column(nullable = false, length = 2000)
    private String message;

    /** Réponse de l'administrateur, envoyée aussi en notification. */
    @Column(length = 2000)
    private String reponse;

    /** Étudiant auteur de la demande. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Prénom et nom affichés à l'administrateur. */
    @Column(length = 200)
    private String auteur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutDemandeAide statut = StatutDemandeAide.OUVERTE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateReponse;

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (statut == null) {
            statut = StatutDemandeAide.OUVERTE;
        }
    }
}
