package com.mystudentguide.controller;

import com.mystudentguide.dto.demarche.TraductionDtos.Traduction;
import com.mystudentguide.service.DemarcheTraductionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Version anglaise du contenu d'une démarche (réservé à l'admin).
 */
@RestController
@RequestMapping("/api/admin/demarches/{id}/traduction")
@PreAuthorize("hasRole('ADMIN')")
public class DemarcheTraductionController {

    private final DemarcheTraductionService traductionService;

    public DemarcheTraductionController(DemarcheTraductionService traductionService) {
        this.traductionService = traductionService;
    }

    @GetMapping
    public Traduction lire(@PathVariable Long id) {
        return traductionService.lire(id);
    }

    @PutMapping
    public Traduction enregistrer(@PathVariable Long id, @Valid @RequestBody Traduction traduction) {
        return traductionService.enregistrer(id, traduction);
    }
}
