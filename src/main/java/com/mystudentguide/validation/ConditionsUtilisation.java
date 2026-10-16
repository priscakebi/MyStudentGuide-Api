package com.mystudentguide.validation;

/**
 * Conditions d'utilisation et politique de confidentialité (RGPD).
 *
 * VERSION identifie le texte accepté par l'étudiant à l'inscription ;
 * elle est enregistrée avec la date d'acceptation (User.conditionsVersion,
 * User.conditionsAccepteesLe). À changer en même temps que les pages
 * /conditions-utilisation et /confidentialite du frontend
 * (src/app/pages/legal/legal-info.ts, constante VERSION_CONDITIONS).
 */
public final class ConditionsUtilisation {

    /** Version en vigueur (année-mois de publication). */
    public static final String VERSION = "2026-10";

    public static final String MESSAGE_REFUS =
            "Vous devez accepter les conditions d'utilisation et la politique de confidentialité.";

    private ConditionsUtilisation() {
    }
}
