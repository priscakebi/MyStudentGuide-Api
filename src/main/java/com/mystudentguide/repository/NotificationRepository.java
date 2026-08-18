package com.mystudentguide.repository;

import com.mystudentguide.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository permettant de gérer les notifications
 * des utilisateurs de MyStudentGuide.
 *
 * Spring Data JPA fournit automatiquement les opérations CRUD
 * grâce à JpaRepository.
 */
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    /**
     * Récupère toutes les notifications d'un utilisateur.
     *
     * Les notifications sont triées de la plus récente
     * à la plus ancienne.
     *
     * Exemple :
     *
     * notificationRepository
     *     .findByUserIdOrderByDateCreationDesc(userId);
     *
     * @param userId identifiant de l'utilisateur
     * @return liste des notifications de l'utilisateur
     */
    List<Notification> findByUserIdOrderByDateCreationDesc(Long userId);

    /**
     * Récupère uniquement les notifications non lues
     * d'un utilisateur.
     *
     * Elles sont également classées de la plus récente
     * à la plus ancienne.
     *
     * @param userId identifiant de l'utilisateur
     * @param lu valeur indiquant si la notification est lue
     * @return liste des notifications correspondant aux critères
     */
    List<Notification> findByUserIdAndLuOrderByDateCreationDesc(
            Long userId,
            Boolean lu
    );

    /**
     * Compte le nombre de notifications d'un utilisateur
     * selon leur état de lecture.
     *
     * Cette méthode sera notamment utile pour afficher
     * le nombre de notifications non lues dans le tableau
     * de bord.
     *
     * Exemple :
     *
     * "Vous avez 3 nouvelles notifications"
     *
     * @param userId identifiant de l'utilisateur
     * @param lu état de lecture recherché
     * @return nombre de notifications correspondant aux critères
     */
    long countByUserIdAndLu(Long userId, Boolean lu);
}