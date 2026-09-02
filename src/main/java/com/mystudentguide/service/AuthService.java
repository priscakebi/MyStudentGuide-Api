package com.mystudentguide.service;

import com.mystudentguide.dto.auth.AuthResponse;
import com.mystudentguide.dto.auth.LoginRequest;
import com.mystudentguide.dto.auth.RegisterRequest;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.UserRepository;
import com.mystudentguide.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service responsable de la gestion de l'authentification
 * et de l'inscription des utilisateurs.
 *
 * Il fait le lien entre :
 *
 * Controller
 *     ↓
 * AuthService
 *     ↓
 * UserRepository
 *     ↓
 * Base de données
 *
 * Pour la connexion :
 *
 * LoginRequest
 *     ↓
 * AuthenticationManager
 *     ↓
 * JwtService
 *     ↓
 * AuthResponse
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Injection des dépendances nécessaires à l'authentification.
     *
     * @param userRepository accès aux utilisateurs en base
     * @param passwordEncoder chiffrement des mots de passe
     * @param authenticationManager gestion de l'authentification
     * @param jwtService génération du token JWT
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Inscription d'un nouvel utilisateur.
     *
     * Le mot de passe reçu depuis Angular ne doit jamais
     * être enregistré directement en base de données.
     *
     * Il est d'abord encodé avec PasswordEncoder.
     *
     * @param request données envoyées lors de l'inscription
     * @return AuthResponse contenant les informations
     *         de l'utilisateur créé
     */
    public AuthResponse register(RegisterRequest request) {

        /*
         * Vérification que l'adresse email n'est pas
         * déjà utilisée par un autre utilisateur.
         */
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Cette adresse email est déjà utilisée"
            );
        }

        /*
         * Création de l'entité User.
         *
         * Les noms des setters doivent correspondre
         * exactement à ceux présents dans ton User.java.
         */
        User user = new User();

        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());

        /*
         * Le rôle ne doit pas être choisi librement par
         * l'utilisateur lors de son inscription.
         *
         * Il sera défini côté serveur.
         *
         * Adapte cette ligne au nom exact de ton enum.
         */
        user.setRole(RoleType.ETUDIANT);

        /*
         * Enregistrement de l'utilisateur en base.
         */
        User savedUser = userRepository.save(user);

        /*
         * Génération du JWT après création du compte.
         */
        String token = jwtService.generateToken(savedUser);

        /*
         * Retour au frontend.
         */
        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().toString()
        );
    }

    /**
     * Authentifie un utilisateur.
     *
     * Spring Security vérifie ici l'email et le mot de passe.
     *
     * @param request données de connexion
     * @return AuthResponse contenant le JWT
     */
    public AuthResponse login(LoginRequest request) {

        /*
         * Spring Security vérifie les identifiants.
         *
         * Si le mot de passe est incorrect ou si
         * l'utilisateur n'existe pas, une exception
         * d'authentification sera générée.
         */
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        /*
         * Si l'authentification réussit, on récupère
         * l'utilisateur depuis la base de données.
         */
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Utilisateur introuvable"
                        )
                );

        /*
         * Génération du token JWT.
         */
        String token = jwtService.generateToken(user);

        /*
         * Retour des informations nécessaires au frontend.
         */
        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole().toString()
        );
    }
}