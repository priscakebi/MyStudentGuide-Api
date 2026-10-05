package com.mystudentguide.controller;

import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.User;
import com.mystudentguide.repository.UserRepository;
import com.mystudentguide.service.NotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Vérifie qu'un utilisateur ne peut accéder qu'à SES notifications. */
@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock private NotificationService notificationService;
    @Mock private UserRepository userRepository;

    private NotificationController controller;
    private Authentication authAsUser1;

    @BeforeEach
    void setUp() {
        controller = new NotificationController(notificationService, userRepository);

        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("etudiant1@email.com");

        when(userRepository.findByEmail("etudiant1@email.com")).thenReturn(Optional.of(user1));
        authAsUser1 = new UsernamePasswordAuthenticationToken("etudiant1@email.com", null, List.of());
    }

    @Test
    void lireSesPropresNotificationsEstAutorise() {
        when(notificationService.getNotificationsByUser(1L)).thenReturn(List.of());

        assertThat(controller.getNotificationsByUser(1L, authAsUser1).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void lireLesNotificationsDUnAutreUtilisateurEstInterdit() {
        assertThat(controller.getNotificationsByUser(2L, authAsUser1).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(notificationService, never()).getNotificationsByUser(2L);
    }

    @Test
    void supprimerLaNotificationDUnAutreUtilisateurEstInterdit() {
        when(notificationService.getNotificationById(99L))
                .thenReturn(Optional.of(new Notification()));
        when(notificationService.belongsToUser(99L, 1L)).thenReturn(false);

        assertThat(controller.deleteNotification(99L, authAsUser1).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(notificationService, never()).deleteNotification(99L);
    }

    @Test
    void supprimerSaPropreNotificationEstAutorise() {
        when(notificationService.getNotificationById(5L))
                .thenReturn(Optional.of(new Notification()));
        when(notificationService.belongsToUser(5L, 1L)).thenReturn(true);

        assertThat(controller.deleteNotification(5L, authAsUser1).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);

        verify(notificationService).deleteNotification(5L);
    }

    @Test
    void creerUneNotificationIgnoreLIdentifiantEnvoye() {
        Notification envoyee = new Notification();
        envoyee.setId(42L); // tentative d'écraser la notification n°42 d'un autre utilisateur
        when(notificationService.saveNotification(envoyee)).thenReturn(envoyee);

        controller.createNotification(envoyee, authAsUser1);

        assertThat(envoyee.getId()).isNull();
    }
}
