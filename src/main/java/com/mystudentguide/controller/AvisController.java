package com.mystudentguide.controller;

import com.mystudentguide.dto.avis.AvisRequest;
import com.mystudentguide.dto.avis.AvisResponse;
import com.mystudentguide.service.AvisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Avis des étudiants.
 *
 * GET    /api/avis/public?limit=6     → page d'accueil (accessible à tous)
 * POST   /api/avis                    → l'étudiant donne son avis
 * GET    /api/avis                    → administration : tous les avis
 * PATCH  /api/avis/{id}/visible?visible=false → administration : masquer
 * DELETE /api/avis/{id}               → administration : supprimer
 */
@RestController
@RequestMapping("/api/avis")
public class AvisController {

    private final AvisService avisService;

    public AvisController(AvisService avisService) {
        this.avisService = avisService;
    }

    @GetMapping("/public")
    public ResponseEntity<List<AvisResponse>> getAvisPublics(
            @RequestParam(defaultValue = "6") int limit
    ) {
        return ResponseEntity.ok(avisService.getAvisPublics(limit));
    }

    @PostMapping
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<AvisResponse> creerAvis(
            @Valid @RequestBody AvisRequest request,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(avisService.creerAvis(authentication.getName(), request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AvisResponse>> getTousLesAvis() {
        return ResponseEntity.ok(avisService.getTousLesAvis());
    }

    @PatchMapping("/{id}/visible")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AvisResponse> changerVisibilite(
            @PathVariable Long id,
            @RequestParam boolean visible
    ) {
        return ResponseEntity.ok(avisService.changerVisibilite(id, visible));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimerAvis(@PathVariable Long id) {
        avisService.supprimerAvis(id);
        return ResponseEntity.noContent().build();
    }
}
