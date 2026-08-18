package com.mystudentguide.dto.demarche;

import com.mystudentguide.enums.DemarcheStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO utilisé uniquement pour modifier le statut
 * d'une démarche.
 *
 * On utilise un DTO séparé afin d'éviter qu'une simple
 * modification du statut puisse également modifier le nom,
 * la description ou les relations de la démarche.
 */
public class DemarcheStatusRequest {

    /**
     * Nouveau statut de la démarche.
     */
    @NotNull(message = "Le nouveau statut est obligatoire")
    private DemarcheStatus statut;

    /**
     * Constructeur vide nécessaire à Spring.
     */
    public DemarcheStatusRequest() {
    }

    public DemarcheStatus getStatut() {
        return statut;
    }

    public void setStatut(DemarcheStatus statut) {
        this.statut = statut;
    }
}