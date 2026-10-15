package com.mystudentguide.servicepublic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Pattern;

/**
 * Récupère une fiche pratique sur le flux open data de Service-Public
 * (« Le Comarquage », DILA, Licence Ouverte) :
 *
 * https://lecomarquage.service-public.gouv.fr/vdd/3.5/part/xml/F2231.xml
 *
 * Seuls les identifiants de fiche (F + chiffres) sont acceptés :
 * l'adresse appelée ne peut donc pas être détournée.
 */
@Component
public class ServicePublicClient {

    /** Identifiant d'une fiche pratique « Particuliers » (ex. : F2231). */
    public static final Pattern FICHE_ID = Pattern.compile("^F\\d{1,6}$");

    /** Taille maximale acceptée pour une fiche (les plus grosses font ~300 Ko). */
    private static final int TAILLE_MAX = 3 * 1024 * 1024;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final ServicePublicParser parser = new ServicePublicParser();

    private final String baseUrl;

    public ServicePublicClient(
            @Value("${app.service-public.base-url:https://lecomarquage.service-public.gouv.fr/vdd/3.5/part/xml/}")
            String baseUrl
    ) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    /**
     * Télécharge et analyse une fiche.
     *
     * @throws IllegalArgumentException identifiant invalide ou fiche introuvable
     * @throws ServicePublicIndisponibleException service indisponible
     */
    public FicheServicePublic getFiche(String ficheId) {

        String id = ficheId == null ? "" : ficheId.trim().toUpperCase();

        if (!FICHE_ID.matcher(id).matches()) {
            throw new IllegalArgumentException(
                    "Identifiant de fiche invalide : utilisez le format F2231.");
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + id + ".xml"))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/xml, text/xml")
                .GET()
                .build();

        try {
            HttpResponse<InputStream> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            try (InputStream body = response.body()) {

                if (response.statusCode() == 404) {
                    throw new IllegalArgumentException("La fiche " + id + " n'existe pas sur Service-Public.");
                }

                if (response.statusCode() != 200) {
                    throw new ServicePublicIndisponibleException(
                            "Service-Public a répondu " + response.statusCode() + " pour la fiche " + id + ".");
                }

                byte[] contenu = body.readNBytes(TAILLE_MAX + 1);

                if (contenu.length > TAILLE_MAX) {
                    throw new ServicePublicIndisponibleException("La fiche " + id + " est anormalement volumineuse.");
                }

                return parser.parse(new String(contenu, StandardCharsets.UTF_8));
            }

        } catch (IOException e) {
            throw new ServicePublicIndisponibleException("Service-Public est injoignable pour le moment.", e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServicePublicIndisponibleException("Récupération de la fiche interrompue.", e);
        }
    }
}
