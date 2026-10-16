package com.mystudentguide.service;

import com.mystudentguide.dto.auth.AuthResponse;
import com.mystudentguide.dto.auth.LoginRequest;
import com.mystudentguide.dto.auth.RegisterRequest;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.UserRepository;
import com.mystudentguide.security.CompteTemporairementBloqueException;
import com.mystudentguide.security.JwtService;
import com.mystudentguide.security.LoginAttemptService;
import com.mystudentguide.validation.ConditionsUtilisation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests unitaires de l'inscription (sans base de données ni contexte Spring). */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private NotificationService notificationService;
    @Mock private EmailService emailService;
    @Mock private LoginAttemptService loginAttemptService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                notificationService,
                emailService,
                loginAttemptService);
    }

    private RegisterRequest request(String email) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("MotDePasse123");
        request.setPrenom("Prisca");
        request.setNom("Kouenin");
        request.setAccepteConditions(true);
        return request;
    }

    @Test
    void register_refuseUnEmailDejaUtilise() {
        when(userRepository.existsByEmail("prisca@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request("prisca@email.com")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("déjà utilisée");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_normaliseLEmailEtCreeUnEtudiantAvecMotDePasseEncode() {
        when(userRepository.existsByEmail("prisca@email.com")).thenReturn(false);
        when(passwordEncoder.encode("MotDePasse123")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findByRole(RoleType.ADMIN)).thenReturn(List.of());
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request("  Prisca@Email.COM "));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());

        assertThat(saved.getValue().getEmail()).isEqualTo("prisca@email.com");
        assertThat(saved.getValue().getPassword()).isEqualTo("HASH");
        assertThat(saved.getValue().getRole()).isEqualTo(RoleType.ETUDIANT);

        // Preuve du consentement (RGPD).
        assertThat(saved.getValue().getConditionsVersion()).isEqualTo(ConditionsUtilisation.VERSION);
        assertThat(saved.getValue().getConditionsAccepteesLe()).isNotNull();

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("ETUDIANT");

        verify(emailService).envoyerConfirmationInscription(anyString(), anyString());
    }


    // ==============================================================
    // CONNEXION : LIMITATION DES TENTATIVES
    // ==============================================================

    private static LoginRequest connexion(String email, String motDePasse) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(motDePasse);
        return request;
    }

    @Test
    void login_compteBloque_leMotDePasseNEstPasTeste() {
        doThrow(new CompteTemporairementBloqueException(600))
                .when(loginAttemptService).verifier("prisca@email.com");

        assertThatThrownBy(() -> authService.login(connexion(" prisca@email.com ", "MotDePasse123!")))
                .isInstanceOf(CompteTemporairementBloqueException.class);

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void login_mauvaisMotDePasse_compteUnEchec() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("mauvais mot de passe"));

        assertThatThrownBy(() -> authService.login(connexion("prisca@email.com", "faux")))
                .isInstanceOf(BadCredentialsException.class);

        verify(loginAttemptService).echec("prisca@email.com");
        verify(loginAttemptService, never()).succes(anyString());
    }

    @Test
    void login_reussi_remetLeCompteurAZero() {
        User user = new User();
        user.setId(1L);
        user.setEmail("prisca@email.com");
        user.setRole(RoleType.ETUDIANT);

        when(userRepository.findByEmail("prisca@email.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(connexion("prisca@email.com", "MotDePasse123!"));

        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(loginAttemptService).succes("prisca@email.com");
        verify(loginAttemptService, never()).echec(anyString());
    }
}
