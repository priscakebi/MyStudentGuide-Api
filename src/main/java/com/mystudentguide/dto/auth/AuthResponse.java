package com.mystudentguide.dto.auth;

/**
 * DTO représentant la réponse envoyée au client
 * après une authentification réussie.
 *
 * Contrairement à LoginRequest, qui contient les données
 * envoyées par l'utilisateur, AuthResponse contient les
 * données que le backend retourne à Angular.
 *
 * Exemple de réponse :
 *
 * {
 *     "token": "eyJhbGciOiJIUzI1NiJ9...",
 *     "userId": 1,
 *     "email": "etudiant@email.com",
 *     "role": "ETUDIANT"
 * }
 */
public class AuthResponse {

    /**
     * Token JWT permettant à l'utilisateur de s'authentifier
     * sur les prochaines requêtes vers l'API.
     */
    private String token;

    /**
     * Identifiant de l'utilisateur authentifié.
     */
    private Long userId;

    /**
     * Adresse email de l'utilisateur authentifié.
     */
    private String email;

    /**
     * Rôle de l'utilisateur.
     *
     * Exemple :
     * - ETUDIANT
     * - ADMIN
     */
    private String role;

    /**
     * Constructeur permettant de créer une réponse
     * d'authentification complète.
     *
     * @param token token JWT
     * @param userId identifiant de l'utilisateur
     * @param email adresse email
     * @param role rôle de l'utilisateur
     */
    public AuthResponse(
            String token,
            Long userId,
            String email,
            String role
    ) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    /**
     * true : le mot de passe est correct mais un code de l'application
     * d'authentification est encore demandé (aucun JWT n'est remis).
     */
    private boolean deuxFacteurs;

    /**
     * Défi à renvoyer avec le code à POST /api/auth/login/2fa (valable 5 minutes).
     */
    private String defi;

    /**
     * Réponse intermédiaire de la connexion quand la double authentification est activée.
     *
     * @param defi jeton de défi signé
     * @param email adresse email
     */
    public static AuthResponse deuxFacteursRequis(String defi, String email) {
        AuthResponse response = new AuthResponse(null, null, email, null);
        response.deuxFacteurs = true;
        response.defi = defi;
        return response;
    }

    public boolean isDeuxFacteurs() {
        return deuxFacteurs;
    }

    public String getDefi() {
        return defi;
    }

    /**
     * Retourne le token JWT.
     *
     * @return token JWT
     */
    public String getToken() {
        return token;
    }

    /**
     * Modifie le token JWT.
     *
     * @param token nouveau token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Retourne l'identifiant de l'utilisateur.
     *
     * @return identifiant utilisateur
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Modifie l'identifiant de l'utilisateur.
     *
     * @param userId nouvel identifiant
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Retourne l'adresse email.
     *
     * @return email
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
     * Retourne le rôle de l'utilisateur.
     *
     * @return rôle
     */
    public String getRole() {
        return role;
    }

    /**
     * Modifie le rôle de l'utilisateur.
     *
     * @param role nouveau rôle
     */
    public void setRole(String role) {
        this.role = role;
    }
}