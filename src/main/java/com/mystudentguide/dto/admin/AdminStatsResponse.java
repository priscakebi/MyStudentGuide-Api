package com.mystudentguide.dto.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Statistiques du tableau de bord administrateur.
 *
 * GET /api/admin/stats
 *
 * Tout est calculé côté API en une seule requête : le front
 * n'a plus besoin de télécharger les listes complètes
 * (utilisateurs, démarches...) juste pour les compter.
 */
public record AdminStatsResponse(

        /** Nombre d'étudiants inscrits. */
        long etudiants,

        /** Étudiants inscrits au cours des 7 derniers jours. */
        long nouveauxEtudiants7j,

        /** Étudiants qui n'ont encore commencé aucune démarche. */
        long etudiantsSansDemarche,

        long demarches,
        long categories,

        /** Suivis en cours / terminés (toutes démarches confondues). */
        long suivisEnCours,
        long suivisTermines,

        /** Terminés / (en cours + terminés), en %, arrondi. */
        int tauxCompletion,

        long avis,

        /** Note moyenne des avis (sur 5), ou null s'il n'y a aucun avis. */
        Double noteMoyenne,

        /** Les 5 démarches les plus commencées. */
        List<TopDemarche> topDemarches,

        /** Inscriptions d'étudiants des 8 dernières semaines (de la plus ancienne à la plus récente). */
        List<InscriptionsSemaine> inscriptionsParSemaine,

        /** Dernières notifications reçues par l'administrateur connecté. */
        List<Activite> activiteRecente,

        /** Signalements d'erreurs envoyés par les étudiants, pas encore traités. */
        long signalementsOuverts
) {

    public record TopDemarche(Long id, String titre, long commencees, long terminees) {
    }

    /** debutSemaine : lundi de la semaine. */
    public record InscriptionsSemaine(LocalDate debutSemaine, long inscriptions) {
    }

    public record Activite(String titre, String message, LocalDateTime date, boolean lu) {
    }
}
