package com.mystudentguide.repository;

import com.mystudentguide.entity.Notification;
import com.mystudentguide.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /** Vrai si la notification existe ET appartient à cet utilisateur. */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * Vérifie si une notification avec le même utilisateur,
     * le même type et le même message existe déjà.
     *
     * Cette méthode permet notamment d'éviter d'envoyer plusieurs fois
     * le même rappel d'échéance lorsque le scheduler s'exécute.
     *
     * @param userId identifiant de l'utilisateur
     * @param type type de notification
     * @param message contenu exact de la notification
     * @return true si la notification existe déjà, sinon false
     */
    boolean existsByUserIdAndTypeAndMessage(
            Long userId,
            NotificationType type,
            String message
    );

    /**
     * Supprime toutes les notifications associées à un utilisateur.
     *
     * Cette méthode est utilisée lors de la suppression définitive
     * du compte afin de supprimer les notifications de l'étudiant
     * avant de supprimer son compte.
     *
     * Spring Data JPA génère automatiquement la requête
     * correspondant à cette méthode grâce à son nom.
     *
     * @param userId identifiant de l'utilisateur
     */
    void deleteByUserId(Long userId);

    /**
     * Notifications « à action » encore sans réponse
     * pour un suivi de démarche donné.
     */
    @Query("""
        SELECT n
        FROM Notification n
        WHERE n.studentDemarcheId = :studentDemarcheId
        AND n.action = :action
        AND (n.actionTraitee IS NULL OR n.actionTraitee = false)
    """)
    List<Notification> findPendingActions(
            @Param("studentDemarcheId") Long studentDemarcheId,
            @Param("action") String action
    );

    /**
     * Suggestions encore ouvertes d'une démarche pour un utilisateur.
     *
     * Sert à clore la suggestion quand l'étudiant commence
     * la démarche, et à éviter les suggestions en double.
     */
    @Query("""
        SELECT n
        FROM Notification n
        WHERE n.user.id = :userId
        AND n.demarcheId = :demarcheId
        AND n.action = :action
        AND (n.actionTraitee IS NULL OR n.actionTraitee = false)
    """)
    List<Notification> findPendingDemarcheActions(
            @Param("userId") Long userId,
            @Param("demarcheId") Long demarcheId,
            @Param("action") String action
    );
}