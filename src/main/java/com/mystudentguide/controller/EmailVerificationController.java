package com.mystudentguide.controller;

import com.mystudentguide.dto.auth.MessageResponse;
import com.mystudentguide.dto.auth.VerifyEmailRequest;
import com.mystudentguide.service.EmailVerificationService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Confirmation de l'adresse e-mail.
 *
 * POST /api/auth/verify-email            { "token": "..." }  (public : lien reçu par e-mail)
 * POST /api/account/verification-email   renvoie le lien (étudiant connecté, 1 par minute)
 */
@RestController
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/api/auth/verify-email")
    public ResponseEntity<MessageResponse> confirmer(@Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.confirmer(request.token());
        return ResponseEntity.ok(new MessageResponse("Votre adresse e-mail est confirmée. Merci !"));
    }

    @PostMapping("/api/account/verification-email")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> renvoyer(Authentication authentication) {
        emailVerificationService.renvoyer(authentication.getName());
        return ResponseEntity.ok(new MessageResponse(
                "Un nouveau lien de confirmation vient de vous être envoyé."));
    }
}
