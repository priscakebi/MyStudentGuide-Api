package com.mystudentguide.service;

import com.mystudentguide.entity.EmailVerificationToken;
import com.mystudentguide.entity.User;
import com.mystudentguide.repository.EmailVerificationTokenRepository;
import com.mystudentguide.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EmailVerificationTokenRepository tokenRepository;
    @Mock private EmailService emailService;

    private EmailVerificationService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationService(userRepository, tokenRepository, emailService,
                "https://mystudentguide.fr/", 48, 60);
        user = new User();
        user.setId(7L);
        user.setEmail("aicha@mail.com");
        user.setPrenom("Aïcha");
        user.setEmailVerified(false);
    }

    @Test
    void envoieUnLienDontSeuleLEmpreinteEstStockee() {
        service.envoyerLien(user, true);

        ArgumentCaptor<EmailVerificationToken> token = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).deleteByUserId(7L);
        verify(tokenRepository).save(token.capture());

        ArgumentCaptor<String> lien = ArgumentCaptor.forClass(String.class);
        verify(emailService).envoyerLienConfirmationEmail(
                eq("aicha@mail.com"), eq("Aïcha"), lien.capture(), eq(48L), eq(true));

        assertThat(lien.getValue()).startsWith("https://mystudentguide.fr/verifier-email?token=");
        String jeton = lien.getValue().substring(lien.getValue().indexOf('=') + 1);
        assertThat(token.getValue().getTokenHash())
                .isEqualTo(PasswordResetService.sha256Hex(jeton))
                .isNotEqualTo(jeton);
        assertThat(token.getValue().getEmail()).isEqualTo("aicha@mail.com");
        assertThat(token.getValue().getExpiresAt()).isAfter(LocalDateTime.now().plusHours(47));
    }

    @Test
    void confirmeLAdresseAvecUnLienValide() {
        when(tokenRepository.findByTokenHash(PasswordResetService.sha256Hex("abc")))
                .thenReturn(Optional.of(jeton("aicha@mail.com", LocalDateTime.now().plusHours(1))));

        service.confirmer(" abc ");

        assertThat(user.getEmailVerified()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).deleteByUserId(7L);
    }

    @Test
    void refuseUnLienExpireOuEnvoyeAUneAncienneAdresse() {
        when(tokenRepository.findByTokenHash(PasswordResetService.sha256Hex("vieux")))
                .thenReturn(Optional.of(jeton("aicha@mail.com", LocalDateTime.now().minusMinutes(1))));
        when(tokenRepository.findByTokenHash(PasswordResetService.sha256Hex("ancienne")))
                .thenReturn(Optional.of(jeton("ancienne@mail.com", LocalDateTime.now().plusHours(1))));

        assertThatThrownBy(() -> service.confirmer("vieux")).hasMessage(EmailVerificationService.LIEN_INVALIDE);
        assertThatThrownBy(() -> service.confirmer("ancienne")).hasMessage(EmailVerificationService.LIEN_INVALIDE);
        assertThatThrownBy(() -> service.confirmer("inconnu")).hasMessage(EmailVerificationService.LIEN_INVALIDE);
        assertThat(user.getEmailVerified()).isFalse();
    }

    @Test
    void renvoieLeLienUneFoisParMinuteAuPlus() {
        when(userRepository.findByEmail("aicha@mail.com")).thenReturn(Optional.of(user));
        when(tokenRepository.existsByUser_IdAndCreatedAtAfter(eq(7L), any())).thenReturn(false, true);

        service.renvoyer("aicha@mail.com");
        verify(emailService).envoyerLienConfirmationEmail(anyString(), anyString(), anyString(), anyLong(), eq(false));

        assertThatThrownBy(() -> service.renvoyer("aicha@mail.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Patientez");
    }

    @Test
    void neRenvoiePasDeLienAUneAdresseDejaConfirmee() {
        user.setEmailVerified(true);
        when(userRepository.findByEmail("aicha@mail.com")).thenReturn(Optional.of(user));

        service.renvoyer("aicha@mail.com");

        verify(emailService, never()).envoyerLienConfirmationEmail(anyString(), anyString(), anyString(), anyLong(), anyBoolean());
    }

    private EmailVerificationToken jeton(String email, LocalDateTime expiration) {
        EmailVerificationToken t = new EmailVerificationToken();
        t.setUser(user);
        t.setEmail(email);
        t.setExpiresAt(expiration);
        return t;
    }
}
