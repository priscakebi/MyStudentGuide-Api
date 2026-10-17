package com.mystudentguide.security;

import com.mystudentguide.entity.User;
import com.mystudentguide.enums.RoleType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "une-cle-de-test-d-au-moins-32-caracteres-OK";

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 60_000L);

        user = new User();
        user.setEmail("prisca@email.com");
        user.setRole(RoleType.ETUDIANT);
    }

    @Test
    void unTokenGenereEstValidePourSonUtilisateur() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("prisca@email.com");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void unTokenExpireEstRefuse() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1_000L);
        String token = jwtService.generateToken(user);

        // JJWT lève une exception à la lecture d'un token expiré :
        // le filtre JWT l'attrape et la requête reste non authentifiée.
        assertThatThrownBy(() -> jwtService.isTokenValid(token, user))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    @Test
    void unTokenSigneAvecUneAutreCleEstRefuse() {
        String token = jwtService.generateToken(user);

        JwtService autre = new JwtService();
        ReflectionTestUtils.setField(autre, "secretKey", "une-AUTRE-cle-de-test-de-32-caracteres-min");
        ReflectionTestUtils.setField(autre, "jwtExpiration", 60_000L);

        assertThatThrownBy(() -> autre.extractUsername(token))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    @Test
    void unSecretTropCourtEmpecheLeDemarrage() {
        ReflectionTestUtils.setField(jwtService, "secretKey", "trop-court");

        assertThatThrownBy(() -> jwtService.validateConfiguration())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unDefiDeuxFacteursNOuvrePasDeSession() {
        String defi = jwtService.genererDefiDeuxFacteurs(user);

        assertThat(jwtService.emailDuDefi(defi)).isEqualTo("prisca@email.com");
        assertThat(jwtService.isTokenValid(defi, user)).isFalse();
    }

    @Test
    void unJetonDeSessionNEstPasUnDefi() {
        String token = jwtService.generateToken(user);

        assertThatThrownBy(() -> jwtService.emailDuDefi(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expiré");
        assertThatThrownBy(() -> jwtService.emailDuDefi("n-importe-quoi"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
