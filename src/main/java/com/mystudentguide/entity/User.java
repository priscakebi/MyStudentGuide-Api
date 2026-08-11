package com.mystudentguide.entity;

import com.mystudentguide.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


/**
 * Représente un utilisateur de l'application.
 *
 * Cette entité est dédiée à l'authentification et aux informations
 * essentielles du compte utilisateur.
 *
 * Les informations administratives (visa, nationalité, logement...)
 * seront stockées dans StudentProfile.
 */
@Entity
@Table(name = "users") // Nom de la table dans la base de données
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Clé primaire de la table.
     *
     * GenerationType.IDENTITY signifie que c'est MySQL
     * qui générera automatiquement l'identifiant.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Prénom de l'utilisateur.
     */
    @Column(nullable = false, length = 100)
    private String firstName;

    /**
     * Nom de famille.
     */
    @Column(nullable =false, length =100)
    private String lastName;

    /**
     * Adresse email.
     *
     * Elle doit être unique afin qu'un même utilisateur
     * ne puisse pas créer plusieurs comptes avec le même email.
     */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Mot de passe.
     *
     * IMPORTANT :
     * Ce mot de passe sera toujours enregistré
     * sous forme chiffrée (BCrypt).
     *
     * On ne stocke JAMAIS un mot de passe en clair.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Rôle de l'utilisateur.
     *
     * EnumType.STRING permet d'enregistrer
     * "ADMIN" ou "ETUDIANT"
     * plutôt que 0 ou 1.
     *
     * C'est beaucoup plus lisible dans la base.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;

    /**
     * Indique si le compte est actif.
     *
     * true  -> compte utilisable
     * false -> compte désactivé
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * Permet de savoir si l'utilisateur
     * a confirmé son adresse email.
     *
     * Pour le MVP, cette valeur restera à true.
     * Plus tard, on pourra ajouter une vérification
     * par email.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = true;

    /**
     * Date de création du compte.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Dernière date de modification.
     */
    private LocalDateTime updatedAt;

    /**
     * Relation OneToOne avec StudentProfile.
     *
     * Un utilisateur possède un seul profil.
     *
     * mappedBy signifie que la clé étrangère sera
     * située dans StudentProfile.
     */
    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private StudentProfile studentProfile;

    /**
     * Méthode automatiquement appelée
     * juste avant l'insertion en base.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Méthode automatiquement appelée
     * avant chaque mise à jour.
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

}