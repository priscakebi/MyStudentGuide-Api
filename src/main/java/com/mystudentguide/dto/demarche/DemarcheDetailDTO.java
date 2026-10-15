package com.mystudentguide.dto.demarche;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO utilisé pour retourner le détail complet d'une démarche.
 *
 * Contrairement à l'entité Demarche, ce DTO permet de contrôler
 * précisément les informations envoyées au frontend.
 *
 * Lorsqu'un étudiant clique sur "Voir plus", il pourra notamment
 * recevoir :
 *
 * - les informations générales de la démarche ;
 * - la catégorie associée ;
 * - les documents requis ;
 * - les différentes étapes ;
 * - les liens vers les plateformes officielles.
 *
 * Ce DTO évite également d'exposer directement les relations
 * entre les entités JPA et permet de préparer une réponse API
 * adaptée aux besoins du frontend Angular.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemarcheDetailDTO {

    /**
     * Identifiant unique de la démarche.
     */
    private Long id;

    /**
     * Titre de la démarche.
     *
     * Exemple :
     * "Renouvellement du titre de séjour étudiant"
     */
    private String titre;

    /**
     * Description générale de la démarche.
     */
    private String description;

    /**
     * Date de création de la démarche dans MyStudentGuide.
     */
    private LocalDate dateCreation;

    /**
     * Date limite éventuelle associée à la démarche.
     */
    private LocalDate dateLimite;

    /**
     * Nom de la catégorie à laquelle appartient la démarche.
     *
     * Exemple :
     * "Titre de séjour"
     */
    private String categorie;

    /**
     * Liste des documents nécessaires pour réaliser la démarche.
     *
     * Cette liste sera alimentée à partir des DocumentRequis.
     */
    private List<DocumentRequisDTO> documentsRequis;

    /**
     * Liste des différentes étapes à suivre.
     *
     * Cette liste sera alimentée à partir des EtapeDemarche.
     */
    private List<EtapeDemarcheDTO> etapes;

    /**
     * Liste des liens vers les sources ou plateformes officielles.
     *
     * Cette liste sera alimentée à partir des LienOfficiel.
     */
    private List<LienOfficielDTO> liensOfficiels;

    /**
     * Fiche Service-Public d'origine (null si la démarche a été saisie
     * à la main). La mention de la source et de sa date de mise à jour
     * est obligatoire (Licence Ouverte).
     */
    private String sourceUrl;
    private LocalDate sourceDateMaj;
}