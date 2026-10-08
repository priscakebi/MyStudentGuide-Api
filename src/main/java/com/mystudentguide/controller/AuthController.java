package com.mystudentguide.controller;


import com.mystudentguide.dto.auth.AuthResponse;
import com.mystudentguide.dto.auth.ForgotPasswordRequest;
import com.mystudentguide.dto.auth.MessageResponse;
import com.mystudentguide.dto.auth.ResetPasswordRequest;
import com.mystudentguide.dto.auth.LoginRequest;
import com.mystudentguide.dto.auth.RegisterRequest;
import com.mystudentguide.service.AuthService;
import com.mystudentguide.service.PasswordResetService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsable de l'authentification.
 *
 * Il expose les endpoints permettant :
 *
 * - de créer un compte utilisateur ;
 * - de se connecter ;
 * - d'obtenir un JWT après une authentification réussie.
 *
 * Les routes de ce controller sont publiques.
 *
 * Exemple :
 *
 * POST /api/auth/register
 * POST /api/auth/login
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Service contenant toute la logique métier
     * liée à l'authentification.
     */
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    /**
     * Injection du AuthService.
     *
     * @param authService service d'authentification
     */
    public AuthController(
            AuthService authService,
            PasswordResetService passwordResetService
    ) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    /**
     * Inscription d'un nouvel utilisateur.
     *
     * Endpoint :
     *
     * POST /api/auth/register
     *
     * Exemple de requête :
     *
     * {
     *     "prenom": "Prisca",
     *     "nom": "Kouenin",
     *     "email": "prisca@email.com",
     *     "password": "MotDePasse123"
     * }
     *
     * @param request informations nécessaires à l'inscription
     * @return AuthResponse contenant les informations
     *         du compte créé et le JWT
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Connexion d'un utilisateur.
     *
     * Endpoint :
     *
     * POST /api/auth/login
     *
     * Exemple :
     *
     * {
     *     "email": "prisca@email.com",
     *     "password": "MotDePasse123"
     * }
     *
     * @param request email et mot de passe
     * @return AuthResponse contenant le JWT
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Mot de passe oublié : envoie un lien de réinitialisation par e-mail.
     *
     * POST /api/auth/forgot-password   { "email": "...", "espace": "ETUDIANT" | "ADMIN" }
     *
     * La réponse est TOUJOURS la même (200), que l'adresse existe ou non,
     * pour ne pas révéler quelles adresses sont inscrites.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        passwordResetService.requestReset(request.getEmail(), request.getEspace());

        return ResponseEntity.ok(new MessageResponse(
                "Si un compte correspond à cette adresse, un e-mail contenant "
                + "un lien de réinitialisation vient d'être envoyé."));
    }

    /**
     * Définit un nouveau mot de passe à partir du jeton reçu par e-mail.
     *
     * POST /api/auth/reset-password   { "token": "...", "newPassword": "..." }
     *
     * 400 si le lien est invalide ou expiré.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

        return ResponseEntity.ok(new MessageResponse(
                "Votre mot de passe a été modifié. Vous pouvez maintenant vous connecter."));
    }
}
