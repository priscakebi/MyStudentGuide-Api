package com.mystudentguide.repository;

import com.mystudentguide.entity.Avis;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvisRepository extends JpaRepository<Avis, Long> {

    boolean existsByStudentDemarcheId(Long studentDemarcheId);

    /** Avis publics, du plus récent au plus ancien. */
    List<Avis> findByVisibleTrueOrderByDateCreationDesc(Pageable pageable);

    List<Avis> findAllByOrderByDateCreationDesc();

    void deleteByUserId(Long userId);

    /** Avis laissés par un étudiant (export de ses données, RGPD). */
    List<Avis> findByUserIdOrderByDateCreationDesc(Long userId);
}
