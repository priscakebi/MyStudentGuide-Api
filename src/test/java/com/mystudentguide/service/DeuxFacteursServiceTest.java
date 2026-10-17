package com.mystudentguide.service;

import com.mystudentguide.entity.User;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.UserRepository;
import com.mystudentguide.security.TotpService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/** Activation, connexion et désactivation de la double authentification (horloge fixe). */
@ExtendWith(MockitoExtension.class)
class DeuxFacteursServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private TotpService totp;
    private DeuxFacteursService service;
    private User admin;

    @BeforeEach
    void setUp() {
        totp = new TotpService(Clock.fixed(Instant.ofEpochSecond(1_000_000), ZoneOffset.UTC));
        service = new DeuxFacteursService(userRepository, totp, passwordEncoder);

        admin = new User();
        admin.setEmail("admin@email.com");
        admin.setPassword("HASH");
        admin.setRole(RoleType.ADMIN);

        lenient().when(userRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(admin));
        lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private List<String> activer() {
        DeuxFacteursService.Preparation preparation = service.preparer("admin@email.com");
        return service.activer("admin@email.com", totp.codeActuel(preparation.secret()));
    }

    @Test
    void preparer_neBloquePasEncoreLaConnexion() {
        DeuxFacteursService.Preparation preparation = service.preparer("admin@email.com");

        assertThat(preparation.uri()).contains("secret=" + preparation.secret());
        assertThat(admin.deuxFacteursActive()).isFalse();
        assertThat(service.etat("admin@email.com").active()).isFalse();
    }

    @Test
    void activer_codeFaux_resteDesactivee() {
        service.preparer("admin@email.com");

        assertThatThrownBy(() -> service.activer("admin@email.com", "000000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Code incorrect");
        assertThat(admin.deuxFacteursActive()).isFalse();
    }

    @Test
    void activer_codeCorrect_remetDixCodesDeSecours() {
        List<String> codes = activer();

        assertThat(codes).hasSize(10);
        assertThat(admin.deuxFacteursActive()).isTrue();
        assertThat(admin.getTotpCodesSecours()).doesNotContain(codes.get(0));
        assertThat(service.etat("admin@email.com")).isEqualTo(new DeuxFacteursService.Etat(true, 10));
    }

    @Test
    void connexion_leCodeDActivationNeResertPas() {
        String code = totp.codeActuel(service.preparer("admin@email.com").secret());
        service.activer("admin@email.com", code);

        assertThat(service.verifierCode(admin, code)).isFalse();
    }

    @Test
    void connexion_unCodeDeSecoursNeSertQuUneFois() {
        List<String> codes = activer();

        assertThat(service.verifierCode(admin, codes.get(3))).isTrue();
        assertThat(service.verifierCode(admin, codes.get(3))).isFalse();
        assertThat(service.etat("admin@email.com").codesSecoursRestants()).isEqualTo(9);
    }

    @Test
    void connexion_refuseUnCodeQuandLaDoubleAuthentificationEstDesactivee() {
        service.preparer("admin@email.com");

        assertThat(service.verifierCode(admin, totp.codeActuel(admin.getTotpSecret()))).isFalse();
    }

    @Test
    void desactiver_exigeLeMotDePasseEtUnCode() {
        List<String> codes = activer();
        when(passwordEncoder.matches("faux", "HASH")).thenReturn(false);
        when(passwordEncoder.matches("bon", "HASH")).thenReturn(true);

        assertThatThrownBy(() -> service.desactiver("admin@email.com", "faux", codes.get(0)))
                .hasMessageContaining("mot de passe");
        assertThatThrownBy(() -> service.desactiver("admin@email.com", "bon", "000000"))
                .hasMessageContaining("Code incorrect");
        assertThat(admin.deuxFacteursActive()).isTrue();

        service.desactiver("admin@email.com", "bon", codes.get(0));

        assertThat(admin.deuxFacteursActive()).isFalse();
        assertThat(admin.getTotpSecret()).isNull();
        assertThat(admin.getTotpCodesSecours()).isNull();
    }

    @Test
    void regenererLesCodes_lesAnciensNeMarchentPlus() {
        List<String> anciens = activer();

        List<String> nouveaux = service.regenererCodesSecours("admin@email.com", anciens.get(0));

        assertThat(service.verifierCode(admin, anciens.get(1))).isFalse();
        assertThat(service.verifierCode(admin, nouveaux.get(1))).isTrue();
    }
}
