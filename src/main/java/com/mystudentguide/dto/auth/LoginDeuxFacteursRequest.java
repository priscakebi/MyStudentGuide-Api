package com.mystudentguide.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Seconde étape de la connexion avec double authentification.
 *
 * @param defi jeton reçu de POST /api/auth/login
 * @param code code à 6 chiffres de l'application, ou code de secours
 */
public record LoginDeuxFacteursRequest(
        @NotBlank @Size(max = 2000) String defi,
        @NotBlank(message = "Saisissez le code.") @Size(max = 20) String code
) {
}
