package com.mystudentguide.controller;

import com.mystudentguide.service.DemarcheService;
import com.mystudentguide.dto.demarche.DemarcheDetailDTO;
import com.mystudentguide.dto.demarche.DemarcheRequest;
import com.mystudentguide.entity.Demarche;


import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller responsable de la gestion des démarches
 * administratives générales de MyStudentGuide.
 *
 * ============================================================
 * RÔLE DE DEMARCHE
 * ============================================================
 *
 * Une Demarche représente une procédure administrative générale.
 *
 * Exemple :
 *
 * - Renouvellement du titre de séjour
 * - Première demande de titre de séjour
 * - Inscription universitaire
 * - Demande de logement
 * - Demande de sécurité sociale
 * - Demande d'aide financière
 *
 * Une démarche générale ne contient PAS les informations
 * personnelles liées à un étudiant.
 *
 * Les informations personnelles sont gérées par :
 *
 *      StudentProfile
 *           ↓
 *      StudentDemarche
 *
 * ============================================================
 * DOCUMENTS
 * ============================================================
 *
 * Une démarche peut posséder plusieurs DocumentRequis.
 *
 * IMPORTANT :
 *
 * DocumentRequis représente uniquement la liste des documents
 * nécessaires pour effectuer une démarche.
 *
 * MyStudentGuide ne stocke pas les fichiers personnels
 * des étudiants.
 *
 * ============================================================
 * DROITS D'ACCÈS
 * ============================================================
 *
 * Les étudiants peuvent consulter les démarches.
 *
 * Seuls les administrateurs peuvent :
 *
 * - créer une démarche ;
 * - modifier une démarche ;
 * - supprimer une démarche.
 */
@RestController
@RequestMapping("/api/demarches")
public class DemarcheController {

    /**
     * Service responsable de la logique métier
     * concernant les démarches générales.
     */
    private final DemarcheService demarcheService;


    /**
     * Constructeur du contrôleur.
     *
     * Spring injecte automatiquement le DemarcheService.
     *
     * @param demarcheService service de gestion des démarches
     */
    public DemarcheController(
            DemarcheService demarcheService
    ) {
        this.demarcheService = demarcheService;
    }


    // ============================================================
    // RÉCUPÉRER TOUTES LES DÉMARCHES
    // ============================================================

    /**
     * Récupère toutes les démarches générales.
     *
     * Endpoint :
     *
     * GET /api/demarches
     *
     * Les utilisateurs authentifiés peuvent consulter
     * la liste des démarches.
     *
     * @return liste de toutes les démarches
     */
    @GetMapping
    public ResponseEntity<List<Demarche>> getAllDemarches(
            Authentication authentication
    ) {

        /*
         * Récupération des démarches via le service.
         *
         * Les brouillons (ex. fiches Service-Public importées
         * mais pas encore validées) ne sont renvoyés qu'aux admins.
         */
        List<Demarche> demarches =
                demarcheService.getAllDemarches(isAdmin(authentication));

        /*
         * Retourne HTTP 200 OK avec la liste.
         */
        return ResponseEntity.ok(demarches);
    }


    // ============================================================
    // RÉCUPÉRER LE DÉTAIL D'UNE DÉMARCHE
    // ============================================================

    /**
     * Récupère le détail complet d'une démarche.
     *
     * Endpoint :
     *
     * GET /api/demarches/{id}
     *
     * Exemple :
     *
     * GET /api/demarches/1
     *
     * Le résultat est retourné sous forme de
     * DemarcheDetailDTO.
     *
     * Le DTO contient notamment :
     *
     * - les informations générales ;
     * - la catégorie ;
     * - les documents requis ;
     * - les étapes ;
     * - les liens officiels.
     *
     * @param id identifiant de la démarche
     * @return détail complet de la démarche
     */
    @GetMapping("/{id}")
    public ResponseEntity<DemarcheDetailDTO> getDemarcheById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        /*
         * Récupération du détail via le service.
         *
         * La méthode du service est annotée
         * avec @Transactional(readOnly = true).
         *
         * Cela permet notamment à Hibernate de charger
         * correctement les relations LAZY pendant
         * la construction du DTO.
         */
        DemarcheDetailDTO demarche =
                demarcheService.getDemarcheDetailById(id, isAdmin(authentication));

