package com.mystudentguide.dto.avis;

import com.mystudentguide.entity.Avis;

import java.time.LocalDateTime;

/**
 * Avis tel qu'il est affiché (page d'accueil et administration).
 * Ne contient ni e-mail ni identifiant d'utilisateur.
 */
public record AvisResponse(
        Long id,
        String auteur,
        String demarcheTitre,
        Integer note,
        String commentaire,
        boolean visible,
        LocalDateTime dateCreation
) {

    public static AvisResponse from(Avis avis) {
        return new AvisResponse(
                avis.getId(),
                avis.getAuteur(),
                avis.getDemarcheTitre(),
                avis.getNote(),
                avis.getCommentaire(),
                Boolean.TRUE.equals(avis.getVisible()),
                avis.getDateCreation()
        );
    }
}
