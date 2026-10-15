package com.mystudentguide.dto.demarche;

import com.mystudentguide.entity.Demarche;
import com.mystudentguide.enums.LogementType;
import com.mystudentguide.enums.VisaType;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Ciblage d'une démarche pour les suggestions automatiques.
 *
 * Utilisé pour le ciblage par défaut des fiches du catalogue et pour
 * la modification par l'admin (PUT /api/admin/demarches/{id}/ciblage).
 *
 * @param active          suggestion automatique activée
 * @param horsUE          true : hors UE seulement, false : UE seulement, null : tous
 * @param premiereArrivee true : première arrivée, false : déjà installés, null : tous
 * @param visas           types de visa visés (vide : tous)
 * @param logements       types de logement visés (vide : tous)
 */
public record CiblageDTO(
        boolean active,
        Boolean horsUE,
        Boolean premiereArrivee,
        Set<VisaType> visas,
        Set<LogementType> logements
) {

    /** Ciblage désactivé. */
    public static final CiblageDTO AUCUN = new CiblageDTO(false, null, null, Set.of(), Set.of());

    /** Ciblage actif pour tous les profils, sans autre critère. */
    public static CiblageDTO actif() {
        return new CiblageDTO(true, null, null, Set.of(), Set.of());
    }

    public CiblageDTO pourHorsUE() {
        return new CiblageDTO(active, true, premiereArrivee, visas, logements);
    }

    public CiblageDTO pourPremiereArrivee() {
        return new CiblageDTO(active, horsUE, true, visas, logements);
    }

    public CiblageDTO pourLogements(LogementType... types) {
        return new CiblageDTO(active, horsUE, premiereArrivee, visas, Set.of(types));
    }

    /** Copie ce ciblage dans la démarche. */
    public void appliquerA(Demarche demarche) {
        demarche.setCibleActive(active);
        demarche.setCibleHorsUE(horsUE);
        demarche.setCiblePremiereArrivee(premiereArrivee);
        demarche.setCibleVisas(new HashSet<>(visas == null ? Set.of() : visas));
        demarche.setCibleLogements(new HashSet<>(logements == null ? Set.of() : logements));
    }

    /** Lit le ciblage d'une démarche. */
    public static CiblageDTO de(Demarche demarche) {
        return new CiblageDTO(
                demarche.isCiblageActif(),
                demarche.getCibleHorsUE(),
                demarche.getCiblePremiereArrivee(),
                demarche.getCibleVisas().isEmpty() ? Set.of() : EnumSet.copyOf(demarche.getCibleVisas()),
                demarche.getCibleLogements().isEmpty() ? Set.of() : EnumSet.copyOf(demarche.getCibleLogements())
        );
    }
}
