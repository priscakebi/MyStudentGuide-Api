package com.mystudentguide.controller;

import com.mystudentguide.dto.demarche.ConseilDtos.AstuceDuJour;
import com.mystudentguide.dto.demarche.ConseilDtos.ConseilDTO;
import com.mystudentguide.dto.demarche.ConseilDtos.ConseilRequest;
import com.mystudentguide.service.ConseilService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Conseils pratiques des démarches.
 *
 * Admin :
 * GET    /api/admin/demarches/{id}/conseils
 * POST   /api/admin/demarches/{id}/conseils               { texte, texteEn }
 * PUT    /api/admin/demarches/{id}/conseils/{conseilId}   { texte, texteEn }
 * DELETE /api/admin/demarches/{id}/conseils/{conseilId}
 * (chaque appel renvoie la liste à jour)
 *
 * Étudiant :
 * GET /api/conseils/astuce-du-jour   → 200 { texte, texteEn, demarcheId, ... } ou 204 s'il n'y en a pas
 *
 * Sur la fiche publique, les conseils sont inclus dans GET /api/demarches/{id}.
 */
@RestController
public class ConseilController {

    private final ConseilService conseilService;

    public ConseilController(ConseilService conseilService) {
        this.conseilService = conseilService;
    }

    @GetMapping("/api/admin/demarches/{id}/conseils")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ConseilDTO> lister(@PathVariable Long id) {
        return conseilService.lister(id);
    }

    @PostMapping("/api/admin/demarches/{id}/conseils")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ConseilDTO> ajouter(@PathVariable Long id, @Valid @RequestBody ConseilRequest request) {
        return conseilService.ajouter(id, request);
    }

    @PutMapping("/api/admin/demarches/{id}/conseils/{conseilId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ConseilDTO> modifier(@PathVariable Long id, @PathVariable Long conseilId,
                                     @Valid @RequestBody ConseilRequest request) {
        return conseilService.modifier(id, conseilId, request);
    }

    @DeleteMapping("/api/admin/demarches/{id}/conseils/{conseilId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ConseilDTO> supprimer(@PathVariable Long id, @PathVariable Long conseilId) {
        return conseilService.supprimer(id, conseilId);
    }

    @GetMapping("/api/conseils/astuce-du-jour")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<AstuceDuJour> astuceDuJour(Authentication authentication) {
        return conseilService.astuceDuJour(authentication.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
