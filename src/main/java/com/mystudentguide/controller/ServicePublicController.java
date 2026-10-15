package com.mystudentguide.controller;

import com.mystudentguide.entity.Demarche;
import com.mystudentguide.enums.StatutPublication;
import com.mystudentguide.service.ServicePublicImportService;
import com.mystudentguide.service.ServicePublicImportService.ApercuFiche;
import com.mystudentguide.service.ServicePublicImportService.FicheCatalogue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Import et synchronisation des démarches depuis Service-Public.
 *
 * Toutes les routes sont réservées aux administrateurs.
 *
 * GET   /api/admin/service-public/catalogue              fiches suggérées
 * GET   /api/admin/service-public/fiches/{ficheId}       aperçu d'une fiche
 * POST  /api/admin/service-public/import                 import en brouillon
 * POST  /api/admin/service-public/verifier               vérifier les mises à jour maintenant
 * POST  /api/admin/service-public/demarches/{id}/mise-a-jour   appliquer la mise à jour
 * PATCH /api/admin/service-public/demarches/{id}/publication?statut=PUBLIEE|BROUILLON
 */
@RestController
@RequestMapping("/api/admin/service-public")
@PreAuthorize("hasRole('ADMIN')")
public class ServicePublicController {

    private final ServicePublicImportService importService;

    public ServicePublicController(ServicePublicImportService importService) {
        this.importService = importService;
    }

    public record ImportRequest(
            @NotBlank(message = "L'identifiant de la fiche est obligatoire") String ficheId,
            @NotNull(message = "La catégorie est obligatoire") Long categorieId
    ) {
    }

    @GetMapping("/catalogue")
    public List<FicheCatalogue> catalogue() {
        return importService.getCatalogue();
    }

    @GetMapping("/fiches/{ficheId}")
    public ApercuFiche apercu(@PathVariable String ficheId) {
        return importService.apercu(ficheId);
    }

    @PostMapping("/import")
    public ResponseEntity<Demarche> importer(@Valid @RequestBody ImportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(importService.importer(request.ficheId(), request.categorieId()));
    }

    @PostMapping("/verifier")
    public Map<String, Integer> verifier() {
        return Map.of("misesAJourDetectees", importService.verifierMisesAJour());
    }

    @PostMapping("/demarches/{id}/mise-a-jour")
    public Demarche appliquerMiseAJour(@PathVariable Long id) {
        return importService.appliquerMiseAJour(id);
    }

    @PatchMapping("/demarches/{id}/publication")
    public Demarche publication(@PathVariable Long id, @RequestParam StatutPublication statut) {
        return importService.changerPublication(id, statut);
    }
}
