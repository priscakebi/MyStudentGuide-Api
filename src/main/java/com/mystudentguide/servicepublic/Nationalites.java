package com.mystudentguide.servicepublic;

import java.util.List;

/**
 * Reconnaît, à partir de la nationalité saisie librement dans le profil
 * (« Ivoirienne », « allemand »...), si l'étudiant est ressortissant de
 * l'Union européenne, de l'Espace économique européen ou de la Suisse.
 *
 * Ces étudiants n'ont pas besoin de titre de séjour pour étudier en France :
 * les démarches de séjour ne doivent pas leur être suggérées.
 */
public final class Nationalites {

    private Nationalites() {
    }

    /**
     * Début des adjectifs de nationalité (sans accents, en minuscules) :
     * « allemand » couvre aussi « allemande ».
     * UE (27) + EEE (Islande, Liechtenstein, Norvège) + Suisse.
     */
    private static final List<String> UE_EEE_SUISSE = List.of(
            "allemand", "autrichien", "belge", "bulgare", "chypriote", "croate",
            "danois", "espagnol", "estonien", "finlandais", "francais", "grec",
            "hongrois", "irlandais", "italien", "letton", "lituanien",
            "luxembourgeois", "maltais", "neerlandais", "hollandais", "polonais",
            "portugais", "roumain", "slovaque", "slovene", "suedois", "tcheque",
            "islandais", "liechtensteinois", "norvegien", "suisse"
    );

    /**
     * @return true si hors UE/EEE/Suisse, false sinon,
     *         null si la nationalité n'est pas renseignée (inconnu).
     */
    public static Boolean estHorsUE(String nationalite) {
        String n = ThemesEtudiant.normaliser(nationalite);

        if (n.isBlank()) {
            return null;
        }

        return UE_EEE_SUISSE.stream().noneMatch(n::startsWith);
    }
}
