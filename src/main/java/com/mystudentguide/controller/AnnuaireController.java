package com.mystudentguide.controller;

import com.mystudentguide.repository.StudentProfileRepository;
import com.mystudentguide.servicepublic.AnnuaireClient;
import com.mystudentguide.servicepublic.PrefectureInfo;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Préfecture compétente, d'après l'API Annuaire de l'administration.
 *
 * GET /api/annuaire/prefecture/me        préfecture de la ville d'études du profil (ETUDIANT)
 * GET /api/annuaire/prefecture?ville=X   préfecture d'une ville (utilisateur connecté)
 *
 * 204 No Content : ville non renseignée ou inconnue.
 */
@RestController
@RequestMapping("/api/annuaire")
public class AnnuaireController {

    private final AnnuaireClient annuaireClient;
    private final StudentProfileRepository studentProfileRepository;

    public AnnuaireController(
            AnnuaireClient annuaireClient,
            StudentProfileRepository studentProfileRepository
    ) {
        this.annuaireClient = annuaireClient;
        this.studentProfileRepository = studentProfileRepository;
    }

    @GetMapping("/prefecture/me")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<PrefectureInfo> maPrefecture(Authentication authentication) {
        return studentProfileRepository.findByUserEmail(authentication.getName())
                .map(profil -> profil.getVilleEtudes())
                .flatMap(annuaireClient::prefecturePourVille)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/prefecture")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PrefectureInfo> prefecture(@RequestParam String ville) {
        return annuaireClient.prefecturePourVille(ville)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
