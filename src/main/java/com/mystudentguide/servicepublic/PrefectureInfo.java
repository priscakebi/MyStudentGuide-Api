package com.mystudentguide.servicepublic;

import java.util.List;

/**
 * Préfecture compétente pour la ville d'études d'un étudiant,
 * issue de l'API Annuaire de l'administration (DILA, Licence Ouverte).
 *
 * @param nom            ex. : « Préfecture - Rhône »
 * @param ville          ville d'études reconnue (ex. : « Lyon »)
 * @param departement    code du département (ex. : « 69 »)
 * @param adresse        adresse postale sur une ligne
 * @param telephone      numéro principal (peut être null)
 * @param horaires       ex. : « Lundi au Vendredi : 08:30 – 15:30 »
 * @param rendezVous     lien de prise de rendez-vous (peut être null)
 * @param siteInternet   site de la préfecture (peut être null)
 * @param contact        formulaire de contact (peut être null)
 * @param ficheAnnuaire  fiche sur lannuaire.service-public.gouv.fr
 * @param latitude       pour l'itinéraire (peut être null)
 * @param longitude      pour l'itinéraire (peut être null)
 */
public record PrefectureInfo(
        String nom,
        String ville,
        String departement,
        String adresse,
        String telephone,
        List<String> horaires,
        String rendezVous,
        String siteInternet,
        String contact,
        String ficheAnnuaire,
        String latitude,
        String longitude
) {
}
