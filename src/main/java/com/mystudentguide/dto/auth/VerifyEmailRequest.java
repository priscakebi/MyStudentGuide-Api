package com.mystudentguide.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Jeton reçu dans le lien de confirmation de l'adresse e-mail.
 */
public record VerifyEmailRequest(
        @NotBlank(message = "Le lien de confirmation est incomplet.")
        @Size(max = 200, message = "Le lien de confirmation est invalide.")
        String token
) {
}
