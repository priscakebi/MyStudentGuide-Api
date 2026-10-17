package com.mystudentguide.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Conseil pratique rattaché à une démarche, rédigé par l'admin
 * (ex. « Les créneaux de l'ANEF se libèrent souvent tôt le matin »).
 *
 * Affiché dans l'encadré « Conseils pratiques » de la fiche et,
 * pour les démarches suivies, dans « Astuce du jour » (Mon espace).
 */
@Entity
@Table(name = "conseils_demarche")
public class ConseilDemarche {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String texte;

    /** Version anglaise (facultative : à défaut, le texte français est affiché). */
    @Column(name = "texte_en", length = 500)
    private String texteEn;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demarche_id", nullable = false)
    @JsonIgnore
    private Demarche demarche;

    public ConseilDemarche() {
    }

    public ConseilDemarche(String texte, String texteEn, Demarche demarche) {
        this.texte = texte;
        this.texteEn = texteEn;
        this.demarche = demarche;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public String getTexteEn() {
        return texteEn;
    }

    public void setTexteEn(String texteEn) {
        this.texteEn = texteEn;
    }

    public Demarche getDemarche() {
        return demarche;
    }

    public void setDemarche(Demarche demarche) {
        this.demarche = demarche;
    }
}
