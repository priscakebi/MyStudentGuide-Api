package com.mystudentguide.repository;

import com.mystudentguide.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository permettant d'accéder aux données de l'entité User.
 *
 * Spring Data JPA fournit automatiquement les opérations CRUD :
 *
 * - save()
 * - findById()
 * - findAll()
 * - delete()
 * - deleteById()
 * - existsById()
 *
 * Il n'est donc pas nécessaire d'écrire nous-mêmes
 * les requêtes SQL correspondantes.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur à partir de son adresse email.
     *
     * Cette méthode sera notamment utilisée lors de la connexion
     * et de l'authentification.
     *
     * Spring Data JPA comprend automatiquement :
     *
     * findBy + Email
     *
     * et génère la requête correspondante.
     *
     * @param email adresse email recherchée
     * @return l'utilisateur correspondant s'il existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si une adresse email existe déjà.
     *
     * Cette méthode sera utile lors de l'inscription
     * afin d'empêcher la création de plusieurs comptes
     * avec la même adresse email.
     *
     * @param email adresse email à vérifier
     * @return true si l'email existe déjà, false sinon
     */
    boolean existsByEmail(String email);
}