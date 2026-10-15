package com.mystudentguide.security;

/**
 * Trop de tentatives de connexion échouées : le compte est bloqué
 * quelques minutes (protection contre les attaques par force brute).
 *
 * Traduite en HTTP 429 (Too Many Requests) avec l'en-tête Retry-After
 * par GlobalExceptionHandler.
 */
public class CompteTemporairementBloqueException extends RuntimeException {

    private final long secondesRestantes;

    public CompteTemporairementBloqueException(long secondesRestantes) {
        super(message(secondesRestantes));
        this.secondesRestantes = secondesRestantes;
    }

    public long getSecondesRestantes() {
        return secondesRestantes;
    }

    private static String message(long secondes) {
        long minutes = Math.max(1, (secondes + 59) / 60);
        return "Trop de tentatives de connexion. Réessayez dans "
                + minutes + " minute" + (minutes > 1 ? "s" : "")
                + " ou réinitialisez votre mot de passe.";
    }
}
