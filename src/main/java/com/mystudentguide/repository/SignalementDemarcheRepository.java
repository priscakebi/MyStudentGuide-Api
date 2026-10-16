package com.mystudentguide.repository;

import com.mystudentguide.entity.SignalementDemarche;
import com.mystudentguide.enums.StatutSignalement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SignalementDemarcheRepository extends JpaRepository<SignalementDemarche, Long> {

    /** Signalements (avec leur démarche), du plus récent au plus ancien. */
    @Query("""
        SELECT s
        FROM SignalementDemarche s
        JOIN FETCH s.demarche
        WHERE (:statut IS NULL OR s.statut = :statut)
        ORDER BY s.dateCreation DESC
    """)
    List<SignalementDemarche> findAvecDemarche(@Param("statut") StatutSignalement statut);

    long countByStatut(StatutSignalement statut);

    /** Un étudiant a-t-il déjà un signalement ouvert sur cette démarche ? */
    boolean existsByDemarche_IdAndUserIdAndStatut(Long demarcheId, Long userId, StatutSignalement statut);

    /** Suppression du compte : les signalements de l'étudiant sont effacés. */
    void deleteByUserId(Long userId);

    /** Signalements envoyés par un étudiant (export de ses données, RGPD). */
    List<SignalementDemarche> findByUserIdOrderByDateCreationDesc(Long userId);
}
