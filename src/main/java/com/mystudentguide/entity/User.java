package com.mystudentguide.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mystudentguide.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Collection;

import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


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
public class User implements UserDetails {

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
    private String prenom;

    /**
     * Nom de famille.
     */
    @Column(nullable =false, length =100)
    private String nom;

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
     * Jamais renvoyé dans le JSON (liste des utilisateurs de l'admin).
     */
    @JsonIgnore
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
     * Version des conditions d'utilisation et de la politique de
     * confidentialité acceptées à l'inscription (ex. « 2026-10 »).
     * Vide pour les comptes créés avant l'ajout du consentement
     * et pour les administrateurs.
     */
    @Column(length = 20)
    private String conditionsVersion;

    /**
     * Date à laquelle l'étudiant a accepté ces conditions (preuve du consentement, RGPD).
     */
    private LocalDateTime conditionsAccepteesLe;

    /**
     * Dernière connexion réussie (inscription comprise). Sert à supprimer
     * les comptes inactifs (RGPD, InactiviteService). Vide pour les comptes
     * créés avant ce suivi : la date de création sert alors de référence.
     */
    private LocalDateTime derniereConnexion;

    /**
     * Date d'envoi de l'e-mail prévenant que le compte inactif va être supprimé.
     * Remise à vide à chaque connexion.
     */
    private LocalDateTime avertissementInactiviteLe;

    /**
     * Double authentification (administrateurs) : secret TOTP partagé avec
     * l'application d'authentification, encodé en Base32. Vide si jamais configurée.
     */
    @JsonIgnore
    @Column(length = 64)
    private String totpSecret;

    /**
     * true quand la double authentification est activée : un code à 6 chiffres
     * est demandé à chaque connexion. Vide (= désactivée) pour les comptes existants.
     */
    private Boolean totpActive;

    /**
     * Dernier pas de 30 secondes accepté : un même code ne sert qu'une fois.
     */
    @JsonIgnore
    private Long totpDernierPas;

    /**
     * Empreintes SHA-256 des codes de secours encore utilisables,
     * séparées par des virgules (les codes eux-mêmes ne sont jamais stockés).
     */
    @JsonIgnore
    @Column(length = 1000)
    private String totpCodesSecours;

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

    /**
     * Vrai si un code de l'application d'authentification est demandé à la connexion.
     */
    public boolean deuxFacteursActive() {
        return Boolean.TRUE.equals(totpActive) && totpSecret != null;
    }

    @Override
public Collection<? extends GrantedAuthority> getAuthorities() {

    return List.of(
        new SimpleGrantedAuthority("ROLE_" + role.name())
);
}

@Override
public String getPassword() {
    return password;
}

@Override
public String getUsername() {
    return email;
}

@Override
public boolean isAccountNonExpired() {
    return true;
}

@Override
public boolean isAccountNonLocked() {
    return true;
}

@Override
public boolean isCredentialsNonExpired() {
    return true;
}

@Override
public boolean isEnabled() {
    return Boolean.TRUE.equals(enabled);
}

}