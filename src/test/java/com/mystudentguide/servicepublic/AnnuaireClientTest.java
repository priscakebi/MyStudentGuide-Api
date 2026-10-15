package com.mystudentguide.servicepublic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnnuaireClientTest {

    private final AnnuaireClient client = new AnnuaireClient("https://geo.invalid", "https://annuaire.invalid");

    /** Enregistrement réel de l'annuaire (Préfecture du Rhône) : les sous-champs sont du JSON en texte. */
    private static final String RHONE = """
        {
          "nom": "Préfecture - Rhône",
          "adresse": "[{\\"type_adresse\\": \\"Adresse\\", \\"numero_voie\\": \\"106 rue Pierre-Corneille\\", \\"code_postal\\": \\"69003\\", \\"nom_commune\\": \\"Lyon\\", \\"longitude\\": \\"4.84452\\", \\"latitude\\": \\"45.7602\\"}]",
          "telephone": "[{\\"valeur\\": \\"04 72 61 61 61\\", \\"description\\": \\"Serveur vocal\\"}]",
          "site_internet": "[{\\"libelle\\": \\"\\", \\"valeur\\": \\"https://www.rhone.gouv.fr/\\"}, {\\"libelle\\": \\"Prise de rendez-vous en ligne\\", \\"valeur\\": \\"https://www.rhone.gouv.fr/Prendre-un-rendez-vous\\"}]",
          "url_service_public": "https://lannuaire.service-public.gouv.fr/auvergne-rhone-alpes/rhone/97aee73f",
          "plage_ouverture": "[{\\"nom_jour_debut\\": \\"Lundi\\", \\"nom_jour_fin\\": \\"Vendredi\\", \\"valeur_heure_debut_1\\": \\"08:30:00\\", \\"valeur_heure_fin_1\\": \\"12:00:00\\", \\"valeur_heure_debut_2\\": \\"13:30:00\\", \\"valeur_heure_fin_2\\": \\"15:30:00\\"}]",
          "formulaire_contact": "https://www.rhone.gouv.fr/Nous-contacter"
        }
        """;

    @Test
    void convertitUnEnregistrementDeLAnnuaire() throws Exception {
        JsonNode record = new ObjectMapper().readTree(RHONE);

        PrefectureInfo p = client.convertir(record, "Lyon", "69");

        assertThat(p.nom()).isEqualTo("Préfecture - Rhône");
        assertThat(p.ville()).isEqualTo("Lyon");
        assertThat(p.departement()).isEqualTo("69");
        assertThat(p.adresse()).isEqualTo("106 rue Pierre-Corneille 69003 Lyon");
        assertThat(p.telephone()).isEqualTo("04 72 61 61 61");
        assertThat(p.horaires()).containsExactly("Lundi au Vendredi : 08:30 – 12:00 et 13:30 – 15:30");
        assertThat(p.rendezVous()).isEqualTo("https://www.rhone.gouv.fr/Prendre-un-rendez-vous");
        assertThat(p.siteInternet()).isEqualTo("https://www.rhone.gouv.fr/");
        assertThat(p.contact()).isEqualTo("https://www.rhone.gouv.fr/Nous-contacter");
        assertThat(p.latitude()).isEqualTo("45.7602");
    }

    @Test
    void champsAbsentsOuIllisiblesNeFontPasPlanter() throws Exception {
        JsonNode record = new ObjectMapper().readTree("""
            {"nom": "Préfecture - Test", "adresse": "pas du json", "telephone": null, "site_internet": ""}
            """);

        PrefectureInfo p = client.convertir(record, "Test", "01");

        assertThat(p.nom()).isEqualTo("Préfecture - Test");
        assertThat(p.adresse()).isNull();
        assertThat(p.telephone()).isNull();
        assertThat(p.horaires()).isEmpty();
        assertThat(p.rendezVous()).isNull();
    }

    @Test
    void villeVideOuTropLongueIgnoree() {
        // Aucun appel réseau : les adresses .invalid ne sont jamais contactées.
        assertThat(client.prefecturePourVille("   ")).isEmpty();
        assertThat(client.prefecturePourVille("x".repeat(101))).isEmpty();
    }
}
