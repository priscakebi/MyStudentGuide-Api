package com.mystudentguide.exception;

import com.mystudentguide.security.CompteTemporairementBloqueException;
import com.mystudentguide.servicepublic.ServicePublicIndisponibleException;
import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gestion centralisée des erreurs de l'API.
 *
 * Toutes les erreurs non gérées dans un controller sont renvoyées
 * au frontend dans un format JSON unique :
 *
 * {
 *   "timestamp": "...",
 *   "status": 400,
 *   "message": "Message lisible",
 *   "errors": { "champ": "message" }   // uniquement pour la validation
 * }
 *
 * Les controllers qui interceptent déjà leurs exceptions (try/catch)
 * conservent leur comportement actuel.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Erreurs de validation (@Valid) : 400 avec le détail champ par champ. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        Map<String, Object> body = body(HttpStatus.BAD_REQUEST, "Les données envoyées sont invalides.");
        body.put("errors", errors);

        return ResponseEntity.badRequest().body(body);
    }

    /** Corps JSON absent ou mal formé, ou paramètre de mauvais type : 400 (et non 500). */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) {
        return ResponseEntity.badRequest()
                .body(body(HttpStatus.BAD_REQUEST, "La requête est mal formée."));
    }

    /** Règle métier non respectée (ex : email déjà utilisé). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    /**
     * Trop de tentatives de connexion : 429 + Retry-After (en secondes),
     * pour que le front affiche le délai d'attente.
     */
    @ExceptionHandler(CompteTemporairementBloqueException.class)
    public ResponseEntity<Map<String, Object>> handleCompteBloque(CompteTemporairementBloqueException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getSecondesRestantes()))
                .body(body(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage()));
    }

    /** Échec d'authentification (identifiants incorrects, compte désactivé...) : 401. Message volontairement générique. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(body(HttpStatus.UNAUTHORIZED, "Adresse e-mail ou mot de passe incorrect."));
    }

    /** Utilisateur connecté mais sans les droits nécessaires : 403. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(body(HttpStatus.FORBIDDEN, "Vous n'avez pas les droits nécessaires."));
    }

    /** Ressource introuvable : 404. */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(body(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    /** Service-Public injoignable : 502, avec un message lisible pour l'admin. */
    @ExceptionHandler(ServicePublicIndisponibleException.class)
    public ResponseEntity<Map<String, Object>> handleServicePublic(ServicePublicIndisponibleException ex) {
        log.warn("Service-Public indisponible : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(body(HttpStatus.BAD_GATEWAY, ex.getMessage()));
    }

    /** Erreur levée volontairement avec un statut précis : on conserve ce statut. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return ResponseEntity.status(status).body(body(status, message));
    }

    /**
     * Toute autre erreur applicative : 500, sans exposer les détails techniques.
     * Les erreurs HTTP standard de Spring MVC (405, 415, 404 de ressource...) ne
     * sont pas des RuntimeException et gardent donc leur traitement par défaut.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(RuntimeException ex) {
        log.error("Erreur inattendue", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur interne est survenue."));
    }

    private Map<String, Object> body(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("message", message);
        return body;
    }
}