        /*
         * Retourne HTTP 200 OK avec le détail
         * de la démarche.
         */
        return ResponseEntity.ok(demarche);
    }


    // ============================================================
    // RÉCUPÉRER LES DÉMARCHES D'UNE CATÉGORIE
    // ============================================================

    /**
     * Récupère les démarches appartenant à une catégorie.
     *
     * Endpoint :
     *
     * GET /api/demarches/categorie/{categorieId}
     *
     * Exemple :
     *
     * GET /api/demarches/categorie/1
     *
     * @param categorieId identifiant de la catégorie
     * @return liste des démarches de la catégorie
     */
    @GetMapping("/categorie/{categorieId}")
    public ResponseEntity<List<Demarche>> getByCategorie(
            @PathVariable Long categorieId,
            Authentication authentication
    ) {

        try {

            /*
             * Le service vérifie l'existence de la catégorie
             * avant de rechercher les démarches.
             */
            List<Demarche> demarches =
                    demarcheService.getDemarchesByCategorie(
                            categorieId
                    );

            // Brouillons réservés aux administrateurs.
            if (!isAdmin(authentication)) {
                demarches = demarches.stream()
                        .filter(Demarche::isPubliee)
                        .toList();
            }

            /*
             * Retourne HTTP 200 OK.
             */
            return ResponseEntity.ok(demarches);

        } catch (RuntimeException exception) {

            /*
             * Si la catégorie n'existe pas,
             * retourne HTTP 404 NOT FOUND.
             */
            return ResponseEntity.notFound().build();
        }
    }


    // ============================================================
    // CRÉER UNE DÉMARCHE
    // ============================================================

    /**
     * Crée une nouvelle démarche administrative.
     *
     * Endpoint :
     *
     * POST /api/demarches
     *
     * IMPORTANT :
     *
     * Seul un administrateur peut créer une démarche.
     *
     * Exemple de JSON :
     *
     * {
     *     "titre": "Renouvellement du titre de séjour",
     *     "description": "Procédure à effectuer avant expiration...",
     *     "categorieId": 1,
     *     "dateLimite": "2026-10-30"
     * }
     *
     * @param request données de la démarche
     * @return démarche créée
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Demarche> createDemarche(
            @Valid @RequestBody DemarcheRequest request
    ) {

        /*
         * @Valid demande à Spring de vérifier
         * les contraintes de DemarcheRequest.
         */
        Demarche savedDemarche =
                demarcheService.createDemarche(request);

        /*
         * HTTP 201 CREATED indique que la ressource
         * a été créée avec succès.
         */
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedDemarche);
    }


    // ============================================================
    // MODIFIER UNE DÉMARCHE
    // ============================================================

    /**
     * Modifie une démarche existante.
     *
     * Endpoint :
     *
     * PUT /api/demarches/{id}
     *
     * IMPORTANT :
     *
     * Seul un administrateur peut modifier
     * une démarche générale.
     *
     * @param id identifiant de la démarche
     * @param request nouvelles informations
     * @return démarche mise à jour
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Demarche> updateDemarche(
            @PathVariable Long id,
            @Valid @RequestBody DemarcheRequest request
    ) {

        try {

            /*
             * Le service recherche la démarche,
             * vérifie la catégorie puis effectue la modification.
             */
            Demarche updatedDemarche =
                    demarcheService.updateDemarche(
                            id,
                            request
                    );

            /*
             * Retourne HTTP 200 OK.
             */
            return ResponseEntity.ok(updatedDemarche);

        } catch (RuntimeException exception) {

            /*
             * Si la démarche n'existe pas,
             * retourne HTTP 404 NOT FOUND.
             */
            return ResponseEntity.notFound().build();
        }
    }


    // ============================================================
    // SUPPRIMER UNE DÉMARCHE
    // ============================================================

    /**
     * Supprime une démarche générale.
     *
     * Endpoint :
     *
     * DELETE /api/demarches/{id}
     *
     * IMPORTANT :
     *
     * Seul un administrateur peut supprimer une démarche.
     *
     * @param id identifiant de la démarche
     * @return HTTP 204 si la suppression réussit
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDemarche(
            @PathVariable Long id
    ) {

        try {

            /*
             * Vérifie d'abord que la démarche existe.
             */
            demarcheService.getDemarcheById(id);

            /*
             * Supprime la démarche.
             */
            demarcheService.deleteDemarche(id);

            /*
             * HTTP 204 NO CONTENT indique que
             * la suppression a réussi.
             */
            return ResponseEntity.noContent().build();

        } catch (RuntimeException exception) {

            /*
             * Si la démarche n'existe pas,
             * retourne HTTP 404 NOT FOUND.
             */
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Vrai si la requête vient d'un administrateur connecté.
     *
     * Les routes GET sont publiques : authentication vaut null
     * pour un visiteur anonyme.
     */
    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}