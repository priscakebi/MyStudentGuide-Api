package com.mystudentguide.repository;

import com.mystudentguide.entity.SuggestionEnvoyee;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SuggestionEnvoyeeRepository extends JpaRepository<SuggestionEnvoyee, Long> {

    boolean existsByUserIdAndDemarcheId(Long userId, Long demarcheId);

    /** Suppression du compte de l'étudiant. */
    void deleteByUserId(Long userId);

    /** Suppression de la démarche. */
    void deleteByDemarcheId(Long demarcheId);
}
