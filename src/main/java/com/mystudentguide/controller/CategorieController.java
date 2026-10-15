package com.mystudentguide.controller;

import com.mystudentguide.entity.Categorie;
import com.mystudentguide.service.CategorieDefautService;
import com.mystudentguide.service.CategorieService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller responsable de la gestion des catégories
 * de démarches administratives.
 *
 * Une catégorie permet de regrouper les démarches
 * par type.
 *
 * Exemples :
 *
 * - Visa
 * - Titre de séjour
 * - Logement
 * - Études
 * - Santé
 * - Travail
 * - Banque
 *
 * Architecture :
 *
 * Angular
 *    ↓
 * CategorieController
 *    ↓
 * CategorieService
 *    ↓
 * CategorieRepository
 *    ↓
 * MySQL
 */
@RestController
@RequestMapping("/api/categories")
public class CategorieController {

    /**
     * Service responsable de la logique métier
     * concernant les catégories.
     */
    private final CategorieService categorieService;

    /**
     * Création des catégories de base (thèmes de la page d'accueil).
     */
    private final CategorieDefautService categorieDefautService;

    /**
     * Injection du service par constructeur.
     *
     * @param categorieService service de gestion des catégories
     * @param categorieDefautService catégories de base
     */
    public CategorieController(
            CategorieService categorieService,
            CategorieDefautService categorieDefautService
    ) {
        this.categorieService = categorieService;
        this.categorieDefautService = categorieDefautService;
    }

    /**
     * Ajoute les catégories de base qui manquent (Séjour & visa, Santé,
     * Logement, Études, Banque & budget, Emploi, Transports, Vie quotidienne).
     *
     * Endpoint :
     *
     * POST /api/categories/defaut
     *
     * Les catégories existantes ne sont pas modifiées.
     *
     * @return nombre de catégories créées
     */
    @PostMapping("/defaut")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Integer> ajouterCategoriesDeBase() {
        return Map.of("creees", categorieDefautService.creerCategoriesManquantes());
    }

    /**
     * Récupère toutes les catégories.
     *
     * Endpoint :
     *
     * GET /api/categories
     *
     * Cette route permettra notamment à Angular
     * d'afficher les différentes catégories
     * proposées à l'étudiant.
     *
     * @return liste des catégories
     */
    @GetMapping
    public ResponseEntity<List<Categorie>> getAllCategories() {

        List<Categorie> categories =
                categorieService.getAllCategories();

        return ResponseEntity.ok(categories);
    }

    /**
     * Récupère une catégorie grâce à son identifiant.
     *
     * Endpoint :
     *
     * GET /api/categories/{id}
     *
     * Exemple :
     *
     * GET /api/categories/1
     *
     * @param id identifiant de la catégorie
     * @return catégorie trouvée ou 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Categorie> getCategorieById(
            @PathVariable Long id
    ) {

        Optional<Categorie> categorie =
                categorieService.getCategorieById(id);

        return categorie
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound().build()
                );
    }

    /**
     * Crée une nouvelle catégorie.
     *
     * Endpoint :
     *
     * POST /api/categories
     *
     * Cette opération sera normalement réservée
     * à un administrateur.
     *
     * @param categorie catégorie à créer
     * @return catégorie créée
     */
    @PostMapping
    public ResponseEntity<Categorie> createCategorie(
            @RequestBody Categorie categorie
    ) {

        Categorie savedCategorie =
                categorieService.saveCategorie(categorie);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedCategorie);
    }

    /**
     * Modifie une catégorie existante.
     *
     * Endpoint :
     *
     * PUT /api/categories/{id}
     *
     * @param id identifiant de la catégorie
     * @param categorie nouvelles informations
     * @return catégorie mise à jour
     */
    @PutMapping("/{id}")
    public ResponseEntity<Categorie> updateCategorie(
            @PathVariable Long id,
            @RequestBody Categorie categorie
    ) {

        Optional<Categorie> existingCategorie =
                categorieService.getCategorieById(id);

        /*
         * Si la catégorie n'existe pas,
         * on retourne HTTP 404.
         */
        if (existingCategorie.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        /*
         * On conserve l'identifiant existant.
         */
        categorie.setId(id);

        Categorie updatedCategorie =
                categorieService.saveCategorie(categorie);

        return ResponseEntity.ok(updatedCategorie);
    }

    /**
     * Supprime une catégorie.
     *
     * Endpoint :
     *
     * DELETE /api/categories/{id}
     *
     * @param id identifiant de la catégorie
     * @return HTTP 204 si la suppression réussit
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategorie(
            @PathVariable Long id
    ) {

        Optional<Categorie> existingCategorie =
                categorieService.getCategorieById(id);

        /*
         * Vérification avant suppression.
         */
        if (existingCategorie.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        categorieService.deleteCategorie(id);

        return ResponseEntity.noContent().build();
    }
}