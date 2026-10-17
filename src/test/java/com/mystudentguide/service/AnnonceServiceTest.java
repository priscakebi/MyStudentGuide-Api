package com.mystudentguide.service;

import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.NotificationType;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnonceServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private EmailService emailService;

    private AnnonceService service;

    @BeforeEach
    void setUp() {
        service = new AnnonceService(userRepository, notificationService, emailService, "https://site.fr/");

        User confirme = etudiant("a@mail.com", true);
        User nonConfirme = etudiant("b@mail.com", false);
        when(userRepository.findByRole(RoleType.ETUDIANT)).thenReturn(List.of(confirme, nonConfirme));
    }

    private static User etudiant(String email, boolean confirme) {
        User user = new User();
        user.setEmail(email);
        user.setPrenom("Prénom");
        user.setRole(RoleType.ETUDIANT);
        user.setEmailVerified(confirme);
        return user;
    }

    @Test
    void notificationPourChaqueEtudiant_emailSeulementAuxAdressesConfirmees() {
        AnnonceService.Resultat resultat = service.envoyer("  Fermeture  ", " La préfecture est fermée. ", true);

        assertThat(resultat).isEqualTo(new AnnonceService.Resultat(2, 1));

        ArgumentCaptor<Notification> notification = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService, times(2)).saveNotification(notification.capture());
        assertThat(notification.getValue().getTitre()).isEqualTo("Fermeture");
        assertThat(notification.getValue().getType()).isEqualTo(NotificationType.INFORMATION);

        verify(emailService).envoyerAnnonce("a@mail.com", "Prénom", "Fermeture",
                "La préfecture est fermée.", "https://site.fr");
    }

    @Test
    void sansEmail_aucunEnvoi() {
        AnnonceService.Resultat resultat = service.envoyer("Titre", "Message", false);

        assertThat(resultat.emails()).isZero();
        verify(emailService, never()).envoyerAnnonce(any(), any(), any(), any(), any());
    }
}
