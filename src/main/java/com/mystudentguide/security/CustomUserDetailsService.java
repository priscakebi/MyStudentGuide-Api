package com.mystudentguide.security;

import com.mystudentguide.entity.User;
import com.mystudentguide.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service utilisé par Spring Security pour récupérer
 * les informations d'un utilisateur depuis la base de données.
 *
 * Dans MyStudentGuide, l'adresse email sert d'identifiant
 * de connexion.
 *
 * Fonctionnement :
 *
 * Email
 *   ↓
 * UserRepository
 *   ↓
 * User
 *   ↓
 * UserDetails
 *   ↓
 * Spring Security
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Injection du repository permettant d'accéder
     * aux utilisateurs.
     *
     * @param userRepository repository des utilisateurs
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Recherche un utilisateur à partir de son email.
     *
     * Cette méthode est appelée automatiquement par
     * Spring Security lors de l'authentification.
     *
     * @param email email de l'utilisateur
     * @return informations de sécurité de l'utilisateur
     * @throws UsernameNotFoundException si l'utilisateur
     *         n'existe pas
     */
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        /*
         * Recherche de l'utilisateur dans la base de données.
         */
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Utilisateur introuvable avec l'email : " + email
                        )
                );

        /*
         * On retourne l'utilisateur sous forme de UserDetails.
         *
         * Cela nécessite que notre entité User implémente
         * l'interface UserDetails.
         */
        return user;
    }
}