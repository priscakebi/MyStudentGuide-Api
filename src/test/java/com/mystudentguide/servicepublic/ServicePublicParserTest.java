package com.mystudentguide.servicepublic;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicePublicParserTest {

    private final ServicePublicParser parser = new ServicePublicParser();

    private static String fiche() throws IOException {
        try (InputStream in = ServicePublicParserTest.class.getResourceAsStream("/servicepublic/F9999.xml")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void extraitLesInformationsPrincipales() throws IOException {
        FicheServicePublic fiche = parser.parse(fiche());

        assertThat(fiche.id()).isEqualTo("F9999");
        assertThat(fiche.titre()).isEqualTo("Étudiant étranger : titre de séjour");
        assertThat(fiche.url()).isEqualTo("https://www.service-public.gouv.fr/particuliers/vosdroits/F9999");
        assertThat(fiche.dateMaj()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(fiche.theme()).isEqualTo("Étranger - Europe");
        assertThat(fiche.description())
                .startsWith("Résumé de la fiche.")
                .contains("Si vous êtes étranger et voulez étudier en France");
    }

    @Test
    void unChapitreDevientUneEtapeSansDoublon() throws IOException {
        FicheServicePublic fiche = parser.parse(fiche());

        assertThat(fiche.etapes()).extracting(FicheServicePublic.Etape::titre)
                .containsExactly("Comment valider le VLS-TS ?", "Quelle démarche à la fin du VLS-TS ?");

        // Le texte du chapitre, sans son titre ni ses liens, un paragraphe par ligne.
        assertThat(fiche.etapes().get(0).description())
                .isEqualTo("Vous devez valider votre visa en ligne.\nPayer la taxe\nGarder l'attestation");
    }

    @Test
    void extraitLesLiensOfficielsSansDoublonNiLienDangereux() throws IOException {
        FicheServicePublic fiche = parser.parse(fiche());

        assertThat(fiche.liens()).extracting(FicheServicePublic.Lien::nom)
                .containsExactly(
                        "Valider un VLS-TS",
                        "Visas et cartes de séjour pour étudiant",
                        "Où s'adresser : Préfecture");

        assertThat(fiche.liens().get(0).description())
                .isEqualTo("Téléservice · Ministère chargé de l'intérieur");
    }

    @Test
    void refuseUnXmlAvecDoctype() {
        // Attaque XXE : tentative de lecture d'un fichier local.
        String xxe = "<?xml version=\"1.0\"?><!DOCTYPE p [<!ENTITY x SYSTEM \"file:///etc/passwd\">]>"
                + "<Publication ID=\"F1\"><dc:title>&x;</dc:title></Publication>";

        assertThatThrownBy(() -> parser.parse(xxe))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void leClientRefuseUnIdentifiantInvalide() {
        ServicePublicClient client = new ServicePublicClient("https://exemple.invalid/");

        for (String id : new String[] {"../../etc/passwd", "R123", "F", "F12a", "https://x"}) {
            assertThatThrownBy(() -> client.getFiche(id))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Identifiant de fiche invalide");
        }
    }
}
