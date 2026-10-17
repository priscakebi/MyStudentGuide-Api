package com.mystudentguide.service;

import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.NotificationType;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.StudentProfileRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RappelTitreSejourServiceTest {

    private static final LocalDate AUJOURDHUI = LocalDate.of(2026, 10, 17);

    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private NotificationService notificationService;
    @Mock private EmailService emailService;

    private RappelTitreSejourService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AUJOURDHUI.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new RappelTitreSejourService(
                studentProfileRepository, notificationService, emailService, "https://site.fr/", clock);
    }

    private StudentProfile profil(LocalDate fin, boolean emailConfirme) {
        User user = new User();
        user.setId(1L);
        user.setEmail("aicha@mail.com");
        user.setPrenom("Aïcha");
        user.setRole(RoleType.ETUDIANT);
        user.setEmailVerified(emailConfirme);

        StudentProfile profil = new StudentProfile();
        profil.setUser(user);
        profil.setDateFinTitreSejour(fin);
        when(studentProfileRepository.findByDateFinTitreSejourBetween(
                AUJOURDHUI.minusDays(30), AUJOURDHUI.plusDays(120))).thenReturn(List.of(profil));
        return profil;
    }

    @Test
    void palier_leplusProcheContenantLesJoursRestants() {
        assertThat(RappelTitreSejourService.palier(100)).isEqualTo(120);
        assertThat(RappelTitreSejourService.palier(60)).isEqualTo(60);
        assertThat(RappelTitreSejourService.palier(20)).isEqualTo(30);
        assertThat(RappelTitreSejourService.palier(0)).isEqualTo(7);
    }

    @Test
    void aMoinsDe30Jours_notificationEtEmail() {
        profil(AUJOURDHUI.plusDays(20), true);

        assertThat(service.envoyerRappels()).isEqualTo(1);

        ArgumentCaptor<Notification> notification = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).saveNotification(notification.capture());
        assertThat(notification.getValue().getTitre()).isEqualTo("Titre de séjour : moins de 30 jours");
        assertThat(notification.getValue().getMessage()).contains("06/11/2026").contains("sans attendre");
        assertThat(notification.getValue().getType()).isEqualTo(NotificationType.ECHEANCE);
        verify(emailService).envoyerRappelTitreSejour(eq("aicha@mail.com"), eq("Aïcha"),
                eq("Titre de séjour : moins de 30 jours"), anyString(), eq("https://site.fr/dashboard/profile"));
    }

    @Test
    void dejaEnvoye_aucunDoublon() {
        profil(AUJOURDHUI.plusDays(100), true);
        when(notificationService.existsNotification(eq(1L), eq(NotificationType.ECHEANCE), anyString()))
                .thenReturn(true);

        assertThat(service.envoyerRappels()).isZero();

        verify(notificationService, never()).saveNotification(any());
        verify(emailService, never()).envoyerRappelTitreSejour(any(), any(), any(), any(), any());
    }

    @Test
    void adresseNonConfirmee_notificationSeulement() {
        profil(AUJOURDHUI.plusDays(90), false);

        service.envoyerRappels();

        verify(notificationService).saveNotification(any());
        verify(emailService, never()).envoyerRappelTitreSejour(any(), any(), any(), any(), any());
    }

    @Test
    void titreExpire_messageDedie() {
        profil(AUJOURDHUI.minusDays(3), true);

        service.envoyerRappels();

        ArgumentCaptor<Notification> notification = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).saveNotification(notification.capture());
        assertThat(notification.getValue().getTitre()).isEqualTo("Titre de séjour expiré");
        assertThat(notification.getValue().getMessage()).contains("14/10/2026");
    }
}
