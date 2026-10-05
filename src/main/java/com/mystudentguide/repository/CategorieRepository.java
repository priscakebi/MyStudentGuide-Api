package com.mystudentguide.repository;

import com.mystudentguide.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository permettant de gérer les données des catégories
 * de démarches administratives.
 *
 * Spring Data JPA fournit automatiquement les opérations CRUD
 * grâce à JpaRepository.
 */
public interface CategorieRepository extends JpaRepository<Categorie, Long> {

    /**
     * Recherche une catégorie à partir de son nom.
     *
     * Exemple :
     *
     * findByNom("Logement")
     *
     * permet de rechercher la catégorie "Logement".
     *
     * Optional est utilisé car la catégorie recherchée
     * peut ne pas exister.
     *
     * @param nom nom de la catégorie
     * @return la catégorie si elle existe
     */
    Optional<Categorie> findByNom(String nom);

    /**
     * Vérifie si une catégorie existe déjà avec
     * le nom donné.
     *
     * Cette méthode sera notamment utile lors de la
     * création d'une nouvelle catégorie par un administrateur.
     *
     * @param nom nom à vérifier
     * @return true si le nom existe déjà, false sinon
     */
    boolean existsByNom(String nom);
}