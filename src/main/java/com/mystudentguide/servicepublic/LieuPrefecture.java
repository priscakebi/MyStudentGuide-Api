package com.mystudentguide.servicepublic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lieu servant à trouver la préfecture d'un étudiant.
 *
 * La préfecture compétente (titre de séjour…) est celle du département
 * où l'étudiant HABITE : on utilise d'abord le code postal de son
 * adresse, puis, à défaut, sa ville d'études.
 */
public final class LieuPrefecture {

    private LieuPrefecture() {
    }

    /** Origine du lieu utilisé, affichée à l'étudiant. */
    public enum Base { ADRESSE, VILLE_ETUDES }

    /** Texte à chercher (« Saint-Denis 93200 », « 93200 » ou « Lyon ») et son origine. */
    public record Lieu(String recherche, Base base) {
    }

    /** Code postal français (5 chiffres isolés). */
    private static final Pattern CODE_POSTAL = Pattern.compile("\\b(\\d{5})\\b");

    private static final Pattern VILLE_SIMPLE = Pattern.compile("[\\p{L}][\\p{L}' -]*");

    /** Lieux à essayer dans l'ordre : adresse (domicile), puis ville d'études. */
    public static List<Lieu> candidats(String adresse, String villeEtudes) {
        List<Lieu> lieux = new ArrayList<>();

        String depuisAdresse = depuisAdresse(adresse);
        if (depuisAdresse != null) {
            lieux.add(new Lieu(depuisAdresse, Base.ADRESSE));
        }
        if (villeEtudes != null && !villeEtudes.isBlank()) {
            lieux.add(new Lieu(villeEtudes.trim(), Base.VILLE_ETUDES));
        }
        return lieux;
    }

    /**
     * « 12 Rue de la République 93200 Saint-Denis » → « Saint-Denis 93200 » ;
     * « 3 allée X 75013 Paris 13e » → « 75013 » (le code postal suffit) ;
     * sans code postal → null.
     */
    static String depuisAdresse(String adresse) {
        if (adresse == null || adresse.isBlank()) {
            return null;
        }
        // Dernier code postal de l'adresse ; la ville est le texte qui le suit,
        // jusqu'à la virgule suivante (« 69007 Lyon, France » → « Lyon »).
        Matcher m = CODE_POSTAL.matcher(adresse);
        String codePostal = null;
        int fin = -1;
        while (m.find()) {
            codePostal = m.group(1);
            fin = m.end();
        }
        if (codePostal == null) {
            return null;
        }
        String ville = adresse.substring(fin).split(",", 2)[0].trim();
        return VILLE_SIMPLE.matcher(ville).matches() ? ville + " " + codePostal : codePostal;
    }
}
