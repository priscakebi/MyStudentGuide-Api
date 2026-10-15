package com.mystudentguide.enums;

/**
 * Raison pour laquelle un étudiant signale une démarche.
 */
public enum MotifSignalement {

    /** L'information n'est plus à jour (règle, montant, délai...). */
    INFO_OBSOLETE("Information obsolète"),

    /** Un lien officiel ne fonctionne plus. */
    LIEN_CASSE("Lien cassé"),

    /** Une étape ou un document manque. */
    INFO_MANQUANTE("Information manquante"),

    /** Erreur ou formulation incorrecte. */
    ERREUR("Erreur dans le contenu"),

    AUTRE("Autre");

    private final String libelle;

    MotifSignalement(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
