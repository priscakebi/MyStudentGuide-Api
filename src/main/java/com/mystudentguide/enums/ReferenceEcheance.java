package com.mystudentguide.enums;

/**
 * Date du profil de l'étudiant à partir de laquelle est calculée
 * la date limite personnelle d'une démarche (voir EcheancePersonnelle).
 *
 * Exemples :
 * - validation du VLS-TS : 3 mois après l'ARRIVEE_EN_FRANCE ;
 * - renouvellement du titre de séjour : 2 mois avant la FIN_TITRE_SEJOUR.
 */
public enum ReferenceEcheance {

    /** Date d'arrivée en France (StudentProfile.dateArrivee). */
    ARRIVEE_EN_FRANCE,

    /** Fin de validité du visa ou du titre de séjour actuel (StudentProfile.dateFinTitreSejour). */
    FIN_TITRE_SEJOUR
}
