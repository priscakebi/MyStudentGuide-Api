package com.mystudentguide.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormRulesTest {

    @Test
    void motDePasseConformeAccepte() {
        assertThat(FormRules.isStrongPassword("Etudiant2026!")).isTrue();
        assertThat(FormRules.isStrongPassword("Àbcdefghij1#")).isTrue();
    }

    @Test
    void motDePasseTropCourtRefuse() {
        // 11 caractères.
        assertThat(FormRules.isStrongPassword("Etudiant26!")).isFalse();
    }

    @Test
    void motDePasseSansCategorieRefuse() {
        assertThat(FormRules.isStrongPassword("etudiant2026!")).isFalse(); // pas de majuscule
        assertThat(FormRules.isStrongPassword("ETUDIANT2026!")).isFalse(); // pas de minuscule
        assertThat(FormRules.isStrongPassword("Etudiantdeux!")).isFalse(); // pas de chiffre
        assertThat(FormRules.isStrongPassword("Etudiant2026a")).isFalse(); // pas de caractère spécial
    }

    @Test
    void motDePasseTropLongRefuse() {
        assertThat(FormRules.isStrongPassword("Aa1!" + "x".repeat(69))).isFalse();
        assertThat(FormRules.isStrongPassword(null)).isFalse();
    }

    @Test
    void emailEtNomValides() {
        assertThat("ana@free.fr".matches(FormRules.EMAIL_REGEX)).isTrue();
        assertThat("ana@localhost".matches(FormRules.EMAIL_REGEX)).isFalse();
        assertThat("Jean-Pierre".matches(FormRules.NAME_REGEX)).isTrue();
        assertThat("N'Guessan".matches(FormRules.NAME_REGEX)).isTrue();
        assertThat("Élodie".matches(FormRules.NAME_REGEX)).isTrue();
        assertThat("J".matches(FormRules.NAME_REGEX)).isFalse();
        assertThat("Jean3".matches(FormRules.NAME_REGEX)).isFalse();
    }
}
