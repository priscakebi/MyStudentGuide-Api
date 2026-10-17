package com.mystudentguide.dto.demarche;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Conseils pratiques des démarches. */
public final class ConseilDtos {

    private ConseilDtos() {
    }

    /** Conseil affiché sur la fiche (et renvoyé à l'admin). */
    public record ConseilDTO(Long id, String texte, String texteEn) {
    }

    /** Ajout ou modification par l'admin. */
    public record ConseilRequest(
            @NotBlank(message = "Le conseil est vide.")
            @Size(max = 500, message = "Un conseil ne doit pas dépasser 500 caractères.")
            String texte,

            @Size(max = 500, message = "La version anglaise ne doit pas dépasser 500 caractères.")
            String texteEn
    ) {
    }

    /** « Astuce du jour » de Mon espace, avec la démarche d'où elle vient. */
    public record AstuceDuJour(
            String texte,
            String texteEn,
            Long demarcheId,
            String demarcheTitre,
            String demarcheTitreEn
    ) {
    }
}
