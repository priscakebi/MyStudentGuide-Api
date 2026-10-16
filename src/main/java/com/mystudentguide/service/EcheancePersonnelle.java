package com.mystudentguide.service;

import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.StudentProfile;

import java.time.LocalDate;

/**
 * Date limite d'une démarche pour un étudiant donné.
 *
 * Si la démarche a une règle d'échéance (ex. « 90 jours après l'arrivée en
 * France ») et que l'étudiant a renseigné la date correspondante dans son
 * profil, la date limite est calculée pour lui. Sinon, c'est la date limite
 * générale de la démarche (éventuellement vide).
 */
public final class EcheancePersonnelle {

    private EcheancePersonnelle() {
    }

    public static LocalDate calculer(Demarche demarche, StudentProfile profil) {
        if (demarche == null) {
            return null;
        }

        LocalDate reference = dateDeReference(demarche, profil);

        if (reference != null && demarche.getEcheanceDecalageJours() != null) {
            return reference.plusDays(demarche.getEcheanceDecalageJours());
        }

        return demarche.getDateLimite();
    }

    /** Date du profil utilisée par la règle de la démarche (null si pas de règle ou date absente). */
    private static LocalDate dateDeReference(Demarche demarche, StudentProfile profil) {
        if (profil == null || demarche.getEcheanceReference() == null) {
            return null;
        }
        return switch (demarche.getEcheanceReference()) {
            case ARRIVEE_EN_FRANCE -> profil.getDateArrivee();
            case FIN_TITRE_SEJOUR -> profil.getDateFinTitreSejour();
        };
    }
}
