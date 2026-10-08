package com.mystudentguide.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * Jeton de réinitialisation de mot de passe.
 *
 * Sécurité :
 * - le jeton en clair n'est JAMAIS stocké : il n'existe que dans l'e-mail
 *   envoyé à l'utilisateur. La base ne contient que son empreinte SHA-256 ;
 *   une fuite de la base ne permet donc pas de réinitialiser un compte ;
 * - le jeton a une durée de vie limitée (expiresAt) ;
 * - il est à usage unique : il est supprimé dès que le mot de passe est changé.
 *
 * La suppression d'un utilisateur supprime ses jetons (ON DELETE CASCADE).
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Empreinte SHA-256 (hexadécimal, 64 caractères) du jeton envoyé par e-mail. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
