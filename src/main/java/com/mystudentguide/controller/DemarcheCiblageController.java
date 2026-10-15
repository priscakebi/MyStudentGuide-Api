package com.mystudentguide.controller;

import com.mystudentguide.dto.demarche.CiblageDTO;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.service.ServicePublicImportService.ResultatPublication;
import com.mystudentguide.service.SuggestionAutoService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Ciblage des suggestions automatiques d'une démarche.
 *
 * PUT /api/admin/demarches/{id}/ciblage  (ADMIN)
 *
 * Enregistre le ciblage puis, si la démarche est publiée, envoie la
 * suggestion aux étudiants dont le profil correspond (jamais deux fois).
 */
@RestController
@RequestMapping("/api/admin/demarches")
@PreAuthorize("hasRole('ADMIN')")
public class DemarcheCiblageController {

    private final DemarcheRepository demarcheRepository;
    private final SuggestionAutoService suggestionAutoService;

    public DemarcheCiblageController(
            DemarcheRepository demarcheRepository,
            SuggestionAutoService suggestionAutoService
    ) {
        this.demarcheRepository = demarcheRepository;
        this.suggestionAutoService = suggestionAutoService;
    }

    @PutMapping("/{id}/ciblage")
    @Transactional
    public ResultatPublication modifierCiblage(@PathVariable Long id, @RequestBody CiblageDTO ciblage) {

        Demarche demarche = demarcheRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Démarche introuvable avec l'id : " + id));

        ciblage.appliquerA(demarche);
        Demarche sauvee = demarcheRepository.save(demarche);

        return new ResultatPublication(sauvee, suggestionAutoService.suggererPourDemarche(sauvee));
    }
}
