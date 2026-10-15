package com.mystudentguide.dto.aide;

import com.mystudentguide.entity.DemandeAide;
import com.mystudentguide.enums.StatutDemandeAide;
import com.mystudentguide.enums.SujetAide;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Données échangées pour les demandes d'aide.
 */
public final class DemandeAideDtos {

    private DemandeAideDtos() {
    }

    /** Envoyé par l'étudiant : POST /api/aide */
    public record DemandeAideRequest(

            @NotNull(message = "Choisissez le sujet de votre demande.")
            SujetAide sujet,

            /** Démarche concernée, facultative. */
            Long demarcheId,

            @NotBlank(message = "Décrivez votre difficulté.")
            @Size(min = 10, max = 2000, message = "Votre message doit faire entre 10 et 2000 caractères.")
            String message
    ) {
    }

    /** Envoyé par l'administrateur : PUT /api/admin/aide/{id}/reponse */
    public record ReponseAideRequest(

            @NotBlank(message = "Écrivez une réponse.")
            @Size(max = 2000, message = "La réponse ne doit pas dépasser 2000 caractères.")
            String reponse
    ) {
    }

    /** Affiché à l'étudiant (ses demandes) et à l'administrateur (toutes). */
    public record DemandeAideResponse(
            Long id,
            SujetAide sujet,
            String sujetLibelle,
            Long demarcheId,
            String demarcheTitre,
            String message,
            String reponse,
            String auteur,
            StatutDemandeAide statut,
            LocalDateTime dateCreation,
            LocalDateTime dateReponse
    ) {

        public static DemandeAideResponse of(DemandeAide d) {
            return new DemandeAideResponse(
                    d.getId(),
                    d.getSujet(),
                    d.getSujet().getLibelle(),
                    d.getDemarcheId(),
                    d.getDemarcheTitre(),
                    d.getMessage(),
                    d.getReponse(),
                    d.getAuteur(),
                    d.getStatut(),
                    d.getDateCreation(),
                    d.getDateReponse()
            );
        }
    }
}
