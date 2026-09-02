package com.mystudentguide.controller;

import com.mystudentguide.enums.DemarcheStatus;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.service.DemarcheService;

import com.mystudentguide.dto.demarche.DemarcheRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller responsable de la gestion des démarches
 * administratives proposées par MyStudentGuide.
 *
 * Une démarche représente une procédure administrative
 * qu'un étudiant peut devoir effectuer.
 *
 * Exemples :
 *
 * - renouvellement d'un titre de séjour ;
 * - demande de visa ;
 * - inscription universitaire ;
 * - demande de logement ;
 * - demande d'aide financière ;
 * - etc.
 *
 * Architecture :
 *
 * Client / Angular
 * ↓
 * DemarcheController
 * ↓
 * DemarcheService
 * ↓
 * DemarcheRepository
 * ↓
 * MySQL
 */
@RestController
@RequestMapping("/api/demarches")
public class DemarcheController {

    /**
     * Service responsable de la logique métier
     * concernant les démarches.
     */
    private final DemarcheService demarcheService;

    /**
     * Injection du service par constructeur.
     *
     * @param demarcheService service de gestion des démarches
     */
    public DemarcheController(
            DemarcheService demarcheService) {
        this.demarcheService = demarcheService;
    }

    @GetMapping("/student-profile/{studentProfileId}/statut/{statut}")
    public ResponseEntity<List<Demarche>> getByStatut(
            @PathVariable Long studentProfileId,
            @PathVariable DemarcheStatus statut) {

        List<Demarche> demarches = demarcheService.getDemarchesByStatus(
                studentProfileId,
                statut);

        return ResponseEntity.ok(demarches);
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<Demarche> updateStatus(
            @PathVariable Long id,
            @RequestParam DemarcheStatus statut) {

        try {

            Demarche updatedDemarche = demarcheService.updateStatus(id, statut);

            return ResponseEntity.ok(updatedDemarche);

        } catch (RuntimeException exception) {

            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Récupère toutes les démarches.
     *
     * Endpoint :
     *
     * GET /api/demarches
     *
     * Cette route pourra notamment permettre à Angular
     * d'afficher la liste des démarches disponibles.
     *
     * @return liste des démarches
     */
    @GetMapping
    public ResponseEntity<List<Demarche>> getAllDemarches() {

        List<Demarche> demarches = demarcheService.getAllDemarches();

        return ResponseEntity.ok(demarches);
    }

    /**
     * Récupère une démarche à partir de son identifiant.
     *
     * Endpoint :
     *
     * GET /api/demarches/{id}
     *
     * Exemple :
     *
     * GET /api/demarches/1
     *
     * @param id identifiant de la démarche
     * @return démarche trouvée ou 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Demarche> getDemarcheById(
            @PathVariable Long id) {

        Optional<Demarche> demarche = demarcheService.getDemarcheById(id);

        return demarche
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound().build());
    }

    /**
     * Récupère les démarches appartenant
     * à une catégorie donnée.
     *
     * Endpoint :
     *
     * GET /api/demarches/categorie/{categorieId}
     *
     * Exemple :
     *
     * GET /api/demarches/categorie/2
     *
     * @param categorieId identifiant de la catégorie
     * @return liste des démarches de cette catégorie
     */
    @GetMapping("/categorie/{categorieId}")
    public ResponseEntity<List<Demarche>> getByCategorie(
            @PathVariable Long categorieId) {

        List<Demarche> demarches = demarcheService.getDemarchesByCategorie(categorieId);

        return ResponseEntity.ok(demarches);
    }

    /**
     * Récupère les démarches associées
     * à un profil étudiant.
     *
     * Endpoint :
     *
     * GET /api/demarches/student-profile/{studentProfileId}
     *
     * @param studentProfileId identifiant du profil étudiant
     * @return liste des démarches du profil
     */
    @GetMapping("/student-profile/{studentProfileId}")
    public ResponseEntity<List<Demarche>> getByStudentProfile(
            @PathVariable Long studentProfileId) {

        List<Demarche> demarches = demarcheService.getDemarchesByStudentProfile(
                studentProfileId);

        return ResponseEntity.ok(demarches);
    }

    /**
     * Crée une nouvelle démarche.
     *
     * Endpoint :
     *
     * POST /api/demarches
     *
     * @param demarche démarche à créer
     * @return démarche créée
     */
    @PostMapping
public ResponseEntity<Demarche> createDemarche(
        @Valid @RequestBody DemarcheRequest request) {

    Demarche savedDemarche =
            demarcheService.createDemarche(request);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(savedDemarche);
}

    /**
     * Modifie une démarche existante.
     *
     * Endpoint :
     *
     * PUT /api/demarches/{id}
     *
     * @param id       identifiant de la démarche
     * @param demarche nouvelles informations
     * @return démarche mise à jour
     */
   @PutMapping("/{id}")
public ResponseEntity<Demarche> updateDemarche(
        @PathVariable Long id,
        @Valid @RequestBody DemarcheRequest request) {

    try {

        Demarche updatedDemarche =
                demarcheService.updateDemarche(id, request);

        return ResponseEntity.ok(updatedDemarche);

    } catch (RuntimeException exception) {

        return ResponseEntity.notFound().build();
    }
}

    /**
     * Supprime une démarche.
     *
     * Endpoint :
     *
     * DELETE /api/demarches/{id}
     *
     * @param id identifiant de la démarche
     * @return 204 si la suppression est effectuée
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDemarche(
            @PathVariable Long id) {

        Optional<Demarche> existingDemarche = demarcheService.getDemarcheById(id);

        if (existingDemarche.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        demarcheService.deleteDemarche(id);

        return ResponseEntity.noContent().build();
    }
}