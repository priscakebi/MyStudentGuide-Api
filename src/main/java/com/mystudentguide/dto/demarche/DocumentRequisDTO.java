package com.mystudentguide.dto.demarche;

import com.mystudentguide.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO représentant un document nécessaire pour réaliser une démarche.
 *
 * Ce DTO permet d'envoyer au frontend uniquement les informations
 * utiles concernant le document requis.
 *
 * Exemple :
 *
 * {
 *     "id": 1,
 *     "nom": "Passeport",
 *     "description": "Copie des pages d'identité du passeport",
 *     "type": "PASSEPORT"
 * }
 *
 * Le lien vers l'entité Demarche n'est pas envoyé ici car le document
 * est déjà contenu dans la liste "documentsRequis" du
 * DemarcheDetailDTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRequisDTO {

    /**
     * Identifiant unique du document requis.
     */
    private Long id;

    /**
     * Nom du document demandé.
     *
     * Exemple :
     * "Passeport"
     * "Certificat de scolarité"
     */
    private String nom;

    /**
     * Description permettant de préciser ce qui est attendu
     * pour ce document.
     */
    private String description;

    /**
     * Type du document.
     *
     * Exemple :
     * PASSEPORT
     * CERTIFICAT_SCOLARITE
     * JUSTIFICATIF_DOMICILE
     */
    private DocumentType type;
}