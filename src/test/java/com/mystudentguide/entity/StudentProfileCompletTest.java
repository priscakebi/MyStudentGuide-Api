package com.mystudentguide.entity;

import com.mystudentguide.enums.LogementType;
import com.mystudentguide.enums.VisaType;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** « Profil complet » : calculé d'après les champs obligatoires, jamais lu du client. */
class StudentProfileCompletTest {

    private static StudentProfile complet() {
        StudentProfile p = new StudentProfile();
        p.setNationalite("Sénégalaise");
        p.setTypeVisa(VisaType.VLS_TS_ETUDIANT);
        p.setPremiereArrivee(false);
        p.setUniversite("Université Lyon 2");
        p.setNiveauEtudes("Master 1");
        p.setVilleEtudes("Lyon");
        p.setTelephone("+33612345678");
        p.setTypeLogement(LogementType.values()[0]);
        p.setAdresse("10 rue de la République 69002 Lyon");
        return p;
    }

    @Test
    void profilRempli_estComplet() {
        assertThat(complet().getProfileCompleted()).isTrue();
    }

    @Test
    void unChampObligatoireVide_nEstPasComplet() {
        StudentProfile p = complet();
        p.setAdresse("   ");
        assertThat(p.getProfileCompleted()).isFalse();

        StudentProfile q = complet();
        q.setPremiereArrivee(null);
        assertThat(q.getProfileCompleted()).isFalse();
    }

    @Test
    void laValeurEnvoyeeParLeClientEstIgnoree() {
        StudentProfile vide = new StudentProfile();
        vide.setProfileCompleted(true);
        assertThat(vide.getProfileCompleted()).isFalse();

        StudentProfile p = complet();
        p.setProfileCompleted(false);
        assertThat(p.getProfileCompleted()).isTrue();
    }
}
