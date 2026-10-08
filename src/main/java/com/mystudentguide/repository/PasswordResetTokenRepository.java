package com.mystudentguide.repository;

import com.mystudentguide.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /** Retrouve un jeton à partir de l'empreinte SHA-256 du jeton reçu. */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** Vrai si un jeton a déjà été créé pour cet utilisateur après la date donnée (anti-spam). */
    boolean existsByUser_IdAndCreatedAtAfter(Long userId, LocalDateTime date);

    /** Supprime tous les jetons d'un utilisateur. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PasswordResetToken t WHERE t.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /** Supprime les jetons expirés (nettoyage quotidien). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now")
    int deleteExpired(@Param("now") LocalDateTime now);
}
