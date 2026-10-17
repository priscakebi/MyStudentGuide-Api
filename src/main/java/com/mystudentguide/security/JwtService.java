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

        // Un défi de double authentification n'ouvre jamais de session.
        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && extractClaim(token, claims -> claims.get(CLAIM_TYPE)) == null;
    }

    // ============================================================
    // DÉFI DE DOUBLE AUTHENTIFICATION
    // ============================================================

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_DEFI = "2fa";
    private static final long DUREE_DEFI_MS = 5 * 60 * 1000;

    /**
     * Jeton remis après un mot de passe correct quand la double authentification
     * est activée : il ne vaut que pour POST /api/auth/login/2fa, pendant 5 minutes.
     */
    public String genererDefiDeuxFacteurs(UserDetails userDetails) {
        long maintenant = System.currentTimeMillis();
        return Jwts.builder()
                .claim(CLAIM_TYPE, TYPE_DEFI)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(maintenant))
                .expiration(new Date(maintenant + DUREE_DEFI_MS))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Adresse e-mail d'un défi valide.
     *
     * @throws IllegalArgumentException si le défi est expiré, falsifié ou n'en est pas un
     */
    public String emailDuDefi(String defi) {
        try {
            Claims claims = extractAllClaims(defi);
            if (!TYPE_DEFI.equals(claims.get(CLAIM_TYPE))) {
                throw new IllegalArgumentException();
            }
            return claims.getSubject();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    "Votre demande de connexion a expiré. Saisissez de nouveau votre mot de passe.");
        }
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