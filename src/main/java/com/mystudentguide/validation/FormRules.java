package com.mystudentguide.validation;

/**
 * Règles de validation des formulaires, partagées par les DTO.
 *
 * ============================================================
 * MOT DE PASSE (recommandations CNIL / ANSSI)
 * ============================================================
 *
 * - 12 caractères minimum ;
 * - 72 caractères maximum (limite de BCrypt) ;
 * - au moins une minuscule, une majuscule, un chiffre
 *   et un caractère spécial.
 *
 * Ces règles s'appliquent à la CRÉATION et au CHANGEMENT
 * de mot de passe, jamais à la connexion : un compte créé
 * avec l'ancienne règle (8 caractères) peut toujours se connecter.
 *
 * Les mêmes règles existent côté Angular (utils/form-rules.ts).
 */
public final class FormRules {

    private FormRules() {
    }

    public static final int PASSWORD_MIN = 12;
    public static final int PASSWORD_MAX = 72;

    /**
     * Minuscule + majuscule + chiffre + caractère spécial, 12 à 72 caractères.
     * Les lettres accentuées comptent (É est une majuscule, é une minuscule).
     */
    public static final String PASSWORD_REGEX =
            "^(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*\\d)(?=.*[^\\p{L}\\p{N}\\s]).{12,72}$";

    public static final String PASSWORD_MESSAGE =
            "Le mot de passe doit contenir entre 12 et 72 caractères, "
                    + "dont une majuscule, une minuscule, un chiffre "
                    + "et un caractère spécial.";

    /** Domaine complet obligatoire (ex. : nom@domaine.fr). */
    public static final String EMAIL_REGEX =
            "^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$";

    public static final int EMAIL_MAX = 254;

    /** Lettres (accents compris), espaces, tirets et apostrophes, 2 à 50 caractères. */
    public static final String NAME_REGEX =
            "^\\p{L}[\\p{L}' -]{1,49}$";

    public static final String NAME_MESSAGE =
            "doit contenir entre 2 et 50 lettres "
                    + "(espaces, tirets et apostrophes autorisés).";

    /** Vrai si le mot de passe respecte la politique. */
    public static boolean isStrongPassword(String password) {
        return password != null && password.matches(PASSWORD_REGEX);
    }
}
