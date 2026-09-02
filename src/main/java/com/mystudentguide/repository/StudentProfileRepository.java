package com.mystudentguide.repository;

import com.mystudentguide.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository permettant de gérer les données des profils étudiants.
 *
 * Spring Data JPA fournit automatiquement les opérations CRUD
 * grâce à l'héritage de JpaRepository.
 */
public interface StudentProfileRepository
        extends JpaRepository<StudentProfile, Long> {

    /**
     * Recherche le profil étudiant associé à un utilisateur.
     *
     * Comme la relation entre User et StudentProfile est
     * une relation OneToOne, un utilisateur ne peut avoir
     * qu'un seul profil étudiant.
     *
     * Exemple :
     *
     * User
     *   ↓
     * StudentProfile
     *
     * Cette méthode sera notamment utile après la connexion
     * pour récupérer le profil de l'utilisateur connecté.
     *
     * @param userId identifiant de l'utilisateur
     * @return le profil étudiant s'il existe
     */
    Optional<StudentProfile> findByUserId(Long userId);

    /**
     * Vérifie si un profil étudiant existe déjà
     * pour un utilisateur.
     *
     * Cette méthode permet d'éviter de créer plusieurs
     * profils pour le même utilisateur.
     *
     * @param userId identifiant de l'utilisateur
     * @return true si un profil existe, false sinon
     */
    boolean existsByUserId(Long userId);


    /**
     * Recherche un profil étudiant à partir
     * de l'adresse email de l'utilisateur.
     *
     * Spring Data comprend automatiquement le chemin :
     *
     * StudentProfile
     *      ↓
     * user
     *      ↓
     * email
     *
     * Cette méthode sera utilisée par
     * GET /api/student-profiles/me
     *
     * @param email email de l'utilisateur connecté
     * @return profil étudiant s'il existe
     */
    Optional<StudentProfile> findByUserEmail(String email);

    
}