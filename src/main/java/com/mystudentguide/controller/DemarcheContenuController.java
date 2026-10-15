package com.mystudentguide.controller;

import com.mystudentguide.dto.demarche.ContenuDtos.Contenu;
import com.mystudentguide.dto.demarche.ContenuDtos.EtapeRequest;
import com.mystudentguide.dto.demarche.ContenuDtos.LienRequest;
import com.mystudentguide.dto.demarche.ContenuDtos.OrdreRequest;
import com.mystudentguide.service.DemarcheContenuService;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Édition des étapes et des liens officiels d'une démarche (ADMIN).
 *
 * GET    /api/admin/demarches/{id}/contenu
 * POST   /api/admin/demarches/{id}/etapes
 * PUT    /api/admin/demarches/{id}/etapes/{etapeId}
 * DELETE /api/admin/demarches/{id}/etapes/{etapeId}
 * PUT    /api/admin/demarches/{id}/etapes/ordre        {"ids":[3,1,2]}
 * POST   /api/admin/demarches/{id}/liens
 * PUT    /api/admin/demarches/{id}/liens/{lienId}
 * DELETE /api/admin/demarches/{id}/liens/{lienId}
 *
 * Chaque réponse renvoie le contenu complet à jour (étapes + liens).
 */
@RestController
@RequestMapping("/api/admin/demarches/{id}")
@PreAuthorize("hasRole('ADMIN')")
public class DemarcheContenuController {

    private final DemarcheContenuService contenuService;

    public DemarcheContenuController(DemarcheContenuService contenuService) {
        this.contenuService = contenuService;
    }

    @GetMapping("/contenu")
    public Contenu lire(@PathVariable Long id) {
        return contenuService.lire(id);
    }

    // ---------------------------------------------------------- étapes

    @PostMapping("/etapes")
    public Contenu ajouterEtape(@PathVariable Long id, @Valid @RequestBody EtapeRequest request) {
        return contenuService.ajouterEtape(id, request);
    }

    /** Le chemin littéral « ordre » est prioritaire sur /etapes/{etapeId}. */
    @PutMapping("/etapes/ordre")
    public Contenu reordonner(@PathVariable Long id, @Valid @RequestBody OrdreRequest request) {
        return contenuService.reordonnerEtapes(id, request.ids());
    }

    @PutMapping("/etapes/{etapeId}")
    public Contenu modifierEtape(@PathVariable Long id, @PathVariable Long etapeId,
                                 @Valid @RequestBody EtapeRequest request) {
        return contenuService.modifierEtape(id, etapeId, request);
    }

    @DeleteMapping("/etapes/{etapeId}")
    public Contenu supprimerEtape(@PathVariable Long id, @PathVariable Long etapeId) {
        return contenuService.supprimerEtape(id, etapeId);
    }

    // ---------------------------------------------------------- liens

    @PostMapping("/liens")
    public Contenu ajouterLien(@PathVariable Long id, @Valid @RequestBody LienRequest request) {
        return contenuService.ajouterLien(id, request);
    }

    @PutMapping("/liens/{lienId}")
    public Contenu modifierLien(@PathVariable Long id, @PathVariable Long lienId,
                                @Valid @RequestBody LienRequest request) {
        return contenuService.modifierLien(id, lienId, request);
    }

    @DeleteMapping("/liens/{lienId}")
    public Contenu supprimerLien(@PathVariable Long id, @PathVariable Long lienId) {
        return contenuService.supprimerLien(id, lienId);
    }
}
