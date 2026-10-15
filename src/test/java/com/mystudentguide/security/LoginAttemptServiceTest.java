package com.mystudentguide.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAttemptServiceTest {

    /** Horloge qu'on avance à la main. */
    private static final class HorlogeTest extends Clock {
        private Instant maintenant = Instant.parse("2026-10-15T10:00:00Z");

        void avancer(Duration duree) {
            maintenant = maintenant.plus(duree);
        }

        @Override public Instant instant() { return maintenant; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
    }

    private HorlogeTest horloge;
    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        horloge = new HorlogeTest();
        service = new LoginAttemptService(5, Duration.ofMinutes(15), Duration.ofMinutes(15), horloge);
    }

    private void echouer(String email, int fois) {
        for (int i = 0; i < fois; i++) {
            service.echec(email);
        }
    }

    @Test
    void quatreEchecsNeBloquentPas() {
        echouer("ana@free.fr", 4);
        assertThatCode(() -> service.verifier("ana@free.fr")).doesNotThrowAnyException();
    }

    @Test
    void cinqEchecsBloquentQuinzeMinutes() {
        echouer("ana@free.fr", 5);

        assertThatThrownBy(() -> service.verifier("ana@free.fr"))
                .isInstanceOf(CompteTemporairementBloqueException.class)
                .hasMessageContaining("15 minutes");

        horloge.avancer(Duration.ofMinutes(14));
        assertThatThrownBy(() -> service.verifier("ana@free.fr"))
                .isInstanceOf(CompteTemporairementBloqueException.class)
                .hasMessageContaining("1 minute ");

        horloge.avancer(Duration.ofMinutes(1));
        assertThatCode(() -> service.verifier("ana@free.fr")).doesNotThrowAnyException();
    }

    @Test
    void lAdresseEstInsensibleALaCasseEtAuxEspaces() {
        echouer("Ana@Free.fr ", 5);
        assertThatThrownBy(() -> service.verifier("ana@free.fr"))
                .isInstanceOf(CompteTemporairementBloqueException.class);
    }

    @Test
    void lesEchecsAnciensNeComptentPlus() {
        echouer("ana@free.fr", 4);
        horloge.avancer(Duration.ofMinutes(16));
        echouer("ana@free.fr", 1);

        assertThatCode(() -> service.verifier("ana@free.fr")).doesNotThrowAnyException();
    }

    @Test
    void uneConnexionReussieRemetLeCompteurAZero() {
        echouer("ana@free.fr", 4);
        service.succes("ana@free.fr");
        echouer("ana@free.fr", 4);

        assertThatCode(() -> service.verifier("ana@free.fr")).doesNotThrowAnyException();
    }

    @Test
    void apresUnBlocageUneNouvelleSerieRecommence() {
        echouer("ana@free.fr", 5);
        horloge.avancer(Duration.ofMinutes(15));

        // Blocage fini : un seul nouvel échec ne rebloque pas.
        echouer("ana@free.fr", 1);
        assertThatCode(() -> service.verifier("ana@free.fr")).doesNotThrowAnyException();
    }

    @Test
    void unCompteBloqueNeGenePasLesAutres() {
        echouer("ana@free.fr", 5);
        assertThatCode(() -> service.verifier("kenji@free.fr")).doesNotThrowAnyException();
    }

    @Test
    void leNettoyageSupprimeLesEntreesExpirees() {
        echouer("ana@free.fr", 5);
        echouer("kenji@free.fr", 2);

        service.nettoyer();
        assertThat(service.taille()).isEqualTo(2);

        horloge.avancer(Duration.ofMinutes(16));
        service.nettoyer();
        assertThat(service.taille()).isZero();
    }
}
