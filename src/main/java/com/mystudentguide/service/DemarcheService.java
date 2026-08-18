package com.mystudentguide.service;

import com.mystudentguide.entity.Demarche;
import com.mystudentguide.enums.DemarcheStatus;
import com.mystudentguide.repository.DemarcheRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service responsable de la gestion des démarches administratives.
 *
 * Le service contient la logique métier liée aux démarches.
 *
 * Architecture :
 *
 * Controller
 *      ↓
 * DemarcheService
 *      ↓
 * DemarcheRepository
 *      ↓
 * MySQL
 */
@Service
public class DemarcheService {

    /**
     * Repository permettant d'accéder aux démarches
     * enregistrées dans la base de données.
     */
    private final DemarcheRepository demarcheRepository;

    /**
     * Injection du repository par constructeur.
     *
     * @param demarcheRepository repository des démarches
     */
    public DemarcheService(DemarcheRepository demarcheRepository) {
        this.demarcheRepository = demarcheRepository;
    }

    /**
     * Récupère toutes les démarches présentes en base de données.
     *
     * Cette méthode pourra notamment être utilisée
     * par l'administration.
     *
     * @return liste de toutes les démarches
     */
    public List<Demarche> getAllDemarches() {
        return demarcheRepository.findAll();
    }

    /**
     * Recherche une démarche à partir de son identifiant.
     *
     * @param id identifiant de la démarche
     * @return démarche trouvée ou Optional.empty()
     */
    public Optional<Demarche> getDemarcheById(Long id) {
        return demarcheRepository.findById(id);
    }

    /**
     * Récupère toutes les démarches d'un étudiant.
     *
     * Cette méthode sera notamment utilisée pour afficher
     * le tableau de bord de l'étudiant.
     *
     * @param studentProfileId identifiant du profil étudiant
     * @return liste des démarches de l'étudiant
     */
    public List<Demarche> getDemarchesByStudentProfile(
            Long studentProfileId
    ) {
        return demarcheRepository
                .findByStudentProfileId(studentProfileId);
    }

    /**
     * Récupère les démarches d'un étudiant
     * selon leur statut.
     *
     * Exemple :
     *
     * - A_FAIRE
     * - EN_COURS
     * - TERMINEE
     * - NON_CONCERNEE
     *
     * @param studentProfileId identifiant du profil étudiant
     * @param statut statut recherché
     * @return liste des démarches correspondant au statut
     */
    public List<Demarche> getDemarchesByStatus(
            Long studentProfileId,
            DemarcheStatus statut
    ) {
        return demarcheRepository
                .findByStudentProfileIdAndStatut(
                        studentProfileId,
                        statut
                );
    }

    /**
     * Récupère toutes les démarches appartenant
     * à une catégorie.
     *
     * @param categorieId identifiant de la catégorie
     * @return liste des démarches de la catégorie
     */
    public List<Demarche> getDemarchesByCategorie(
            Long categorieId
    ) {
        return demarcheRepository.findByCategorieId(categorieId);
    }

    /**
     * Compte le nombre de démarches d'un étudiant
     * ayant un statut donné.
     *
     * Cette méthode sera utile pour le tableau de bord.
     *
     * Exemple :
     *
     * "5 démarches terminées"
     *
     * @param studentProfileId identifiant du profil étudiant
     * @param statut statut recherché
     * @return nombre de démarches
     */
    public long countDemarchesByStatus(
            Long studentProfileId,
            DemarcheStatus statut
    ) {
        return demarcheRepository
                .countByStudentProfileIdAndStatut(
                        studentProfileId,
                        statut
                );
    }

    /**
     * Enregistre une nouvelle démarche ou met à jour
     * une démarche existante.
     *
     * @param demarche démarche à sauvegarder
     * @return démarche enregistrée
     */
    public Demarche saveDemarche(Demarche demarche) {
        return demarcheRepository.save(demarche);
    }

    /**
     * Met à jour le statut d'une démarche.
     *
     * Cette méthode sera notamment utilisée lorsque
     * l'étudiant fait avancer une démarche.
     *
     * Exemple :
     *
     * A_FAIRE → EN_COURS → TERMINEE
     *
     * @param id identifiant de la démarche
     * @param nouveauStatut nouveau statut
     * @return démarche mise à jour
     *
     * @throws RuntimeException si la démarche n'existe pas
     */
    public Demarche updateStatus(
            Long id,
            DemarcheStatus nouveauStatut
    ) {
        Demarche demarche = demarcheRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Démarche introuvable avec l'identifiant : " + id
                        )
                );

        demarche.setStatut(nouveauStatut);

        return demarcheRepository.save(demarche);
    }

    /**
     * Supprime une démarche à partir de son identifiant.
     *
     * Cette opération sera principalement destinée
     * à l'administration.
     *
     * @param id identifiant de la démarche
     */
    public void deleteDemarche(Long id) {
        demarcheRepository.deleteById(id);
    }
}