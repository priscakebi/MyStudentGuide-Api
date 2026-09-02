package com.mystudentguide.controller;

import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.User;
import com.mystudentguide.repository.UserRepository;
import com.mystudentguide.service.NotificationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(
            NotificationService notificationService,
            UserRepository userRepository
    ) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(
            @PathVariable Long userId
    ) {

        List<Notification> notifications =
                notificationService.getNotificationsByUser(userId);

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            @PathVariable Long userId
    ) {

        List<Notification> notifications =
                notificationService.getUnreadNotifications(userId);

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<Long> countUnreadNotifications(
            @PathVariable Long userId
    ) {

        long count =
                notificationService.countUnreadNotifications(userId);

        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(
            @PathVariable Long id
    ) {

        Optional<Notification> notification =
                notificationService.getNotificationById(id);

        return notification
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound().build()
                );
    }

    /**
     * Création d'une notification pour l'utilisateur connecté.
     */
    @PostMapping
    public ResponseEntity<Notification> createNotification(
            @RequestBody Notification notification,
            Authentication authentication
    ) {

        // Récupération de l'email depuis le JWT
        String email = authentication.getName();

        // Recherche de l'utilisateur
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur introuvable avec l'email : " + email
                        )
                );

        // Association de l'utilisateur à la notification
        notification.setUser(user);

        // Sauvegarde
        Notification savedNotification =
                notificationService.saveNotification(notification);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedNotification);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(
            @PathVariable Long id
    ) {

        try {

            Notification notification =
                    notificationService.markAsRead(id);

            return ResponseEntity.ok(notification);

        } catch (RuntimeException exception) {

            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id
    ) {

        Optional<Notification> notification =
                notificationService.getNotificationById(id);

        if (notification.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        notificationService.deleteNotification(id);

        return ResponseEntity.noContent().build();
    }
}