package com.mystudentguide.repository;

import com.mystudentguide.entity.User;
import com.mystudentguide.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
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
     * Cette méthode est notamment utilisée lors de la connexion
     * et de l'authentification.
     *
     * @param email adresse email recherchée
     * @return l'utilisateur correspondant s'il existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si une adresse email existe déjà.
     *
     * Cette méthode est utilisée notamment lors de l'inscription.
     *
     * @param email adresse email à vérifier
     * @return true si l'email existe déjà, false sinon
     */
    boolean existsByEmail(String email);

    /**
     * Vérifie si un email est déjà utilisé par un autre utilisateur.
     *
     * Cette méthode est particulièrement utile lors de la modification
     * de l'adresse email du compte.
     *
     * Exemple :
     *
     * L'utilisateur connecté possède l'id 5.
     *
     * Si l'email demandé appartient à l'utilisateur 5,
     * le résultat sera false.
     *
     * Si l'email demandé appartient à l'utilisateur 8,
     * le résultat sera true.
     *
     * @param email email à vérifier
     * @param id identifiant de l'utilisateur actuel
     * @return true si un autre utilisateur utilise déjà cet email
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Récupère les utilisateurs possédant un rôle donné.
     *
     * Cette méthode sera notamment utilisée pour récupérer
     * tous les administrateurs lorsqu'un étudiant effectue
     * une action importante.
     *
     * @param role rôle recherché
     * @return liste des utilisateurs possédant ce rôle
     */
    List<User> findByRole(RoleType role);
}