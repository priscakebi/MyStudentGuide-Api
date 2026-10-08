package com.mystudentguide.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Demande de réinitialisation de mot de passe.
 *
 * "espace" indique depuis quel formulaire la demande est faite :
 * "ADMIN" (page administrateur) ou "ETUDIANT" (par défaut).
 * Un compte n'est concerné que s'il appartient à l'espace demandé.
 */
public class ForgotPasswordRequest {

    @NotBlank(message = "L'adresse email est obligatoire")
    @Email(message = "L'adresse email doit être valide")
    private String email;

    private String espace;

    public ForgotPasswordRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEspace() {
        return espace;
    }

    public void setEspace(String espace) {
        this.espace = espace;
    }
}
