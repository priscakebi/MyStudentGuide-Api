package com.mystudentguide.controller;

import com.mystudentguide.repository.StudentProfileRepository;
import com.mystudentguide.servicepublic.AnnuaireClient;
import com.mystudentguide.servicepublic.LieuPrefecture;
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
 * GET /api/annuaire/prefecture/me        préfecture du domicile (adresse du profil), sinon de la ville d'études (ETUDIANT)
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

    /**
     * Préfecture du DOMICILE (code postal de l'adresse du profil) : c'est
     * elle qui est compétente pour le titre de séjour. À défaut d'adresse
     * exploitable, celle de la ville d'études. Le champ « base » indique
     * lequel a servi.
     */
    @GetMapping("/prefecture/me")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<PrefectureInfo> maPrefecture(Authentication authentication) {
        return studentProfileRepository.findByUserEmail(authentication.getName())
                .flatMap(profil -> LieuPrefecture.candidats(profil.getAdresse(), profil.getVilleEtudes())
                        .stream()
                        .flatMap(lieu -> annuaireClient.prefecturePourVille(lieu.recherche())
                                .map(prefecture -> prefecture.avecBase(lieu.base()))
                                .stream())
                        .findFirst())
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
