package com.mystudentguide.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de la documentation Swagger / OpenAPI.
 *
 * Cette classe permet de configurer :
 *
 * - le titre et la version de notre API ;
 * - la description de l'API ;
 * - le système d'authentification JWT utilisé par l'API.
 *
 * Swagger permet ensuite de tester directement les endpoints
 * de l'API depuis une interface graphique.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Création de la configuration OpenAPI.
     *
     * Cette méthode indique à Swagger comment présenter
     * et documenter notre API.
     *
     * @return configuration OpenAPI de MyStudentGuide
     */
    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()

                /*
                 * Informations générales sur l'API.
                 *
                 * Ces informations apparaissent notamment
                 * en haut de l'interface Swagger.
                 */
                .info(new Info()
                        .title("MyStudentGuide API")
                        .version("1.0")
                        .description(
                                "API de gestion de MyStudentGuide"
                        )
                )

                /*
                 * Définition des composants de sécurité.
                 *
                 * Ici, on indique à Swagger que notre API
                 * utilise une authentification avec un token JWT.
                 */
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",

                                        /*
                                         * Configuration du système
                                         * d'authentification JWT.
                                         */
                                        new SecurityScheme()

                                                /*
                                                 * HTTP indique que le token
                                                 * sera envoyé dans l'en-tête
                                                 * Authorization de la requête HTTP.
                                                 */
                                                .type(SecurityScheme.Type.HTTP)

                                                /*
                                                 * "bearer" indique que nous
                                                 * utilisons le mécanisme Bearer.
                                                 *
                                                 * Exemple :
                                                 *
                                                 * Authorization: Bearer eyJhbGci...
                                                 */
                                                .scheme("bearer")

                                                /*
                                                 * Indique que le Bearer token
                                                 * utilisé est un JWT.
                                                 */
                                                .bearerFormat("JWT")
                                )
                )

                /*
                 * Indique à Swagger que les endpoints protégés
                 * utilisent le système d'authentification
                 * "bearerAuth" défini ci-dessus.
                 *
                 * Grâce à cette configuration, Swagger affiche
                 * normalement le bouton "Authorize 🔒".
                 */
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("bearerAuth")
                );
    }
}