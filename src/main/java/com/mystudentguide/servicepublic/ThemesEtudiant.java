package com.mystudentguide.servicepublic;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Les 8 grands thèmes de démarches de MyStudentGuide (les mêmes que
 * les cartes de la page d'accueil), utilisés comme catégories de base.
 *
 * Sert aussi à suggérer une catégorie lors d'un import Service-Public :
 * - par le thème du catalogue (ex. : « Santé ») ;
 * - sinon par le thème officiel de la fiche (ex. : « Social - Santé »).
 */
public final class ThemesEtudiant {

    private ThemesEtudiant() {
    }

    /**
     * @param nom          nom de la catégorie créée
     * @param description  description de la catégorie
     * @param icone        emoji associé
     * @param motsOfficiels mots-clés du thème officiel Service-Public correspondant
     * @param motsCles      mots-clés pour reconnaître une catégorie existante
     *                      créée par l'admin (ex. : « Titre de séjour »)
     */
    public record Theme(
            String nom,
            String description,
            String icone,
            List<String> motsOfficiels,
            List<String> motsCles
    ) {
    }

    public static final List<Theme> THEMES = List.of(
            new Theme("Séjour & visa",
                    "Visa long séjour, titre de séjour, renouvellement et préfecture.",
                    "🛂", List.of("etranger"), List.of("sejour", "visa", "titre", "prefecture", "etranger")),
            new Theme("Santé",
                    "Sécurité sociale, carte Vitale, mutuelle et médecin traitant.",
                    "🩺", List.of("sante"), List.of("sante", "securite sociale", "mutuelle", "medecin", "vitale")),
            new Theme("Logement",
                    "Logement Crous, aides au logement (CAF) et assurance habitation.",
                    "🏠", List.of("logement"), List.of("logement", "caf", "crous", "apl", "habitation")),
            new Theme("Études",
                    "Inscription, CVEC, bourses et vie universitaire.",
                    "🎓", List.of("scolarite"), List.of("etude", "universit", "inscription", "bourse", "scolarit", "cvec")),
            new Theme("Banque & budget",
                    "Compte bancaire, RIB et aides financières.",
                    "💶", List.of("argent"), List.of("banque", "bancaire", "budget", "argent", "financ")),
            new Theme("Emploi",
                    "Job étudiant, droit au travail et stages.",
                    "💼", List.of("travail"), List.of("emploi", "travail", "job", "stage")),
            new Theme("Transports",
                    "Abonnements, réductions jeunes et mobilité.",
                    "🚆", List.of("transport"), List.of("transport", "mobilit")),
            new Theme("Vie quotidienne",
                    "Téléphone, permis de conduire, impôts et démarches du quotidien.",
                    "📱", List.of("papiers", "loisirs"), List.of("quotidien", "telephone", "permis", "impot"))
    );

    /**
     * Thème correspondant au thème du catalogue (nom exact),
     * sinon au thème officiel de la fiche (ex. : « Social - Santé »).
     */
    public static Optional<Theme> trouver(String themeCatalogue, String themeOfficiel) {

        if (themeCatalogue != null) {
            Optional<Theme> parNom = THEMES.stream()
                    .filter(t -> t.nom().equalsIgnoreCase(themeCatalogue))
                    .findFirst();
            if (parNom.isPresent()) {
                return parNom;
            }
        }

        String officiel = normaliser(themeOfficiel);
        if (officiel.isEmpty()) {
            return Optional.empty();
        }

        return THEMES.stream()
                .filter(t -> t.motsOfficiels().stream().anyMatch(officiel::contains))
                .findFirst();
    }

    /** Minuscules, sans accents (« Santé » → « sante »). */
    public static String normaliser(String texte) {
        if (texte == null) {
            return "";
        }
        return Normalizer.normalize(texte, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
