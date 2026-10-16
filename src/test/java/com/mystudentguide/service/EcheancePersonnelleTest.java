package com.mystudentguide.service;

import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.StudentDemarche;
import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.enums.ReferenceEcheance;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date limite personnelle d'une démarche, calculée depuis le profil de l'étudiant.
 */
class EcheancePersonnelleTest {

    private Demarche demarche(ReferenceEcheance reference, Integer decalage, LocalDate dateGenerale) {
        Demarche d = new Demarche();
        d.setEcheanceReference(reference);
        d.setEcheanceDecalageJours(decalage);
        d.setDateLimite(dateGenerale);
        return d;
    }

    private StudentProfile profil(LocalDate arrivee, LocalDate finTitre) {
        StudentProfile p = new StudentProfile();
        p.setDateArrivee(arrivee);
        p.setDateFinTitreSejour(finTitre);
        return p;
    }

    @Test
    void validationVlsTs_90JoursApresLArrivee() {
        LocalDate date = EcheancePersonnelle.calculer(
                demarche(ReferenceEcheance.ARRIVEE_EN_FRANCE, 90, null),
                profil(LocalDate.of(2026, 9, 1), null));

        assertThat(date).isEqualTo(LocalDate.of(2026, 11, 30));
    }

    @Test
    void renouvellementTitreDeSejour_60JoursAvantLaFin() {
        LocalDate date = EcheancePersonnelle.calculer(
                demarche(ReferenceEcheance.FIN_TITRE_SEJOUR, -60, null),
                profil(null, LocalDate.of(2027, 8, 31)));

        assertThat(date).isEqualTo(LocalDate.of(2027, 7, 2));
    }

    @Test
    void dateDuProfilAbsente_laDateGeneraleSApplique() {
        LocalDate generale = LocalDate.of(2026, 12, 15);

        LocalDate date = EcheancePersonnelle.calculer(
                demarche(ReferenceEcheance.ARRIVEE_EN_FRANCE, 90, generale),
                profil(null, null));

        assertThat(date).isEqualTo(generale);
    }

    @Test
    void demarcheSansRegle_laDateGeneraleSApplique() {
        LocalDate generale = LocalDate.of(2026, 12, 15);

        LocalDate date = EcheancePersonnelle.calculer(
                demarche(null, null, generale),
                profil(LocalDate.of(2026, 9, 1), null));

        assertThat(date).isEqualTo(generale);
    }

    @Test
    void leSuiviExposeLaDatePersonnelle() {
        StudentDemarche suivi = new StudentDemarche();
        suivi.setDemarche(demarche(ReferenceEcheance.ARRIVEE_EN_FRANCE, 90, null));
        suivi.setStudentProfile(profil(LocalDate.of(2026, 9, 1), null));

        assertThat(suivi.getDateLimitePersonnelle()).isEqualTo(LocalDate.of(2026, 11, 30));
    }
}
