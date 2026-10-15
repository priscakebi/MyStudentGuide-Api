package com.mystudentguide.repository;

import com.mystudentguide.entity.DemandeAide;
import com.mystudentguide.enums.StatutDemandeAide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DemandeAideRepository extends JpaRepository<DemandeAide, Long> {

    /** Demandes d'un étudiant, de la plus récente à la plus ancienne. */
    List<DemandeAide> findByUserIdOrderByDateCreationDesc(Long userId);

    /** Toutes les demandes (filtrées par statut si besoin), les plus récentes d'abord. */
    @Query("""
        SELECT d
        FROM DemandeAide d
        WHERE (:statut IS NULL OR d.statut = :statut)
        ORDER BY d.dateCreation DESC
    """)
    List<DemandeAide> findParStatut(@Param("statut") StatutDemandeAide statut);

    long countByStatut(StatutDemandeAide statut);

    /** Limite anti-abus : nombre de demandes encore en attente d'un étudiant. */
    long countByUserIdAndStatut(Long userId, StatutDemandeAide statut);

    /** Suppression du compte : les demandes de l'étudiant sont effacées. */
    void deleteByUserId(Long userId);

    /** Conservation limitée : purge des demandes traitées trop anciennes. */
    @Modifying
    @Query("DELETE FROM DemandeAide d WHERE d.statut <> :ouverte AND d.dateCreation < :limite")
    int purgerAnciennes(@Param("ouverte") StatutDemandeAide ouverte, @Param("limite") LocalDateTime limite);
}
