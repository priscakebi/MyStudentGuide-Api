package com.mystudentguide.service;

import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.NotificationType;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Annonce de l'équipe à tous les étudiants (ex. « Fermeture de la préfecture
 * le 11 novembre », « Nouvelle démarche disponible »).
 *
 * Chaque étudiant reçoit une notification dans l'application ; si l'admin le
 * demande, elle est doublée d'un e-mail, uniquement pour les adresses confirmées.
 */
@Service
public class AnnonceService {

    /** Résultat affiché à l'admin après l'envoi. */
    public record Resultat(int notifications, int emails) {
    }

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final String frontendUrl;

    public AnnonceService(
            UserRepository userRepository,
            NotificationService notificationService,
            EmailService emailService,
            @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl
    ) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
    }

    @Transactional
    public Resultat envoyer(String titre, String message, boolean parEmail) {
        String titrePropre = titre.trim();
        String messagePropre = message.trim();

        List<User> etudiants = userRepository.findByRole(RoleType.ETUDIANT);
        int emails = 0;

        for (User etudiant : etudiants) {
            notificationService.saveNotification(Notification.builder()
                    .titre(titrePropre)
                    .message(messagePropre)
                    .type(NotificationType.INFORMATION)
                    .lu(false)
                    .user(etudiant)
                    .build());

            if (parEmail && !Boolean.FALSE.equals(etudiant.getEmailVerified())) {
                emailService.envoyerAnnonce(
                        etudiant.getEmail(), etudiant.getPrenom(), titrePropre, messagePropre, frontendUrl);
                emails++;
            }
        }
        return new Resultat(etudiants.size(), emails);
    }
}
