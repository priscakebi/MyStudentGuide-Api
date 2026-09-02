package com.mystudentguide.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {


private final JwtService jwtService;
private final UserDetailsService userDetailsService;

public JwtAuthenticationFilter(
        JwtService jwtService,
        UserDetailsService userDetailsService) {

    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
}

@Override
protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");

    System.out.println("========== SECURITY DEBUG ==========");
    System.out.println("Méthode : " + request.getMethod());
    System.out.println("URL : " + request.getRequestURI());
    System.out.println("Authorization présent : " + (authHeader != null));

    /*
     * Aucun JWT envoyé.
     */
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {

        System.out.println("JWT : ABSENT");
        System.out.println("====================================");

        filterChain.doFilter(request, response);
        return;
    }

    /*
     * Extraction du JWT.
     */
    final String jwt = authHeader.substring(7);

    try {

        /*
         * Extraction de l'email depuis le JWT.
         */
        final String userEmail = jwtService.extractUsername(jwt);

        System.out.println("Email extrait du JWT : " + userEmail);

        /*
         * Si aucune authentification n'est encore présente.
         */
        if (userEmail != null
                && SecurityContextHolder
                        .getContext()
                        .getAuthentication() == null) {

            /*
             * Recherche de l'utilisateur en base.
             */
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(userEmail);

            System.out.println(
                    "Utilisateur trouvé : "
                            + userDetails.getUsername()
            );

            System.out.println(
                    "Authorities : "
                            + userDetails.getAuthorities()
            );

            /*
             * Vérification du JWT.
             */
            boolean tokenValid =
                    jwtService.isTokenValid(jwt, userDetails);

            System.out.println(
                    "Token valide : " + tokenValid
            );

            /*
             * Si le token est valide,
             * on authentifie l'utilisateur.
             */
            if (tokenValid) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);

                System.out.println(
                        "AUTHENTIFICATION : OK"
                );

            } else {

                System.out.println(
                        "AUTHENTIFICATION : REFUSÉE - JWT INVALIDE"
                );
            }
        }

    } catch (Exception exception) {

        System.out.println(
                "ERREUR JWT : "
                        + exception.getClass().getSimpleName()
        );

        System.out.println(
                "Message : "
                        + exception.getMessage()
        );
    }

    System.out.println("====================================");

    filterChain.doFilter(request, response);
}


}
