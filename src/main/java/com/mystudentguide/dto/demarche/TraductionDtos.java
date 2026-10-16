package com.mystudentguide.dto.demarche;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Version anglaise du contenu d'une démarche, saisie par l'admin.
 *
 * Chaque élément (la démarche, une étape, un document, un lien) porte
 * son texte français (lecture seule, pour traduire en regard) et sa
 * traduction. Un texte anglais vide efface la traduction : l'étudiant
 * voit alors le texte français.
 */
public final class TraductionDtos {

    private TraductionDtos() {
    }

    /**
     * @param id          identifiant de l'élément (null pour la démarche elle-même)
     * @param titreFr     titre ou nom français (ignoré à l'enregistrement)
     * @param descriptionFr description française (ignorée à l'enregistrement)
     * @param titre       titre ou nom anglais
     * @param description description anglaise
     */
    public record Element(
            Long id,
            String titreFr,
            String descriptionFr,
            @Size(max = 150, message = "Un titre anglais ne doit pas dépasser 150 caractères.")
            String titre,
            @Size(max = 10000, message = "Une description anglaise ne doit pas dépasser 10000 caractères.")
            String description
    ) {
    }

    public record Traduction(
            @Valid Element demarche,
            List<@Valid Element> etapes,
            List<@Valid Element> documents,
            List<@Valid Element> liens
    ) {
    }
}
