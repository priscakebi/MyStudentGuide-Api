package com.mystudentguide.service;

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
     * L'injection par constructeur est recommandée avec Spring
     * car elle permet de rendre la dépendance obligatoire.
     *
     * @param userRepository repository des utilisateurs
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Récupère tous les utilisateurs.
     *
     * Cette méthode pourra notamment être utilisée par
     * l'administration.
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
     * Cette méthode sera notamment utilisée par
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
     * IMPORTANT :
     * Pour l'inscription réelle, le mot de passe devra être
     * chiffré avec BCrypt avant d'appeler cette méthode.
     *
     * Cette partie sera traitée lorsque nous développerons
     * l'authentification.
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
     * Cette opération sera principalement destinée
     * à l'administration.
     *
     * @param id identifiant de l'utilisateur
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}