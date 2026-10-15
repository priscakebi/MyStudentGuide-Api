package com.mystudentguide.dto.demarche;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Requêtes d'édition des étapes et des liens officiels d'une démarche
 * (/api/admin/demarches/{id}/etapes et /liens).
 */
public final class ContenuDtos {

    private ContenuDtos() {
    }

    public record EtapeRequest(
            @NotBlank(message = "Le titre de l'étape est obligatoire.")
            @Size(max = 150, message = "Le titre de l'étape ne doit pas dépasser 150 caractères.")
            String titre,

            @Size(max = 5000, message = "La description ne doit pas dépasser 5000 caractères.")
            String description
    ) {
    }

    /** Nouvel ordre des étapes : identifiants, de la première à la dernière. */
    public record OrdreRequest(
            @NotEmpty(message = "L'ordre des étapes est vide.")
            List<Long> ids
    ) {
    }

    public record LienRequest(
            @NotBlank(message = "Le nom du lien est obligatoire.")
            @Size(max = 150, message = "Le nom du lien ne doit pas dépasser 150 caractères.")
            String nom,

            @NotBlank(message = "L'adresse du lien est obligatoire.")
            @Size(max = 500, message = "L'adresse ne doit pas dépasser 500 caractères.")
            @Pattern(regexp = "^https?://\\S+$", message = "L'adresse doit commencer par https:// (ou http://).")
            String url,

            @Size(max = 1000, message = "La description ne doit pas dépasser 1000 caractères.")
            String description
    ) {
    }

    /** Étapes et liens d'une démarche, dans l'ordre d'affichage. */
    public record Contenu(List<EtapeDemarcheDTO> etapes, List<LienOfficielDTO> liens) {
    }
}
