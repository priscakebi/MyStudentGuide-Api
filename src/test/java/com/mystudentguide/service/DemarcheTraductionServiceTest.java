package com.mystudentguide.service;

import com.mystudentguide.dto.demarche.TraductionDtos.Element;
import com.mystudentguide.dto.demarche.TraductionDtos.Traduction;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.DocumentRequis;
import com.mystudentguide.entity.EtapeDemarche;
import com.mystudentguide.entity.LienOfficiel;
import com.mystudentguide.repository.DemarcheRepository;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemarcheTraductionServiceTest {

    @Mock private DemarcheRepository demarcheRepository;

    @InjectMocks private DemarcheTraductionService service;

    private Demarche demarche;
    private EtapeDemarche etape2;
    private DocumentRequis passeport;
    private LienOfficiel anef;

    @BeforeEach
    void setUp() {
        demarche = new Demarche();
        demarche.setId(1L);
        demarche.setTitre("Titre de séjour");
        demarche.setDescription("Demander son titre de séjour.");

        EtapeDemarche etape1 = new EtapeDemarche(1, "Préparer le dossier", null, demarche);
        etape1.setId(11L);
        etape2 = new EtapeDemarche(2, "Déposer la demande", "Sur l'ANEF.", demarche);
        etape2.setId(12L);
        // Ordre volontairement inversé : la lecture trie par numéro.
        demarche.getEtapes().add(etape2);
        demarche.getEtapes().add(etape1);

        passeport = new DocumentRequis("Passeport", null, null, demarche);
        passeport.setId(31L);
        demarche.getDocumentsRequis().add(passeport);

        anef = new LienOfficiel("ANEF", "https://anef.example", null, demarche);
        anef.setId(21L);
        demarche.getLiensOfficiels().add(anef);

        lenient().when(demarcheRepository.findById(1L)).thenReturn(Optional.of(demarche));
        lenient().when(demarcheRepository.saveAndFlush(any(Demarche.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void litLeTexteFrancaisEnRegardDeLaTraduction() {
        etape2.setTitreEn("Submit the application");

        Traduction t = service.lire(1L);

        assertThat(t.demarche().titreFr()).isEqualTo("Titre de séjour");
        assertThat(t.demarche().titre()).isNull();
        assertThat(t.etapes()).extracting(Element::titreFr)
                .containsExactly("Préparer le dossier", "Déposer la demande");
        assertThat(t.etapes().get(1).titre()).isEqualTo("Submit the application");
        assertThat(t.documents()).extracting(Element::titreFr).containsExactly("Passeport");
        assertThat(t.liens()).extracting(Element::titreFr).containsExactly("ANEF");
    }

    @Test
    void enregistreLesTraductionsEtIgnoreLesElementsInconnus() {
        Traduction recue = new Traduction(
                new Element(null, "ignoré", null, "  Residence permit  ", "Apply for your residence permit."),
                List.of(new Element(12L, null, null, "Submit the application", "  "),
                        new Element(999L, null, null, "Unknown", null)),
                List.of(new Element(31L, null, null, "Passport", "Valid passport.")),
                List.of(new Element(21L, null, null, "ANEF (online portal)", null)));

        Traduction t = service.enregistrer(1L, recue);

        assertThat(demarche.getTitreEn()).isEqualTo("Residence permit");
        assertThat(demarche.getTitre()).isEqualTo("Titre de séjour");
        assertThat(etape2.getTitreEn()).isEqualTo("Submit the application");
        assertThat(etape2.getDescriptionEn()).isNull();
        assertThat(passeport.getNomEn()).isEqualTo("Passport");
        assertThat(passeport.getDescriptionEn()).isEqualTo("Valid passport.");
        assertThat(anef.getNomEn()).isEqualTo("ANEF (online portal)");
        assertThat(t.etapes().get(1).titre()).isEqualTo("Submit the application");
    }

    @Test
    void unTexteVideEffaceLaTraduction() {
        demarche.setTitreEn("Old title");
        anef.setNomEn("Old link");

        service.enregistrer(1L, new Traduction(
                new Element(null, null, null, "", null), null, null,
                List.of(new Element(21L, null, null, "   ", null))));

        assertThat(demarche.getTitreEn()).isNull();
        assertThat(anef.getNomEn()).isNull();
    }

    @Test
    void demarcheIntrouvable() {
        when(demarcheRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.lire(2L)).isInstanceOf(EntityNotFoundException.class);
    }
}
