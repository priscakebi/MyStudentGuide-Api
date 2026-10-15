package com.mystudentguide.controller;

import com.mystudentguide.dto.signalement.SignalementDtos.SignalementRequest;
import com.mystudentguide.dto.signalement.SignalementDtos.SignalementResponse;
import com.mystudentguide.enums.StatutSignalement;
import com.mystudentguide.service.SignalementService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Signalements d'erreurs sur les démarches.
 *
 * POST  /api/demarches/{id}/signalements            (ETUDIANT) signaler un problème
 * GET   /api/admin/signalements?statut=OUVERT       (ADMIN)    liste
 * PATCH /api/admin/signalements/{id}?statut=TRAITE  (ADMIN)    traiter / rejeter / rouvrir
 */
@RestController
public class SignalementController {

    private final SignalementService signalementService;

    public SignalementController(SignalementService signalementService) {
        this.signalementService = signalementService;
    }

    @PostMapping("/api/demarches/{id}/signalements")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<SignalementResponse> signaler(
            @PathVariable Long id,
            @Valid @RequestBody SignalementRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(signalementService.signaler(id, request, authentication.getName()));
    }

    @GetMapping("/api/admin/signalements")
    @PreAuthorize("hasRole('ADMIN')")
    public List<SignalementResponse> lister(@RequestParam(required = false) StatutSignalement statut) {
        return signalementService.lister(statut);
    }

    @PatchMapping("/api/admin/signalements/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SignalementResponse changerStatut(
            @PathVariable Long id,
            @RequestParam StatutSignalement statut
    ) {
        return signalementService.changerStatut(id, statut);
    }
}
