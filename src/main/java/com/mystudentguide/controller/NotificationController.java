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
import java.util.Optional;

/**
 * Controller des notifications internes.
 *
 * SÉCURITÉ :
 * - les notifications d'un étudiant sont personnelles ;
 * - un étudiant ne peut consulter, modifier ou supprimer
 *   que ses propres notifications ;
 * - les administrateurs disposent d'une route spécifique
 *   permettant de consulter toutes les notifications de la plateforme.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * Constructeur permettant d'injecter les dépendances nécessaires.
     *
     * @param notificationService service de gestion des notifications
     * @param userRepository repository permettant de retrouver l'utilisateur connecté
     */
    public NotificationController(
            NotificationService notificationService,
            UserRepository userRepository
    ) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    // ============================================================
    // ADMINISTRATION
    // ============================================================

    /**
     * Récupère toutes les notifications de la plateforme.
     *
     * Endpoint :
     *
     * GET /api/notifications/admin/all
     *
     * Cette route est exclusivement destinée au tableau de bord
     * administrateur.
     *
     * @PreAuthorize("hasRole('ADMIN')") vérifie que l'utilisateur
     * possède le rôle ADMIN avant d'exécuter la méthode.
     *
     * Un étudiant authentifié recevra donc une réponse 403 Forbidden.
     *
     * @return liste de toutes les notifications
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Notification>> getAllNotifications() {

        // Récupération de toutes les notifications
        // depuis la base de données.
        return ResponseEntity.ok(
                notificationService.getAllNotifications()
        );
    }

    // ============================================================
    // NOTIFICATIONS DE L'UTILISATEUR CONNECTÉ
    // ============================================================

    /**
     * Récupère toutes les notifications d'un utilisateur.
     *
     * L'utilisateur ne peut récupérer que ses propres notifications.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(
            @PathVariable Long userId,
            Authentication authentication
    ) {

        // Vérifie que l'identifiant demandé correspond
        // bien à l'utilisateur connecté.
        if (!isCurrentUser(userId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                notificationService.getNotificationsByUser(userId)
        );
    }

    /**
     * Récupère uniquement les notifications non lues
     * de l'utilisateur connecté.
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            @PathVariable Long userId,
            Authentication authentication
    ) {

        // Empêche un utilisateur de consulter
        // les notifications non lues d'un autre compte.
        if (!isCurrentUser(userId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(userId)
        );
    }

    /**
     * Compte les notifications non lues de l'utilisateur connecté.
     */
    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<Long> countUnreadNotifications(
            @PathVariable Long userId,
            Authentication authentication
    ) {

        // Vérifie que l'utilisateur demande son propre compteur.
        if (!isCurrentUser(userId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                notificationService.countUnreadNotifications(userId)
        );
    }

    /**
     * Récupère une notification à partir de son identifiant.
     *
     * L'utilisateur ne peut consulter que ses propres notifications.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        // Recherche de la notification.
        Optional<Notification> notification =
                notificationService.getNotificationById(id);

        // Si elle n'existe pas, on retourne 404.
        if (notification.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Vérifie que la notification appartient
        // à l'utilisateur actuellement connecté.
        if (!ownsNotification(id, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(notification.get());
    }

    /**
     * Crée une notification pour l'utilisateur connecté.
     */
    @PostMapping
    public ResponseEntity<Notification> createNotification(
            @RequestBody Notification notification,
            Authentication authentication
    ) {

        // Récupération de l'utilisateur associé au JWT.
        User user = getCurrentUser(authentication);

        // L'identifiant fourni par le frontend est ignoré.
        // La base générera un nouvel identifiant.
        notification.setId(null);

        // La notification est obligatoirement associée
        // à l'utilisateur authentifié.
        notification.setUser(user);

        Notification savedNotification =
                notificationService.saveNotification(notification);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedNotification);
    }

    /**
     * Marque une notification comme lue.
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {

        // Vérifie que la notification existe.
        if (notificationService.getNotificationById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Vérifie qu'elle appartient à l'utilisateur connecté.
        if (!ownsNotification(id, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                notificationService.markAsRead(id)
        );
    }

    /**
     * Supprime une notification.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            Authentication authentication
    ) {

        // Vérifie que la notification existe.
        if (notificationService.getNotificationById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Vérifie que la notification appartient
        // à l'utilisateur connecté.
        if (!ownsNotification(id, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        notificationService.deleteNotification(id);

        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // MÉTHODES PRIVÉES DE SÉCURITÉ
    // ============================================================

    /**
     * Récupère l'utilisateur correspondant au JWT.
     *
     * authentication.getName() contient l'email de l'utilisateur
     * puisque User.getUsername() retourne son adresse email.
     */
    private User getCurrentUser(Authentication authentication) {

        // Récupération de l'email contenu dans l'authentification.
        String email = authentication.getName();

        // Recherche de l'utilisateur en base.
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Utilisateur connecté introuvable"
                        )
                );
    }

    /**
     * Vérifie que l'identifiant demandé correspond
     * à l'utilisateur actuellement connecté.
     */
    private boolean isCurrentUser(
            Long userId,
            Authentication authentication
    ) {

        return getCurrentUser(authentication)
                .getId()
                .equals(userId);
    }

    /**
     * Vérifie qu'une notification appartient
     * à l'utilisateur actuellement connecté.
     */
    private boolean ownsNotification(
            Long notificationId,
            Authentication authentication
    ) {

        // Récupération de l'identifiant de l'utilisateur connecté.
        Long currentUserId =
                getCurrentUser(authentication).getId();

        // Vérification de la propriété de la notification.
        return notificationService.belongsToUser(
                notificationId,
                currentUserId
        );
    }
}