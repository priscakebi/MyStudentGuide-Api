package com.mystudentguide.service;

import com.mystudentguide.entity.Notification;
import com.mystudentguide.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service responsable de la gestion des notifications
 * des utilisateurs de MyStudentGuide.
 *
 * Les notifications permettent notamment d'informer
 * l'étudiant concernant :
 *
 * - une nouvelle démarche ;
 * - une échéance ;
 * - une information administrative ;
 * - une notification système.
 *
 * Architecture :
 *
 * Controller
 *      ↓
 * NotificationService
 *      ↓
 * NotificationRepository
 *      ↓
 * MySQL
 */
@Service
public class NotificationService {

    /**
     * Repository permettant d'accéder aux notifications
     * enregistrées dans la base de données.
     */
    private final NotificationRepository notificationRepository;

    /**
     * Injection du repository par constructeur.
     *
     * @param notificationRepository repository des notifications
     */
    public NotificationService(
            NotificationRepository notificationRepository
    ) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Récupère toutes les notifications d'un utilisateur.
     *
     * Les notifications sont retournées de la plus récente
     * à la plus ancienne.
     *
     * @param userId identifiant de l'utilisateur
     * @return liste des notifications
     */
    public List<Notification> getNotificationsByUser(Long userId) {
        return notificationRepository
                .findByUserIdOrderByDateCreationDesc(userId);
    }

    /**
     * Récupère uniquement les notifications non lues
     * d'un utilisateur.
     *
     * @param userId identifiant de l'utilisateur
     * @return liste des notifications non lues
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository
                .findByUserIdAndLuOrderByDateCreationDesc(
                        userId,
                        false
                );
    }

    /**
     * Compte le nombre de notifications non lues
     * d'un utilisateur.
     *
     * Cette méthode pourra être utilisée pour afficher
     * un compteur sur l'icône de notification.
     *
     * Exemple :
     *
     * 🔔 3
     *
     * @param userId identifiant de l'utilisateur
     * @return nombre de notifications non lues
     */
    public long countUnreadNotifications(Long userId) {
        return notificationRepository
                .countByUserIdAndLu(userId, false);
    }


    /**
 * Vérifie si une notification identique existe déjà
 * pour un utilisateur.
 *
 * Cette méthode est utilisée par le système de rappels
 * afin d'éviter de créer plusieurs fois la même notification.
 *
 * Une notification est considérée comme identique lorsque :
 * - elle appartient au même utilisateur ;
 * - elle possède le même type ;
 * - elle possède exactement le même message.
 *
 * @param userId identifiant de l'utilisateur
 * @param type type de notification recherché
 * @param message contenu de la notification
 * @return true si la notification existe déjà,
 *         false dans le cas contraire
 */
public boolean existsNotification(
        Long userId,
        com.mystudentguide.enums.NotificationType type,
        String message
) {

    return notificationRepository
            .existsByUserIdAndTypeAndMessage(
                    userId,
                    type,
                    message
            );
}

    /**
     * Recherche une notification à partir de son identifiant.
     *
     * @param id identifiant de la notification
     * @return notification trouvée ou Optional.empty()
     */
    /** Vrai si la notification appartient à l'utilisateur donné. */
    public boolean belongsToUser(Long notificationId, Long userId) {
        return notificationRepository.existsByIdAndUserId(notificationId, userId);
    }

    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    /**
     * Enregistre une notification.
     *
     * Cette méthode pourra être utilisée lorsqu'une nouvelle
     * notification doit être créée pour un étudiant.
     *
     * @param notification notification à enregistrer
     * @return notification enregistrée
     */
    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    /**
     * Marque une notification comme lue.
     *
     * Lorsqu'un étudiant consulte une notification,
     * son statut "lu" passe de false à true.
     *
     * @param id identifiant de la notification
     * @return notification mise à jour
     *
     * @throws RuntimeException si la notification n'existe pas
     */
    public Notification markAsRead(Long id) {

        Notification notification = notificationRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Notification introuvable avec l'identifiant : " + id
                        )
                );

        notification.setLu(true);

        return notificationRepository.save(notification);
    }

    /**
     * Supprime une notification.
     *
     * @param id identifiant de la notification
     */
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}