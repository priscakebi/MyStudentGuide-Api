package com.mystudentguide.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    private String email;

    /**
     * Mot de passe choisi par l'utilisateur.
     *
     * On impose une longueur minimale pour des raisons
     * de sécurité.
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(
        min = 8,
        message = "Le mot de passe doit contenir au moins 8 caractères"
    )
    private String password;

    /**
     * Prénom de l'utilisateur.
     */
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    /**
     * Nom de famille de l'utilisateur.
     */
    @NotBlank(message = "Le nom est obligatoire")
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