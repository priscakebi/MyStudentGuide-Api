package com.mystudentguide.servicepublic;

/**
 * Service-Public est injoignable ou a renvoyé une réponse inattendue.
 *
 * Traduite en HTTP 502 (Bad Gateway) par GlobalExceptionHandler,
 * avec un message lisible par l'administrateur.
 */
public class ServicePublicIndisponibleException extends RuntimeException {

    public ServicePublicIndisponibleException(String message) {
        super(message);
    }

    public ServicePublicIndisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
