package com.mystudentguide.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Trace d'une suggestion automatique déjà envoyée à un étudiant.
 *
 * Empêche de renvoyer la même suggestion, même si l'étudiant a
 * supprimé la notification ou ignoré la démarche.
 *
 * Identifiants simples, sans clé étrangère : les lignes sont supprimées
 * avec le compte de l'étudiant (AccountService) et avec la démarche
 * (DemarcheService).
 */
@Entity
@Table(
        name = "suggestions_envoyees",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "demarche_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestionEnvoyee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "demarche_id", nullable = false)
    private Long demarcheId;

    @Column(nullable = false)
    private LocalDateTime dateEnvoi;

    @PrePersist
    protected void onCreate() {
        if (dateEnvoi == null) {
            dateEnvoi = LocalDateTime.now();
        }
    }
}
