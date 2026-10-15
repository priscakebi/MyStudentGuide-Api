package com.mystudentguide.dto.signalement;

import com.mystudentguide.entity.SignalementDemarche;
import com.mystudentguide.enums.MotifSignalement;
import com.mystudentguide.enums.StatutSignalement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Données échangées pour les signalements de démarches.
 */
public final class SignalementDtos {

    private SignalementDtos() {
    }

    /** Envoyé par l'étudiant : POST /api/demarches/{id}/signalements */
    public record SignalementRequest(
            @NotNull(message = "Choisissez le type de problème.")
            MotifSignalement motif,

            @Size(max = 1000, message = "Le commentaire ne doit pas dépasser 1000 caractères.")
            String commentaire
    ) {
    }

    /** Affiché à l'administrateur. */
    public record SignalementResponse(
            Long id,
            Long demarcheId,
            String demarcheTitre,
            MotifSignalement motif,
            String motifLibelle,
            String commentaire,
            String auteur,
            StatutSignalement statut,
            LocalDateTime dateCreation,
            LocalDateTime dateTraitement
    ) {

        public static SignalementResponse of(SignalementDemarche s) {
            return new SignalementResponse(
                    s.getId(),
                    s.getDemarche().getId(),
                    s.getDemarche().getTitre(),
                    s.getMotif(),
                    s.getMotif().getLibelle(),
                    s.getCommentaire(),
                    s.getAuteur(),
                    s.getStatut(),
                    s.getDateCreation(),
                    s.getDateTraitement()
            );
        }
    }
}
