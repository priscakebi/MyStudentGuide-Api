package com.mystudentguide.dto.account;

import jakarta.validation.constraints.NotBlank;
import com.mystudentguide.validation.FormRules;
import jakarta.validation.constraints.Pattern;

/**
 * Changement du mot de passe depuis l'application
 * (utilisateur connecté, étudiant ou administrateur).
 *
 * PUT /api/account/password
 */
public class ChangePasswordRequest {

    @NotBlank(message = "Le mot de passe actuel est obligatoire")
    private String currentPassword;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Pattern(regexp = FormRules.PASSWORD_REGEX, message = FormRules.PASSWORD_MESSAGE)
    private String newPassword;

    public ChangePasswordRequest() {
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
