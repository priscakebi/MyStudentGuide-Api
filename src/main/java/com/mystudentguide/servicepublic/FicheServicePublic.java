package com.mystudentguide.servicepublic;

import java.time.LocalDate;
import java.util.List;

/**
 * Contenu utile d'une fiche pratique Service-Public.gouv.fr,
 * extrait du flux open data de la DILA (Licence Ouverte).
 *
 * @param id          identifiant de la fiche (ex. : F2231)
 * @param titre       titre de la fiche
 * @param description résumé + introduction de la fiche
 * @param url         adresse de la fiche sur service-public.gouv.fr
 * @param dateMaj     date de la dernière modification importante
 * @param theme       thème officiel (ex. : « Social - Santé »)
 * @param etapes      sections de la fiche (« Comment valider le VLS-TS ? »...)
 * @param liens       téléservices, sites officiels et « où s'adresser »
 */
public record FicheServicePublic(
        String id,
        String titre,
        String description,
        String url,
        LocalDate dateMaj,
        String theme,
        List<Etape> etapes,
        List<Lien> liens
) {

    public record Etape(String titre, String description) {
    }

    public record Lien(String nom, String url, String description) {
    }
}
