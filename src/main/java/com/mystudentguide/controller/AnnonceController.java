package com.mystudentguide.controller;

import com.mystudentguide.service.AnnonceService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Annonce de l'équipe à tous les étudiants.
 *
 * POST /api/admin/annonces   { "titre": "...", "message": "...", "email": true }
 * Réponse : { "notifications": 42, "emails": 30 }
 */
@RestController
@RequestMapping("/api/admin/annonces")
@PreAuthorize("hasRole('ADMIN')")
public class AnnonceController {

    public record AnnonceRequest(
            @NotBlank(message = "Le titre est obligatoire.")
            @Size(max = 120, message = "Le titre ne doit pas dépasser 120 caractères.")
            String titre,
            @NotBlank(message = "Le message est obligatoire.")
            @Size(max = 2000, message = "Le message ne doit pas dépasser 2000 caractères.")
            String message,
            boolean email
    ) {
    }

    private final AnnonceService annonceService;

    public AnnonceController(AnnonceService annonceService) {
        this.annonceService = annonceService;
    }

    @PostMapping
    public AnnonceService.Resultat envoyer(@Valid @RequestBody AnnonceRequest request) {
        return annonceService.envoyer(request.titre(), request.message(), request.email());
    }
}
