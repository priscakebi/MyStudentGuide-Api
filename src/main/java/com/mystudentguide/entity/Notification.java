package com.mystudentguide.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mystudentguide.enums.NotificationType;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Représente une notification envoyée à un utilisateur
 * de MyStudentGuide.
 *
 * Une notification appartient à un seul utilisateur.
 *
 * Un utilisateur peut recevoir plusieurs notifications.
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
     * Généré automatiquement par MySQL.
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
     * Message de la notification.
     *
     * Exemple :
     * "Votre titre de séjour arrive bientôt à expiration."
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Type de notification.
     *
     * Exemple :
     * ECHEANCE
     * DEMARCHE
     * INFORMATION
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    /**
     * Indique si la notification a été lue.
     *
     * false = non lue
     * true  = lue
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean lu = false;

    /**
     * Date de création de la notification.
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
     * @JsonIgnore est important ici.
     *
     * On ne veut pas que Jackson essaie de transformer
     * tout l'objet User en JSON lorsqu'on retourne
     * une notification.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    /**
     * Initialise automatiquement la date de création
     * avant l'insertion en base.
     */
    @PrePersist
    protected void onCreate() {

        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }

        if (lu == null) {
            lu = false;
        }
    }
}