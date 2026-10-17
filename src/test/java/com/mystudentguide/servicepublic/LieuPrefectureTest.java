package com.mystudentguide.servicepublic;

import com.mystudentguide.servicepublic.LieuPrefecture.Base;
import com.mystudentguide.servicepublic.LieuPrefecture.Lieu;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LieuPrefectureTest {

    @Test
    void extraitVilleEtCodePostalDeLAdresse() {
        // Adresse choisie dans les suggestions (Base Adresse Nationale).
        assertThat(LieuPrefecture.depuisAdresse("12 Rue de la République 93200 Saint-Denis"))
                .isEqualTo("Saint-Denis 93200");
        // Adresse saisie à la main.
        assertThat(LieuPrefecture.depuisAdresse("5 rue Bancel, 69007 Lyon")).isEqualTo("Lyon 69007");
        assertThat(LieuPrefecture.depuisAdresse("5 rue Bancel 69007 Lyon, France")).isEqualTo("Lyon 69007");
    }

    @Test
    void seContenteDuCodePostalQuandLaVilleNEstPasSimple() {
        assertThat(LieuPrefecture.depuisAdresse("3 allée X 75013 Paris 13e")).isEqualTo("75013");
        assertThat(LieuPrefecture.depuisAdresse("Résidence Y, 38000")).isEqualTo("38000");
    }

    @Test
    void ignoreUneAdresseSansCodePostal() {
        assertThat(LieuPrefecture.depuisAdresse("5 rue Bancel Lyon")).isNull();
        assertThat(LieuPrefecture.depuisAdresse(null)).isNull();
    }

    @Test
    void essaieLeDomicileAvantLaVilleDEtudes() {
        assertThat(LieuPrefecture.candidats("12 Rue de la République 93200 Saint-Denis", "Paris 75005"))
                .containsExactly(new Lieu("Saint-Denis 93200", Base.ADRESSE), new Lieu("Paris 75005", Base.VILLE_ETUDES));

        assertThat(LieuPrefecture.candidats("", "Lyon"))
                .containsExactly(new Lieu("Lyon", Base.VILLE_ETUDES));

        assertThat(LieuPrefecture.candidats(null, " ")).isEmpty();
    }
}
