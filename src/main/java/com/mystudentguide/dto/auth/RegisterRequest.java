package com.mystudentguide.dto.auth;

import com.mystudentguide.validation.FormRules;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO utilisé lors de l'inscription d'un nouvel utilisateur.
 *
 * Ce DTO représente uniquement les informations que
 * l'utilisateur est autorisé à envoyer lors de son inscription.
 *
 * Il ne faut pas utiliser directement l'entité User ici.
 */
public class RegisterRequest {

    /**
     * Adresse email de l'utilisateur.
     */
    @NotBlank(message = "L'adresse email est obligatoire")
    @Email(message = "L'adresse email doit être valide")
    @Pattern(regexp = FormRules.EMAIL_REGEX, message = "L'adresse email doit être valide (ex. : nom@domaine.fr)")
    @Size(max = FormRules.EMAIL_MAX, message = "L'adresse email est trop longue")
    private String email;

    /**
     * Mot de passe choisi par l'utilisateur.
     *
     * Politique CNIL / ANSSI : voir FormRules.
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Pattern(regexp = FormRules.PASSWORD_REGEX, message = FormRules.PASSWORD_MESSAGE)
    private String password;

    /**
     * Prénom de l'utilisateur.
     */
    @NotBlank(message = "Le prénom est obligatoire")
    @Pattern(regexp = FormRules.NAME_REGEX, message = "Le prénom " + FormRules.NAME_MESSAGE)
    private String prenom;

    /**
     * Nom de famille de l'utilisateur.
     */
    @NotBlank(message = "Le nom est obligatoire")
    @Pattern(regexp = FormRules.NAME_REGEX, message = "Le nom " + FormRules.NAME_MESSAGE)
    private String nom;

    /**
     * Constructeur vide nécessaire notamment
     * pour la désérialisation JSON par Spring.
     */
    public RegisterRequest() {
    }

    // Getters et Setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}