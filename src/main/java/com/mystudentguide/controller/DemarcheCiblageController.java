package com.mystudentguide.controller;

import com.mystudentguide.dto.demarche.CiblageDTO;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.enums.ReferenceEcheance;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.service.ServicePublicImportService.ResultatPublication;
import com.mystudentguide.service.SuggestionAutoService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    /**
     * Règle de date limite personnalisée d'une démarche.
     *
     * PUT /api/admin/demarches/{id}/echeance
     * Corps : { "reference": "ARRIVEE_EN_FRANCE", "decalageJours": 90 }
     * (reference null = pas de règle : la date limite générale s'applique).
     * Exemples : VLS-TS → ARRIVEE_EN_FRANCE, +90 ; renouvellement du titre de
     * séjour → FIN_TITRE_SEJOUR, -60.
     */
    @PutMapping("/{id}/echeance")
    @Transactional
    public ResponseEntity<?> modifierEcheance(@PathVariable Long id, @RequestBody RegleEcheance regle) {

        Demarche demarche = demarcheRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Démarche introuvable avec l'id : " + id));

        if (regle == null || regle.reference() == null) {
            demarche.setEcheanceReference(null);
            demarche.setEcheanceDecalageJours(null);
        } else {
            if (regle.decalageJours() == null || Math.abs(regle.decalageJours()) > 3650) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Le décalage doit être un nombre de jours entre -3650 et 3650."));
            }
            demarche.setEcheanceReference(regle.reference());
            demarche.setEcheanceDecalageJours(regle.decalageJours());
        }

        Demarche sauvee = demarcheRepository.save(demarche);
        return ResponseEntity.ok(new RegleEcheance(sauvee.getEcheanceReference(), sauvee.getEcheanceDecalageJours()));
    }

    /** Règle d'échéance : date du profil + nombre de jours (négatif = avant). */
    public record RegleEcheance(ReferenceEcheance reference, Integer decalageJours) {
    }
}
