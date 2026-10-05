package com.mystudentguide.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service responsable de la gestion des JWT.
 *
 * Il permet :
 * - de générer un token ;
 * - d'extraire l'email du token ;
 * - d'extraire les informations du token ;
 * - de vérifier qu'un token est valide ;
 * - de vérifier qu'un token n'est pas expiré.
 */
@Service
public class JwtService {

    /**
     * Clé secrète utilisée pour signer les JWT.
     *
     * Elle est récupérée depuis application.properties.
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Durée de validité du token en millisecondes.
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Vérifie au démarrage que le secret est assez long pour HS256
     * (32 octets minimum) : mieux vaut échouer tout de suite
     * qu'avec une erreur obscure à la première connexion.
     */
    @PostConstruct
    void validateConfiguration() {
        if (secretKey == null
                || secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "jwt.secret doit contenir au moins 32 caractères (variable JWT_SECRET)");
        }
    }

    /**
     * Génère un JWT pour un utilisateur.
     *
     * @param userDetails utilisateur authentifié
     * @return token JWT
     */
    public String generateToken(UserDetails userDetails) {

        Map<String, Object> extraClaims = new HashMap<>();

        return generateToken(extraClaims, userDetails);
    }

    /**
     * Génère un JWT avec des informations supplémentaires.
     *
     * @param extraClaims informations supplémentaires
     * @param userDetails utilisateur
     * @return token JWT
     */
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {

        /*
         * Nouvelle API JJWT 0.12.x.
         *
         * subject() remplace setSubject()
         * issuedAt() remplace setIssuedAt()
         * expiration() remplace setExpiration()
         */
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(
                        new Date(
                                System.currentTimeMillis() + jwtExpiration
                        )
                )
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrait le username du JWT.
     *
     * Dans MyStudentGuide, le username correspond
     * à l'adresse email.
     *
     * @param token JWT
     * @return email de l'utilisateur
     */
    public String extractUsername(String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    /**
     * Extrait un claim spécifique du JWT.
     *
     * @param token JWT
     * @param claimsResolver fonction permettant de récupérer
     *                       le claim souhaité
     * @param <T> type du résultat
     * @return valeur du claim
     */
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        final Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    /**
     * Extrait tous les claims du JWT.
     *
     * Nouvelle API JJWT 0.12.x :
     *
     * Jwts.parser()
     *     .verifyWith(...)
     *     .build()
     *     .parseSignedClaims(...)
     */
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Vérifie qu'un token appartient bien à l'utilisateur
     * et qu'il n'est pas expiré.
     *
     * @param token JWT
     * @param userDetails utilisateur
     * @return true si le token est valide
     */
    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        final String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    /**
     * Vérifie si le JWT est expiré.
     *
     * @param token JWT
     * @return true si le token est expiré
     */
    private boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());
    }

    /**
     * Récupère la date d'expiration du JWT.
     *
     * @param token JWT
     * @return date d'expiration
     */
    private Date extractExpiration(String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        );
    }

    /**
     * Transforme la clé secrète Base64 en clé cryptographique.
     *
     * @return clé utilisée pour signer et vérifier les JWT
     */
    private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(
            secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8)
    );
}
}