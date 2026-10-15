package com.mystudentguide.controller;

import com.mystudentguide.dto.aide.DemandeAideDtos.DemandeAideRequest;
import com.mystudentguide.dto.aide.DemandeAideDtos.DemandeAideResponse;
import com.mystudentguide.dto.aide.DemandeAideDtos.ReponseAideRequest;
import com.mystudentguide.enums.StatutDemandeAide;
import com.mystudentguide.service.DemandeAideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Demandes d'aide des étudiants (page « Besoin d'aide ? »).
 *
 * POST   /api/aide                               (ETUDIANT) envoyer une demande
 * GET    /api/aide/me                            (ETUDIANT) mes demandes et leurs réponses
 * DELETE /api/aide/{id}                          (ETUDIANT) supprimer l'une de mes demandes
 * GET    /api/admin/aide?statut=OUVERTE          (ADMIN)    liste
 * PUT    /api/admin/aide/{id}/reponse            (ADMIN)    répondre
 * PATCH  /api/admin/aide/{id}?statut=FERMEE      (ADMIN)    fermer / rouvrir
 */
@RestController
public class DemandeAideController {

    private final DemandeAideService demandeAideService;

    public DemandeAideController(DemandeAideService demandeAideService) {
        this.demandeAideService = demandeAideService;
    }

    @PostMapping("/api/aide")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<DemandeAideResponse> envoyer(
            @Valid @RequestBody DemandeAideRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(demandeAideService.envoyer(request, authentication.getName()));
    }

    @GetMapping("/api/aide/me")
    @PreAuthorize("hasRole('ETUDIANT')")
    public List<DemandeAideResponse> mesDemandes(Authentication authentication) {
        return demandeAideService.mesDemandes(authentication.getName());
    }

    @DeleteMapping("/api/aide/{id}")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id, Authentication authentication) {
        demandeAideService.supprimer(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/aide")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DemandeAideResponse> lister(@RequestParam(required = false) StatutDemandeAide statut) {
        return demandeAideService.lister(statut);
    }

    @PutMapping("/api/admin/aide/{id}/reponse")
    @PreAuthorize("hasRole('ADMIN')")
    public DemandeAideResponse repondre(
            @PathVariable Long id,
            @Valid @RequestBody ReponseAideRequest request
    ) {
        return demandeAideService.repondre(id, request.reponse());
    }

    @PatchMapping("/api/admin/aide/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DemandeAideResponse changerStatut(
            @PathVariable Long id,
            @RequestParam StatutDemandeAide statut
    ) {
        return demandeAideService.changerStatut(id, statut);
    }
}
