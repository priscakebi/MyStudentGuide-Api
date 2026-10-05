package com.mystudentguide.dto.demarche;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO représentant une étape d'une démarche administrative.
 *
 * Ce DTO permet d'envoyer au frontend les informations nécessaires
 * pour afficher les différentes étapes à suivre par l'étudiant.
 *
 * Exemple :
 *
 * 1. Préparer les documents
 * 2. Déposer la demande en ligne
 * 3. Suivre l'avancement de la demande
 * 4. Récupérer le titre
 *
 * L'étape ne contient pas directement la démarche associée.
 * Elle sera déjà incluse dans la liste "etapes" du
 * DemarcheDetailDTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapeDemarcheDTO {

    /**
     * Identifiant unique de l'étape.
     */
    private Long id;

    /**
     * Numéro permettant de déterminer l'ordre
     * dans lequel l'étape doit être réalisée.
     *
     * Exemple :
     * 1, 2, 3, 4...
     */
    private Integer numero;

    /**
     * Titre court de l'étape.
     *
     * Exemple :
     * "Préparer les documents"
     */
    private String titre;

    /**
     * Description détaillée de ce qu'il faut faire
     * pendant cette étape.
     */
    private String description;
}