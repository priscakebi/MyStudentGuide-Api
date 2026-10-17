package com.mystudentguide.controller;

import com.mystudentguide.service.DeuxFacteursService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Double authentification du compte administrateur (« Mon compte »).
 *
 * GET  /api/account/2fa                    état (activée, codes de secours restants)
 * POST /api/account/2fa/preparer           nouveau secret + lien otpauth:// (QR code)
 * POST /api/account/2fa/activer            { code }  → codes de secours (affichés une fois)
 * POST /api/account/2fa/codes-secours      { code }  → nouveaux codes de secours
 * POST /api/account/2fa/desactiver         { motDePasse, code }
 *
 * Erreurs en 400 (jamais 401 : côté front, un 401 déconnecte).
 */
@RestController
@RequestMapping("/api/account/2fa")
@PreAuthorize("hasRole('ADMIN')")
public class DeuxFacteursController {

    public record CodeRequest(@NotBlank(message = "Saisissez le code.") @Size(max = 20) String code) {
    }

    public record DesactivationRequest(
            @NotBlank(message = "Saisissez votre mot de passe.") String motDePasse,
            @NotBlank(message = "Saisissez le code.") @Size(max = 20) String code
    ) {
    }

    public record CodesSecoursResponse(List<String> codesSecours) {
    }

    private final DeuxFacteursService deuxFacteursService;

    public DeuxFacteursController(DeuxFacteursService deuxFacteursService) {
        this.deuxFacteursService = deuxFacteursService;
    }

    @GetMapping
    public DeuxFacteursService.Etat etat(Authentication authentication) {
        return deuxFacteursService.etat(authentication.getName());
    }

    @PostMapping("/preparer")
    public DeuxFacteursService.Preparation preparer(Authentication authentication) {
        return deuxFacteursService.preparer(authentication.getName());
    }

    @PostMapping("/activer")
    public CodesSecoursResponse activer(Authentication authentication, @Valid @RequestBody CodeRequest request) {
        return new CodesSecoursResponse(deuxFacteursService.activer(authentication.getName(), request.code()));
    }

    @PostMapping("/codes-secours")
    public CodesSecoursResponse codesSecours(Authentication authentication, @Valid @RequestBody CodeRequest request) {
        return new CodesSecoursResponse(
                deuxFacteursService.regenererCodesSecours(authentication.getName(), request.code()));
    }

    @PostMapping("/desactiver")
    public ResponseEntity<Map<String, String>> desactiver(
            Authentication authentication,
            @Valid @RequestBody DesactivationRequest request
    ) {
        deuxFacteursService.desactiver(authentication.getName(), request.motDePasse(), request.code());
        return ResponseEntity.ok(Map.of("message", "La double authentification est désactivée."));
    }
}
