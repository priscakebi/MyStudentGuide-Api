package com.mystudentguide.service;

import com.mystudentguide.dto.user.UserResponse;
import com.mystudentguide.dto.user.UserUpdateRequest;
import com.mystudentguide.entity.User;
import com.mystudentguide.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service responsable de la gestion des utilisateurs.
 *
 * Cette classe contient la logique métier liée aux utilisateurs.
 *
 * Architecture :
 *
 * Controller
 *     ↓
 * UserService
 *     ↓
 * UserRepository
 *     ↓
 * Database
 */
@Service
public class UserService {

    /**
     * Repository permettant d'accéder aux utilisateurs
     * enregistrés en base de données.
     */
    private final UserRepository userRepository;


    /**
     * Injection du UserRepository par constructeur.
     *
     * @param userRepository repository des utilisateurs
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // ============================================================
    // ADMINISTRATION DES UTILISATEURS
    // ============================================================

    /**
     * Récupère tous les utilisateurs.
     *
     * @return liste de tous les utilisateurs
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    /**
     * Recherche un utilisateur à partir de son identifiant.
     *
     * @param id identifiant de l'utilisateur
     * @return utilisateur trouvé ou Optional.empty()
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }


    /**
     * Recherche un utilisateur à partir de son adresse email.
     *
     * Cette méthode est notamment utilisée par
     * le système d'authentification.
     *
     * @param email adresse email de l'utilisateur
     * @return utilisateur trouvé ou Optional.empty()
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    /**
     * Vérifie si une adresse email est déjà utilisée.
     *
     * @param email adresse email à vérifier
     * @return true si l'email existe déjà
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }


    /**
     * Enregistre un nouvel utilisateur ou sauvegarde
     * les modifications d'un utilisateur existant.
     *
     * @param user utilisateur à sauvegarder
     * @return utilisateur enregistré
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }


    /**
     * Supprime un utilisateur à partir de son identifiant.
     *
     * @param id identifiant de l'utilisateur
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


    // ============================================================
    // COMPTE DE L'UTILISATEUR CONNECTÉ
    // ============================================================

    /**
     * Récupère les informations du compte actuellement connecté.
     *
     * L'email correspond à l'utilisateur authentifié par le JWT.
     *
     * @param email email de l'utilisateur connecté
     * @return informations du compte sous forme de UserResponse
     */
    public UserResponse getCurrentUser(String email) {

        // Recherche de l'utilisateur dans la base de données.
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Utilisateur introuvable"
                        )
                );

        // Conversion de l'entité User en UserResponse.
        //
        // L'ordre des paramètres correspond exactement
        // au constructeur de UserResponse :
        // id, email, prenom, nom, role.
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getPrenom(),
                user.getNom(),
                user.getRole().name()
        );
    }


    /**
     * Modifie les informations du compte actuellement connecté.
     *
     * L'utilisateur peut modifier :
     *
     * - son prénom ;
     * - son nom ;
     * - son adresse email.
     *
     * Le mot de passe n'est pas modifié par cette méthode.
     *
     * @param email email actuel de l'utilisateur connecté
     * @param updatedUser nouvelles informations du compte
     * @return informations mises à jour
     */
    public UserResponse updateCurrentUser(
            String email,
            UserUpdateRequest updatedUser
    ) {

        // Recherche de l'utilisateur actuellement connecté.
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Utilisateur introuvable"
                        )
                );

        // Mise à jour du prénom.
        user.setPrenom(updatedUser.getPrenom());

        // Mise à jour du nom.
        user.setNom(updatedUser.getNom());

        // Mise à jour de l'adresse email.
        user.setEmail(updatedUser.getEmail());

        // Sauvegarde des modifications dans la base de données.
        User savedUser = userRepository.save(user);

        // Conversion de l'utilisateur sauvegardé
        // en UserResponse pour le frontend.
        return new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getPrenom(),
                savedUser.getNom(),
                savedUser.getRole().name()
        );
    }
}