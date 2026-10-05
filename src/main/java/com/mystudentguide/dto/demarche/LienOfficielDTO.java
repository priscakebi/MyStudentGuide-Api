package com.mystudentguide.dto.demarche;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO représentant un lien vers une source officielle
 * associé à une démarche administrative.
 *
 * Ce DTO permet au frontend d'afficher les liens permettant
 * à l'étudiant d'accéder directement aux plateformes ou
 * sources officielles liées à la démarche.
 *
 * Exemple :
 *
 * - ANEF
 * - Service-Public.fr
 * - Préfecture
 *
 * Le lien vers la démarche elle-même n'est pas inclus ici,
 * car le LienOfficielDTO sera déjà contenu dans la liste
 * "liensOfficiels" du DemarcheDetailDTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LienOfficielDTO {

    /**
     * Identifiant unique du lien officiel.
     */
    private Long id;

    /**
     * Nom affiché pour identifier la source officielle.
     *
     * Exemple :
     * "ANEF"
     * "Service-Public.fr"
     */
    private String nom;

    /**
     * URL permettant d'accéder à la source officielle.
     */
    private String url;

    /**
     * Description permettant de préciser l'utilité
     * de ce lien pour l'étudiant.
     */
    private String description;
}