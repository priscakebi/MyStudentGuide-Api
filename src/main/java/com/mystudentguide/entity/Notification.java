package com.mystudentguide.entity;

import com.mystudentguide.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Représente une notification envoyée à un utilisateur
 * de MyStudentGuide.
 *
 * Une notification peut par exemple informer l'étudiant :
 *
 * - qu'une nouvelle démarche est disponible ;
 * - qu'une échéance approche ;
 * - qu'une démarche a été générée ;
 * - qu'une action importante doit être effectuée.
 *
 * Une notification appartient toujours à un seul utilisateur.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    /**
     * Identifiant unique de la notification.
     *
     * La valeur est générée automatiquement par la base
     * de données lors de la création de la notification.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Titre de la notification.
     *
     * Exemple :
     * "Échéance proche"
     */
    @Column(nullable = false, length = 150)
    private String titre;

    /**
     * Contenu de la notification.
     *
     * Exemple :
     * "Votre démarche concernant votre titre de séjour
     * arrive bientôt à échéance."
     *
     * TEXT permet d'avoir un contenu plus long qu'un simple
     * VARCHAR.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Type de notification.
     *
     * Les différentes valeurs seront définies plus tard
     * dans l'enum NotificationType.
     *
     * Exemple :
     * - ECHEANCE
     * - DEMARCHE
     * - INFORMATION
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    /**
     * Indique si la notification a été lue par l'utilisateur.
     *
     * false = notification non lue
     * true  = notification lue
     *
     * Par défaut, une nouvelle notification n'est pas lue.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean lu = false;

    /**
     * Date et heure à laquelle la notification
     * a été créée.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    /**
     * Utilisateur qui reçoit la notification.
     *
     * Relation :
     *
     * User 1 -------- * Notification
     *
     * Un utilisateur peut donc avoir plusieurs notifications,
     * tandis qu'une notification appartient à un seul utilisateur.
     *
     * LAZY signifie que l'utilisateur associé ne sera pas
     * automatiquement chargé lorsque l'on récupère
     * une notification.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Définit automatiquement la date de création
     * avant l'enregistrement en base de données.
     */
    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }
}