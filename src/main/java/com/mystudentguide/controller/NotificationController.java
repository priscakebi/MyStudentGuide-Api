package com.mystudentguide.controller;

import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.User;
import com.mystudentguide.repository.UserRepository;
import com.mystudentguide.service.NotificationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller des notifications internes.
 *
 * SÉCURITÉ : une notification est strictement personnelle.
 * Un utilisateur connecté ne peut lire, modifier ou supprimer
 * QUE ses propres notifications. Toute tentative d'accès à la
 * notification d'un autre utilisateur renvoie 403.
 *
 * (Avant, n'importe quel utilisateur connecté pouvait lire ou supprimer
 * les notifications des autres en changeant l'identifiant dans l'URL.)
 */
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

    /** Toutes les notifications d'un utilisateur (uniquement les siennes). */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        if (!isCurrentUser(userId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                notificationService.getNotificationsByUser(userId));
    }

    /** Notifications non lues d'un utilisateur (uniquement les siennes). */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        if (!isCurrentUser(userId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(userId));
    }

    /** Nombre de notifications non lues (uniquement les siennes). */
    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<Long> countUnreadNotifications(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        if (!isCurrentUser(userId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                notificationService.countUnreadNotifications(userId));
    }

    /**
     * GET /api/notifications/admin/all
     *
     * Notifications de l'administrateur connecté (compteur du
     * tableau de bord admin). Cet endpoint était appelé par le front
     * mais n'existait pas.
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Notification>> getAdminNotifications(
            Authentication authentication
    ) {
        User admin = getCurrentUser(authentication);

        return ResponseEntity.ok(
                notificationService.getNotificationsByUser(admin.getId()));
    }

    /** Une notification par identifiant (uniquement la sienne). */
    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Optional<Notification> notification =
                notificationService.getNotificationById(id);

        if (notification.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!ownsNotification(id, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(notification.get());
    }

    /**
     * Crée une notification pour l'utilisateur connecté.
     *
     * L'identifiant envoyé par le client est ignoré : sans cela, envoyer
     * un "id" existant aurait écrasé la notification d'un autre utilisateur.
     */
    @PostMapping
    public ResponseEntity<Notification> createNotification(
            @RequestBody Notification notification,
            Authentication authentication
    ) {
        User user = getCurrentUser(authentication);

        notification.setId(null);
        notification.setUser(user);

        Notification savedNotification =
                notificationService.saveNotification(notification);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedNotification);
    }

    /**
     * PATCH /api/notifications/read-all
     *
     * « Tout marquer comme lu » : toutes les notifications de l'utilisateur connecté.
     * Réponse : { "marquees": 3 }
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(Map.of("marquees", notificationService.markAllAsRead(user.getId())));
    }

    /** Marque SA notification comme lue. */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (notificationService.getNotificationById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!ownsNotification(id, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    /** Supprime SA notification. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (notificationService.getNotificationById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!ownsNotification(id, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        notificationService.deleteNotification(id);

        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Méthodes privées
    // ------------------------------------------------------------------

    /** Utilisateur correspondant au JWT de la requête. */
    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(
                        "Utilisateur connecté introuvable"));
    }

    /** Vrai si l'identifiant demandé est celui de l'utilisateur connecté. */
    private boolean isCurrentUser(Long userId, Authentication authentication) {
        return getCurrentUser(authentication).getId().equals(userId);
    }

    /** Vrai si la notification appartient à l'utilisateur connecté. */
    private boolean ownsNotification(Long notificationId, Authentication authentication) {
        Long currentUserId = getCurrentUser(authentication).getId();
        return notificationService.belongsToUser(notificationId, currentUserId);
    }
}
