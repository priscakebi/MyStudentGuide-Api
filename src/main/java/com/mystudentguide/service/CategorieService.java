package com.mystudentguide.service;

import com.mystudentguide.entity.Categorie;
import com.mystudentguide.repository.CategorieRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service responsable de la gestion des catégories
 * de démarches administratives.
 *
 * Une catégorie permet de regrouper les démarches
 * ayant un même domaine.
 *
 * Exemples :
 *
 * - Logement
 * - Séjour
 * - Santé
 * - Études
 * - Travail
 * - Vie quotidienne
 *
 * Architecture :
 *
 * Controller
 *      ↓
 * CategorieService
 *      ↓
 * CategorieRepository
 *      ↓
 * MySQL
 */
@Service
public class CategorieService {

    /**
     * Repository permettant d'accéder aux catégories
     * enregistrées dans la base de données.
     */
    private final CategorieRepository categorieRepository;

    /**
     * Injection du repository par constructeur.
     *
     * @param categorieRepository repository des catégories
     */
    public CategorieService(CategorieRepository categorieRepository) {
        this.categorieRepository = categorieRepository;
    }

    /**
     * Récupère toutes les catégories.
     *
     * Cette méthode pourra notamment être utilisée
     * pour afficher les différentes catégories dans
     * l'application.
     *
     * @return liste de toutes les catégories
     */
    public List<Categorie> getAllCategories() {
        return categorieRepository.findAll();
    }

    /**
     * Recherche une catégorie à partir de son identifiant.
     *
     * @param id identifiant de la catégorie
     * @return catégorie trouvée ou Optional.empty()
     */
    public Optional<Categorie> getCategorieById(Long id) {
        return categorieRepository.findById(id);
    }

    /**
     * Recherche une catégorie à partir de son nom.
     *
     * @param nom nom de la catégorie
     * @return catégorie trouvée ou Optional.empty()
     */
    public Optional<Categorie> getCategorieByNom(String nom) {
        return categorieRepository.findByNom(nom);
    }

    /**
     * Vérifie si une catégorie existe déjà avec le nom fourni.
     *
     * Cette vérification permet d'éviter la création
     * de catégories portant le même nom.
     *
     * @param nom nom de la catégorie
     * @return true si la catégorie existe déjà
     */
    public boolean categorieExists(String nom) {
        return categorieRepository.existsByNom(nom);
    }

    /**
     * Enregistre une nouvelle catégorie ou met à jour
     * une catégorie existante.
     *
     * @param categorie catégorie à sauvegarder
     * @return catégorie enregistrée
     */
    public Categorie saveCategorie(Categorie categorie) {
        return categorieRepository.save(categorie);
    }

    /**
     * Supprime une catégorie à partir de son identifiant.
     *
     * Attention :
     * une catégorie peut être liée à plusieurs démarches.
     * La suppression devra donc être contrôlée lorsque
     * nous développerons la logique métier complète.
     *
     * @param id identifiant de la catégorie
     */
    public void deleteCategorie(Long id) {
        categorieRepository.deleteById(id);
    }
}