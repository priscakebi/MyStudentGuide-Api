package com.mystudentguide.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO utilisé lorsqu'un utilisateur souhaite modifier
 * les informations de son compte.
 *
 * Ce DTO est différent de RegisterRequest :
 *
 * RegisterRequest
 * → création d'un compte
 *
 * UserUpdateRequest
 * → modification d'un compte existant
 */
public class UserUpdateRequest {

    /**
     * Nouvelle adresse email.
     */
    @NotBlank(message = "L'adresse email est obligatoire")
    @Email(message = "L'adresse email doit être valide")
    private String email;

    /**
     * Nouveau prénom.
     */
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    /**
     * Nouveau nom.
     */
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    /**
     * Constructeur vide nécessaire à Spring
     * pour convertir le JSON reçu en objet Java.
     */
    public UserUpdateRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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