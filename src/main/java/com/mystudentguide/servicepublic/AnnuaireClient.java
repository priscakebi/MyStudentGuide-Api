package com.mystudentguide.servicepublic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Trouve la préfecture compétente pour une ville, en deux appels :
 *
 * 1. geo.api.gouv.fr : ville → code du département (ex. : Lyon → 69) ;
 * 2. API Annuaire de l'administration (DILA) : département → préfecture
 *    (adresse, téléphone, horaires, prise de rendez-vous).
 *
 * Paris : les titres de séjour relèvent de la Préfecture de police,
 * cherchée à part (type de service « paris_ppp »).
 *
 * Les réponses sont gardées 24 h en mémoire : les préfectures
 * changent rarement, inutile de rappeler l'API à chaque page.
 */
@Component
public class AnnuaireClient {

    /** Code de département : 01 à 95, 2A, 2B, 971 à 976. */
    private static final Pattern DEPARTEMENT = Pattern.compile("^(\\d{2,3}|2A|2B)$");

    /** Code postal éventuellement saisi avec la ville (« Saint-Denis 93200 »). */
    private static final Pattern CODE_POSTAL = Pattern.compile("\\b(\\d{5})\\b");

    private static final Duration CACHE = Duration.ofHours(24);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    private final String geoUrl;
    private final String annuaireUrl;

    private record EntreeCache(Optional<PrefectureInfo> valeur, Instant expiration) {
    }

    private final Map<String, EntreeCache> cache = new ConcurrentHashMap<>();

    public AnnuaireClient(
            @Value("${app.geo.base-url:https://geo.api.gouv.fr}") String geoUrl,
            @Value("${app.annuaire.base-url:https://api-lannuaire.service-public.gouv.fr/api/explore/v2.1/catalog/datasets/api-lannuaire-administration/records}")
            String annuaireUrl
    ) {
        this.geoUrl = geoUrl;
        this.annuaireUrl = annuaireUrl;
    }

    /**
     * Préfecture compétente pour une ville (vide si la ville est inconnue).
     *
     * @throws ServicePublicIndisponibleException si une des API ne répond pas
     */
    public Optional<PrefectureInfo> prefecturePourVille(String ville) {

        String cle = ThemesEtudiant.normaliser(ville);
        if (cle.isBlank() || cle.length() > 100) {
            return Optional.empty();
        }

        EntreeCache enCache = cache.get(cle);
        if (enCache != null && enCache.expiration().isAfter(Instant.now())) {
            return enCache.valeur();
        }

        Optional<PrefectureInfo> resultat = chercher(ville.trim());
        cache.put(cle, new EntreeCache(resultat, Instant.now().plus(CACHE)));
        return resultat;
    }

