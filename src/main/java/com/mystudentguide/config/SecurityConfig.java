package com.mystudentguide.config;

import com.mystudentguide.security.CustomUserDetailsService;
import com.mystudentguide.security.JwtAuthenticationFilter;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration principale de Spring Security.
 *
 * Cette classe définit :
 *
 * - les routes publiques ;
 * - les routes protégées ;
 * - le système d'authentification ;
 * - le PasswordEncoder ;
 * - le fonctionnement sans session grâce au JWT ;
 * - le filtre JWT ;
 * - la configuration CORS pour Angular.
 *
 * Architecture :
 *
 * Angular
 * ↓
 * JWT
 * ↓
 * JwtAuthenticationFilter
 * ↓
 * Spring Security
 * ↓
 * Controller
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final CustomUserDetailsService userDetailsService;

        /**
         * Origines autorisées pour CORS (séparées par des virgules),
         * configurables via app.cors.allowed-origins / CORS_ALLOWED_ORIGINS.
         */
        @Value("${app.cors.allowed-origins:http://localhost:4200}")
        private List<String> allowedOrigins;

        /**
         * Injection des composants nécessaires à Spring Security.
         *
         * @param jwtAuthenticationFilter filtre permettant
         *                                de vérifier les JWT
         * @param userDetailsService      service permettant de retrouver
         *                                un utilisateur en base
         */
        public SecurityConfig(
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        CustomUserDetailsService userDetailsService) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.userDetailsService = userDetailsService;
        }

        /**
         * Configure la chaîne de sécurité de l'application.
         *
         * Les routes d'inscription et de connexion sont publiques.
         * Les autres routes nécessitent une authentification.
         *
         * @param http configuration HTTP de Spring Security
         * @return chaîne de filtres de sécurité
         */
        @Bean
        public SecurityFilterChain securityFilterChain(
                        org.springframework.security.config.annotation.web.builders.HttpSecurity http)
                        throws Exception {

                http

                                /*
                                 * Activation de CORS.
                                 *
                                 * Cela permet à notre frontend Angular
                                 * de communiquer avec le backend Spring Boot.
                                 */
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                /*
                                 * Désactivation de CSRF.
                                 *
                                 * Notre API utilise une authentification JWT
                                 * et non une authentification basée sur une session
                                 * avec des cookies.
                                 */
                                .csrf(csrf -> csrf.disable())

                                /*
                                 * MyStudentGuide utilise JWT.
                                 *
                                 * Le serveur ne conserve donc pas de session
                                 * utilisateur.
                                 */
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                /*
                                 * Requête non authentifiée sur une route protégée :
                                 * on répond 401 (et non 403) pour que le frontend
                                 * sache qu'il doit renvoyer l'utilisateur vers /login.
                                 */
                                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                                /*
                                 * Configuration de l'AuthenticationProvider.
                                 */
                                .authenticationProvider(authenticationProvider())

                                /*
                                 * Définition des routes accessibles ou protégées.
                                 */
                                .authorizeHttpRequests(auth -> auth
                                                // Page d'erreur de Spring : sans cette règle, toute
                                                // erreur (404, 500...) était renvoyée en 401, ce qui
                                                // déconnectait l'utilisateur côté front.
                                                .dispatcherTypeMatchers(DispatcherType.ERROR)
                                                .permitAll()
                                                .requestMatchers("/error")
                                                .permitAll()

                                                /*
                                                 * ============================================
                                                 * ROUTES PUBLIQUES
                                                 * ============================================
                                                 */

                                                // Inscription
                                                .requestMatchers("/api/auth/register")
                                                .permitAll()

                                                // Connexion
                                                .requestMatchers("/api/auth/login")
                                                .permitAll()
                                                .requestMatchers(
                                                                "/api/auth/forgot-password",
                                                                "/api/auth/reset-password")
                                                .permitAll()

                                                // Avis publics affichés sur la page d'accueil
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/avis/public",
                                                                "/api/avis/public/**")
                                                .permitAll()

                                                /*
                                                 * ============================================
                                                 * SWAGGER
                                                 * ============================================
                                                 */

                                                .requestMatchers(
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/api-docs/**")
                                                .permitAll()

                                                /*
                                                 * ============================================
                                                 * CONSULTATION DES CATÉGORIES
                                                 * ============================================
                                                 *
                                                 * Les étudiants peuvent consulter
                                                 * les catégories.
                                                 */

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/categories",
                                                                "/api/categories/**")
                                                .permitAll()

                                                /*
                                                 * ============================================
                                                 * GESTION DES CATÉGORIES
                                                 * ============================================
                                                 *
                                                 * Seul un ADMIN peut :
                                                 *
                                                 * - créer une catégorie ;
                                                 * - modifier une catégorie ;
                                                 * - supprimer une catégorie.
                                                 */

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/categories",
                                                                "/api/categories/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/categories/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/api/categories/**")
                                                .hasRole("ADMIN")

                                                /*
                                                 * ============================================
                                                 * CONSULTATION DES DÉMARCHES
                                                 * ============================================
                                                 *
                                                 * Les étudiants peuvent consulter
                                                 * les démarches.
                                                 */

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/demarches",
                                                                "/api/demarches/**")
                                                .permitAll()

                                                // Documents requis d'une démarche : visibles
                                                // par tous, inscrits ou non.
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/documents-requis/demarche/**")
                                                .permitAll()

                                                /*
                                                 * ============================================
                                                 * GESTION DES DÉMARCHES
                                                 * ============================================
                                                 *
                                                 * Seul un ADMIN peut gérer les démarches.
                                                 */

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/demarches",
                                                                "/api/demarches/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/demarches/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/demarches/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/api/demarches/**")
                                                .hasRole("ADMIN")

                                                /*
                                                 * ============================================
                                                 * TOUT LE RESTE
                                                 * ============================================
                                                 *
                                                 * Les autres endpoints nécessitent
                                                 * au minimum une authentification.
                                                 */

                                                .anyRequest()
                                                .authenticated())

                                /*
                                 * Notre filtre JWT doit être exécuté avant
                                 * le filtre standard UsernamePasswordAuthenticationFilter.
                                 */
                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        /**
         * Fournit le mécanisme utilisé par Spring Security
         * pour vérifier l'email et le mot de passe.
         *
         * @return AuthenticationProvider
         */
        @Bean
        public AuthenticationProvider authenticationProvider() {

                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

                provider.setPasswordEncoder(passwordEncoder());

                return provider;
        }

        /**
         * Encodeur de mots de passe.
         *
         * BCrypt transforme le mot de passe en une valeur
         * sécurisée avant son stockage en base.
         *
         * Exemple :
         *
         * motdepasse123
         * ↓
         * BCrypt
         * ↓
         * $2a$10$....
         *
         * @return PasswordEncoder
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        /**
         * Fournit l'AuthenticationManager utilisé
         * notamment par AuthService lors de la connexion.
         *
         * @param configuration configuration d'authentification
         * @return AuthenticationManager
         */
        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration configuration) throws Exception {

                return configuration.getAuthenticationManager();
        }

        /**
         * Configuration CORS.
         *
         * Elle permet à Angular de communiquer avec
         * notre API Spring Boot.
         *
         * En développement Angular fonctionne généralement
         * sur :
         *
         * http://localhost:4200
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration configuration = new CorsConfiguration();

                /*
                 * Origine autorisée pour Angular.
                 */
                configuration.setAllowedOrigins(allowedOrigins);

                /*
                 * Méthodes HTTP autorisées.
                 */
                configuration.setAllowedMethods(
                                List.of(
                                                "GET",
                                                "POST",
                                                "PUT",
                                                "PATCH",
                                                "DELETE",
                                                "OPTIONS"));

                /*
                 * Headers autorisés.
                 *
                 * Authorization est indispensable pour
                 * envoyer notre JWT.
                 */
                configuration.setAllowedHeaders(
                                List.of(
                                                "Authorization",
                                                "Content-Type",
                                                "Accept"));

                /*
                 * Headers que le frontend peut lire dans la réponse.
                 */
                configuration.setExposedHeaders(
                                List.of("Authorization"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                /*
                 * Cette configuration s'applique à toutes
                 * les routes de notre API.
                 */
                source.registerCorsConfiguration(
                                "/**",
                                configuration);

                return source;
        }
}