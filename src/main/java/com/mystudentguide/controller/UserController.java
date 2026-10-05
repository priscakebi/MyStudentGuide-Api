package com.mystudentguide.controller;

import com.mystudentguide.dto.user.UserResponse;
import com.mystudentguide.dto.user.UserUpdateRequest;
import com.mystudentguide.entity.User;
import com.mystudentguide.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST permettant de gérer les utilisateurs.
 *
 * Ce contrôleur contient :
 *
 * - les routes d'administration des utilisateurs ;
 * - la consultation des informations du compte connecté ;
 * - la modification des informations du compte connecté.
 *
 * Les opérations d'administration sont réservées aux ADMIN.
 * Les opérations /me sont accessibles à l'utilisateur authentifié.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * Service utilisé pour effectuer les opérations
     * sur les utilisateurs.
     */
    private final UserService userService;


    /**
     * Constructeur permettant d'injecter le UserService.
     *
     * @param userService service de gestion des utilisateurs
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }


    // ============================================================
    // INFORMATIONS DU COMPTE CONNECTÉ
    // ============================================================

    /**
     * Récupère les informations du compte actuellement connecté.
     *
     * Endpoint :
     *
     * GET /api/users/me
     *
     * L'identité de l'utilisateur est récupérée depuis
     * l'authentification créée par le filtre JWT.
     *
     * authentication.getName() correspond à l'email
     * de l'utilisateur puisque User.getUsername()
     * retourne l'email.
     *
     * @param authentication utilisateur actuellement authentifié
     * @return informations du compte connecté
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication
    ) {

        // Récupération de l'email de l'utilisateur connecté.
        String email = authentication.getName();

        // Récupération des informations du compte
        // depuis le service.
        UserResponse user = userService.getCurrentUser(email);

        // Retour des informations au frontend.
        return ResponseEntity.ok(user);
    }


    /**
     * Modifie les informations du compte actuellement connecté.
     *
     * Endpoint :
     *
     * PUT /api/users/me
     *
     * L'utilisateur peut modifier :
     *
     * - son prénom ;
     * - son nom ;
     * - son adresse email.
     *
     * Le mot de passe n'est pas modifié ici.
     *
     * @param authentication utilisateur actuellement authentifié
     * @param updatedUser nouvelles informations du compte
     * @return informations mises à jour
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @RequestBody UserUpdateRequest updatedUser
    ) {

        // Récupération de l'email actuel de l'utilisateur connecté.
        String email = authentication.getName();

        // Modification des informations du compte
        // à travers le service.
        UserResponse updatedAccount =
                userService.updateCurrentUser(
                        email,
                        updatedUser
                );

        // Retour des informations mises à jour au frontend.
        return ResponseEntity.ok(updatedAccount);
    }


    // ============================================================
    // ADMINISTRATION DES UTILISATEURS
    // ============================================================

    /**
     * Récupère la liste de tous les utilisateurs.
     *
     * Endpoint :
     *
     * GET /api/users
     *
     * Cette route est réservée aux administrateurs.
     *
     * @return liste des utilisateurs
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {

        // Récupération de tous les utilisateurs.
        List<User> users = userService.getAllUsers();

        // Retour de la liste.
        return ResponseEntity.ok(users);
    }


    /**
     * Récupère un utilisateur à partir de son identifiant.
     *
     * Endpoint :
     *
     * GET /api/users/{id}
     *
     * La contrainte \\d+ signifie que la valeur de {id}
     * doit obligatoirement être composée de chiffres.
     *
     * Cela permet d'éviter un conflit entre :
     *
     * /api/users/me
     *
     * et :
     *
     * /api/users/{id}
     *
     * Sans cette contrainte, Spring pourrait essayer
     * de convertir "me" en Long, ce qui provoquerait
     * l'erreur :
     *
     * MethodArgumentTypeMismatchException
     *
     * @param id identifiant de l'utilisateur
     * @return utilisateur correspondant
     */
    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(
            @PathVariable Long id
    ) {

        // Recherche de l'utilisateur à partir de son ID.
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound().build()
                );
    }


    /**
     * Supprime un utilisateur à partir de son identifiant.
     *
     * Endpoint :
     *
     * DELETE /api/users/{id}
     *
     * Cette route est réservée aux administrateurs.
     *
     * La contrainte \\d+ permet également d'éviter
     * que /api/users/me soit interprété comme un ID.
     *
     * @param id identifiant de l'utilisateur
     * @return réponse HTTP 204 si la suppression est effectuée
     */
    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id
    ) {

        // Vérifie que l'utilisateur existe avant
        // d'effectuer la suppression.
        if (userService.getUserById(id).isEmpty()) {

            // 404 = utilisateur non trouvé.
            return ResponseEntity.notFound().build();
        }

        // Suppression de l'utilisateur.
        userService.deleteUser(id);

        // 204 = suppression effectuée avec succès
        // sans contenu à retourner.
        return ResponseEntity.noContent().build();
    }
}