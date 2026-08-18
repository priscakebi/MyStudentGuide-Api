package com.mystudentguide.repository;

import com.mystudentguide.entity.Demarche;
import com.mystudentguide.enums.DemarcheStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository permettant de gérer les démarches administratives.
 *
 * Spring Data JPA fournit automatiquement les opérations CRUD
 * grâce à l'héritage de JpaRepository.
 */
public interface DemarcheRepository extends JpaRepository<Demarche, Long> {

    /**
     * Récupère toutes les démarches appartenant
     * à un profil étudiant.
     *
     * Exemple :
     *
     * StudentProfile
     *      |
     *      ├── Démarche 1
     *      ├── Démarche 2
     *      └── Démarche 3
     *
     * @param studentProfileId identifiant du profil étudiant
     * @return liste des démarches de l'étudiant
     */
    List<Demarche> findByStudentProfileId(Long studentProfileId);

    /**
     * Récupère les démarches d'un étudiant
     * ayant un statut précis.
     *
     * Exemple :
     *
     * findByStudentProfileIdAndStatut(
     *      1L,
     *      DemarcheStatus.EN_COURS
     * );
     *
     * permet de récupérer uniquement les démarches
     * actuellement en cours.
     *
     * @param studentProfileId identifiant du profil étudiant
     * @param statut statut recherché
     * @return liste des démarches correspondant aux critères
     */
    List<Demarche> findByStudentProfileIdAndStatut(
            Long studentProfileId,
            DemarcheStatus statut
    );

    /**
     * Récupère toutes les démarches appartenant
     * à une catégorie.
     *
     * Exemple :
     *
     * Catégorie "Logement"
     *      ↓
     * plusieurs démarches
     *
     * @param categorieId identifiant de la catégorie
     * @return liste des démarches de la catégorie
     */
    List<Demarche> findByCategorieId(Long categorieId);

    /**
     * Compte le nombre de démarches d'un étudiant
     * ayant un statut donné.
     *
     * Cette méthode sera particulièrement utile
     * pour le tableau de bord.
     *
     * Exemple :
     *
     * "Vous avez terminé 5 démarches."
     *
     * @param studentProfileId identifiant du profil étudiant
     * @param statut statut recherché
     * @return nombre de démarches correspondant aux critères
     */
    long countByStudentProfileIdAndStatut(
            Long studentProfileId,
            DemarcheStatus statut
    );
}