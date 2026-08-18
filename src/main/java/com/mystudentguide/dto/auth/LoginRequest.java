package com.mystudentguide.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO utilisé lors de la connexion d'un utilisateur.
 *
 * Ce DTO contient uniquement les informations nécessaires
 * pour authentifier l'utilisateur :
 *
 * - son adresse email ;
 * - son mot de passe.
 *
 * On n'utilise pas directement l'entité User afin de ne pas
 * exposer inutilement les propriétés de celle-ci à l'API.
 */
public class LoginRequest {

    /**
     * Adresse email utilisée pour la connexion.
     *
     * @NotBlank vérifie que l'utilisateur n'envoie pas
     * une valeur vide ou composée uniquement d'espaces.
     *
     * @Email vérifie que le format de l'adresse est valide.
     */
    @NotBlank(message = "L'adresse email est obligatoire")
    @Email(message = "L'adresse email doit être valide")
    private String email;

    /**
     * Mot de passe utilisé pour la connexion.
     *
     * Le mot de passe sera ensuite vérifié par
     * Spring Security avec le mot de passe chiffré
     * enregistré en base de données.
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    /**
     * Constructeur vide nécessaire à Spring pour
     * convertir automatiquement le JSON reçu en objet Java.
     */
    public LoginRequest() {
    }

    /**
     * Retourne l'adresse email.
     *
     * @return adresse email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Modifie l'adresse email.
     *
     * @param email nouvelle adresse email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retourne le mot de passe.
     *
     * @return mot de passe
     */
    public String getPassword() {
        return password;
    }

    /**
     * Modifie le mot de passe.
     *
     * @param password nouveau mot de passe
     */
    public void setPassword(String password) {
        this.password = password;
    }
}