    private Optional<PrefectureInfo> chercher(String ville) {

        // 1. Ville → département.
        //    Avec un code postal (« Saint-Denis 93200 ») : recherche exacte.
        //    Sinon : la commune la plus peuplée de ce nom (« Saint-Denis » seul
        //    donne La Réunion : la ville reconnue est affichée à l'étudiant).
        Matcher codePostal = CODE_POSTAL.matcher(ville);
        String cp = codePostal.find() ? codePostal.group(1) : null;
        String nom = CODE_POSTAL.matcher(ville).replaceAll("").replaceAll("[,()]", " ").trim();

        String recherche = cp != null
                ? "codePostal=" + cp + (nom.isBlank() ? "" : "&nom=" + encoder(nom))
                : "nom=" + encoder(nom);

        JsonNode communes = getJson(geoUrl + "/communes?" + recherche
                + "&fields=nom,codeDepartement&boost=population&limit=1");

        if (!communes.isArray() || communes.isEmpty()) {
            return Optional.empty();
        }

        String nomVille = communes.get(0).path("nom").asText(ville);
        String departement = communes.get(0).path("codeDepartement").asText("");

        // Validation stricte : la valeur est insérée dans la requête de l'annuaire.
        if (!DEPARTEMENT.matcher(departement).matches()) {
            return Optional.empty();
        }

        // 2. Département → préfecture.
        String where = "75".equals(departement)
                ? "pivot like \"paris_ppp\" and nom like \"Préfecture de police de Paris\""
                : "pivot like \"prefecture\" and code_insee_commune like \"" + departement + "*\"";

        JsonNode resultats = getJson(annuaireUrl
                + "?where=" + encoder(where)
                + "&select=" + encoder("nom,adresse,telephone,site_internet,url_service_public,plage_ouverture,formulaire_contact")
                + "&limit=1").path("results");

        if (!resultats.isArray() || resultats.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(convertir(resultats.get(0), nomVille, departement));
    }

    // ==============================================================
    // CONVERSION D'UN ENREGISTREMENT DE L'ANNUAIRE
    // ==============================================================

    /** Les champs adresse, téléphone... sont eux-mêmes du JSON encodé en texte. */
    PrefectureInfo convertir(JsonNode r, String ville, String departement) {

        JsonNode adresse = premier(lireTexteJson(r.path("adresse")));
        String ligneAdresse = adresse == null ? null : String.join(" ", List.of(
                adresse.path("numero_voie").asText(""),
                adresse.path("code_postal").asText(""),
                adresse.path("nom_commune").asText("")).stream().filter(s -> !s.isBlank()).toList());

        JsonNode telephone = premier(lireTexteJson(r.path("telephone")));

        String rendezVous = null;
        String site = null;
        for (JsonNode s : lireTexteJson(r.path("site_internet"))) {
            String valeur = s.path("valeur").asText("");
            if (!valeur.startsWith("http")) {
                continue;
            }
            if (rendezVous == null && s.path("libelle").asText("").toLowerCase().contains("rendez-vous")) {
                rendezVous = valeur;
            } else if (site == null) {
                site = valeur;
            }
        }

        List<String> horaires = new ArrayList<>();
        for (JsonNode p : lireTexteJson(r.path("plage_ouverture"))) {
            String debut = p.path("nom_jour_debut").asText("");
            String fin = p.path("nom_jour_fin").asText("");
            String jours = fin.isBlank() || fin.equals(debut) ? debut : debut + " au " + fin;

            String creneaux = String.join(" et ", List.of(
                    creneau(p.path("valeur_heure_debut_1").asText(""), p.path("valeur_heure_fin_1").asText("")),
                    creneau(p.path("valeur_heure_debut_2").asText(""), p.path("valeur_heure_fin_2").asText("")))
                    .stream().filter(c -> !c.isBlank()).toList());

            if (!jours.isBlank() && !creneaux.isBlank()) {
                horaires.add(jours + " : " + creneaux);
            }
        }

        return new PrefectureInfo(
                r.path("nom").asText(""),
                ville,
                departement,
                ligneAdresse,
                telephone == null ? null : telephone.path("valeur").asText(null),
                horaires,
                rendezVous,
                site,
                texteOuNull(r.path("formulaire_contact")),
                texteOuNull(r.path("url_service_public")),
                adresse == null ? null : texteOuNull(adresse.path("latitude")),
                adresse == null ? null : texteOuNull(adresse.path("longitude")),
                null
        );
    }

    private static String creneau(String debut, String fin) {
        if (debut.isBlank() || fin.isBlank()) {
            return "";
        }
        return debut.substring(0, Math.min(5, debut.length())) + " – " + fin.substring(0, Math.min(5, fin.length()));
    }

    private JsonNode lireTexteJson(JsonNode champ) {
        if (champ == null || champ.isNull() || champ.asText("").isBlank()) {
            return mapper.createArrayNode();
        }
        try {
            JsonNode n = champ.isTextual() ? mapper.readTree(champ.asText()) : champ;
            return n.isArray() ? n : mapper.createArrayNode();
        } catch (IOException e) {
            return mapper.createArrayNode();
        }
    }

    private static JsonNode premier(JsonNode tableau) {
        return tableau.isArray() && !tableau.isEmpty() ? tableau.get(0) : null;
    }

    private static String texteOuNull(JsonNode n) {
        String v = n == null || n.isNull() ? "" : n.asText("");
        return v.isBlank() ? null : v;
    }

    // ==============================================================
    // HTTP
    // ==============================================================

    private JsonNode getJson(String url) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ServicePublicIndisponibleException(
                        "L'annuaire de l'administration a répondu " + response.statusCode() + ".");
            }
            return mapper.readTree(response.body());

        } catch (IOException e) {
            throw new ServicePublicIndisponibleException("L'annuaire de l'administration est injoignable.", e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServicePublicIndisponibleException("Recherche de la préfecture interrompue.", e);
        }
    }

    private static String encoder(String valeur) {
        return URLEncoder.encode(valeur, StandardCharsets.UTF_8);
    }
}
