package com.mystudentguide.dto.auth;

import jakarta.validation.constraints.NotBlank;
import com.mystudentguide.validation.FormRules;
import jakarta.validation.constraints.Pattern;

/** Définition du nouveau mot de passe à partir du jeton reçu par e-mail. */
public class ResetPasswordRequest {

    @NotBlank(message = "Le lien de réinitialisation est invalide")
    private String token;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Pattern(regexp = FormRules.PASSWORD_REGEX, message = FormRules.PASSWORD_MESSAGE)
    private String newPassword;

    public ResetPasswordRequest() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
