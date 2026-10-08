package com.mystudentguide.service;

import com.mystudentguide.entity.PasswordResetToken;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.PasswordResetTokenRepository;
import com.mystudentguide.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

/** Tests du « mot de passe oublié » (sans base de données ni envoi réel d'e-mail). */
@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;

    private PasswordResetService service;
    private User student;
    private User admin;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(
                userRepository, tokenRepository, passwordEncoder, emailService,
                "http://localhost:4200/", 30, 60);

        student = new User();
        student.setId(1L);
        student.setEmail("etudiant@email.com");
        student.setPrenom("Amina");
        student.setRole(RoleType.ETUDIANT);

        admin = new User();
        admin.setId(2L);
        admin.setEmail("admin@email.com");
        admin.setPrenom("Marc");
        admin.setRole(RoleType.ADMIN);
    }

    // ------------------------------------------------------------ demande

    @Test
    void adresseInconnue_neCreeNiJetonNiEmail() {
        when(userRepository.findByEmail("inconnu@email.com")).thenReturn(Optional.empty());

        service.requestReset("inconnu@email.com", "ETUDIANT");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).envoyerLienReinitialisationMotDePasse(
                anyString(), anyString(), anyString(), anyLong(), anyBoolean());
    }

    @Test
    void compteEtudiantDepuisLEspaceAdmin_estIgnore() {
        when(userRepository.findByEmail("etudiant@email.com")).thenReturn(Optional.of(student));

        service.requestReset("etudiant@email.com", "ADMIN");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).envoyerLienReinitialisationMotDePasse(
                anyString(), anyString(), anyString(), anyLong(), anyBoolean());
    }

    @Test
    void compteAdminDepuisLEspaceEtudiant_estIgnore() {
        when(userRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(admin));

        service.requestReset("admin@email.com", null);

        verify(tokenRepository, never()).save(any());
    }

    @Test
    void demandeValide_stockeUneEmpreinteEtEnvoieLeLien() {
        when(userRepository.findByEmail("etudiant@email.com")).thenReturn(Optional.of(student));
        when(tokenRepository.existsByUser_IdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class))).thenReturn(false);

        service.requestReset("  Etudiant@Email.COM ", "ETUDIANT");

        ArgumentCaptor<PasswordResetToken> saved = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(saved.capture());

        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(emailService).envoyerLienReinitialisationMotDePasse(
                eq("etudiant@email.com"), eq("Amina"), link.capture(), eq(30L), eq(false));

        // le lien pointe vers la page étudiant et contient le jeton en clair
        assertThat(link.getValue()).startsWith("http://localhost:4200/reset-password?token=");
        String rawToken = link.getValue().substring(link.getValue().indexOf("token=") + 6);

        // la base ne contient QUE l'empreinte, jamais le jeton lui-même
        assertThat(saved.getValue().getTokenHash())
                .isEqualTo(PasswordResetService.sha256Hex(rawToken))
                .isNotEqualTo(rawToken)
                .hasSize(64);
        assertThat(saved.getValue().getExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(29));
        verify(tokenRepository).deleteByUserId(1L);   // anciens jetons invalidés
    }

    @Test
    void demandeAdmin_envoieLeLienVersLaPageAdmin() {
        when(userRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(admin));

        service.requestReset("admin@email.com", "admin");

        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(emailService).envoyerLienReinitialisationMotDePasse(
                eq("admin@email.com"), eq("Marc"), link.capture(), eq(30L), eq(true));
        assertThat(link.getValue()).startsWith("http://localhost:4200/admin-reset-password?token=");
    }

    @Test
    void demandeTropRapprochee_estIgnoree() {
        when(userRepository.findByEmail("etudiant@email.com")).thenReturn(Optional.of(student));
        when(tokenRepository.existsByUser_IdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class))).thenReturn(true);

        service.requestReset("etudiant@email.com", "ETUDIANT");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).envoyerLienReinitialisationMotDePasse(
                anyString(), anyString(), anyString(), anyLong(), anyBoolean());
    }

    // ---------------------------------------------------- nouveau mot de passe

    private PasswordResetToken tokenFor(String raw, User user, LocalDateTime expiresAt) {
        PasswordResetToken t = new PasswordResetToken();
        t.setTokenHash(PasswordResetService.sha256Hex(raw));
        t.setUser(user);
        t.setExpiresAt(expiresAt);
        return t;
    }

    @Test
    void jetonValide_changeLeMotDePasseEtConsommeLesJetons() {
        PasswordResetToken token = tokenFor("abc", student, LocalDateTime.now().plusMinutes(10));
        when(tokenRepository.findByTokenHash(PasswordResetService.sha256Hex("abc"))).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NouveauMdp123")).thenReturn("HASH");

        service.resetPassword("abc", "NouveauMdp123");

        assertThat(student.getPassword()).isEqualTo("HASH");
        verify(userRepository).save(student);
        verify(tokenRepository).deleteByUserId(1L);
        verify(emailService).envoyerConfirmationModificationMotDePasse("etudiant@email.com", "Amina");
    }

    @Test
    void jetonInconnu_estRefuse() {
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword("faux", "NouveauMdp123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PasswordResetService.LIEN_INVALIDE);

        verify(userRepository, never()).save(any());
    }

    @Test
    void jetonExpire_estRefuseEtSupprime() {
        PasswordResetToken token = tokenFor("old", student, LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByTokenHash(PasswordResetService.sha256Hex("old"))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword("old", "NouveauMdp123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PasswordResetService.LIEN_INVALIDE);

        verify(tokenRepository).delete(token);
        verify(userRepository, never()).save(any());
        verify(emailService, never()).envoyerConfirmationModificationMotDePasse(anyString(), anyString());
    }
}
