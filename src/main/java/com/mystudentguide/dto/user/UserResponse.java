package com.mystudentguide.dto.user;

/**
 * DTO utilisé pour retourner les informations publiques
 * d'un utilisateur au frontend.
 *
 * IMPORTANT :
 * Le mot de passe n'est volontairement pas présent
 * dans cette classe.
 *
 * L'entité User contient des informations qui ne doivent
 * pas nécessairement être exposées directement par l'API.
 */
public class UserResponse {

    /**
     * Identifiant unique de l'utilisateur.
     */
    private Long id;

    /**
     * Adresse email de l'utilisateur.
     */
    private String email;

    /**
     * Prénom de l'utilisateur.
     */
    private String prenom;

    /**
     * Nom de famille de l'utilisateur.
     */
    private String nom;

    /**
     * Rôle de l'utilisateur.
     *
     * Exemple :
     * - ETUDIANT
     * - ADMIN
     */
    private String role;

    /** Faux tant que l'étudiant n'a pas cliqué sur le lien de confirmation. */
    private boolean emailVerified = true;

    /**
     * Constructeur vide.
     */
    public UserResponse() {
    }

    /**
     * Constructeur permettant de créer directement
     * une réponse utilisateur.
     *
     * @param id identifiant de l'utilisateur
     * @param email adresse email
     * @param prenom prénom
     * @param nom nom
     * @param role rôle
     */
    public UserResponse(
            Long id,
            String email,
            String prenom,
            String nom,
            String role
    ) {
        this.id = id;
        this.email = email;
        this.prenom = prenom;
        this.nom = nom;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}
