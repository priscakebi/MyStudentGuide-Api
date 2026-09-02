package com.mystudentguide.config;

import com.mystudentguide.security.CustomUserDetailsService;
import com.mystudentguide.security.JwtAuthenticationFilter;

import org.springframework.http.HttpMethod;

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
                                 * Configuration de l'AuthenticationProvider.
                                 */
                                .authenticationProvider(authenticationProvider())

                                /*
                                 * Définition des routes accessibles ou protégées.
                                 */
                                .authorizeHttpRequests(auth -> auth

                                                // Authentification
                                                .requestMatchers("/api/auth/register").permitAll()
                                                .requestMatchers("/api/auth/login").permitAll()

                                                // Swagger
                                                .requestMatchers(
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/api-docs/**")
                                                .permitAll()

                                                // Consultation des démarches et catégories
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/demarches",
                                                                "/api/demarches/**",
                                                                "/api/categories",
                                                                "/api/categories/**")
                                                .permitAll()

                                                // Création de démarches
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/demarches",
                                                                "/api/demarches/**")
                                                .authenticated()

                                                // Modification de démarches
                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/demarches/**")
                                                .authenticated()

                                                // Modification du statut
                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/demarches/**")
                                                .authenticated()

                                                // Suppression
                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/api/demarches/**")
                                                .authenticated()

                                                // Tout le reste
                                                .anyRequest().authenticated())

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
                configuration.setAllowedOrigins(
                                List.of("http://localhost:4200"));

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