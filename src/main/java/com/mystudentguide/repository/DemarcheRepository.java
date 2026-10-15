package com.mystudentguide.repository;

import com.mystudentguide.entity.Demarche;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository permettant d'effectuer les opérations en base de données
 * sur l'entité Demarche.
 *
 * Demarche représente maintenant une démarche générale.
 *
 * Le suivi d'une démarche pour un étudiant particulier est géré
 * par l'entité StudentDemarche.
 */
public interface DemarcheRepository extends JpaRepository<Demarche, Long> {

    /**
     * Recherche toutes les démarches appartenant à une catégorie.
     *
     * Exemple :
     *
     * catégorie = "Séjour"
     *
     * Résultat possible :
     * - Première demande de titre de séjour
     * - Renouvellement du titre de séjour
     * - Changement de statut
     *
     * Spring Data JPA construit automatiquement la requête
     * à partir du nom de la méthode.
     *
     * @param categorieId identifiant de la catégorie
     * @return liste des démarches de cette catégorie
     */
    List<Demarche> findByCategorieId(Long categorieId);

    /** Démarches importées depuis une fiche Service-Public. */
    List<Demarche> findBySourceIdIsNotNull();

    /** Vrai si la fiche Service-Public a déjà été importée. */
    boolean existsBySourceId(String sourceId);
}