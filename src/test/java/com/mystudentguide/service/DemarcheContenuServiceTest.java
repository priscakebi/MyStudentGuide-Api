package com.mystudentguide.service;

import com.mystudentguide.dto.demarche.ContenuDtos.Contenu;
import com.mystudentguide.dto.demarche.ContenuDtos.EtapeRequest;
import com.mystudentguide.dto.demarche.ContenuDtos.LienRequest;
import com.mystudentguide.dto.demarche.EtapeDemarcheDTO;
import com.mystudentguide.entity.Demarche;
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
class DemarcheContenuServiceTest {

    @Mock private DemarcheRepository demarcheRepository;

    @InjectMocks private DemarcheContenuService service;

    private Demarche demarche;

    @BeforeEach
    void setUp() {
        demarche = new Demarche();
        demarche.setId(1L);
        demarche.setTitre("Titre de séjour");

        demarche.getEtapes().add(etape(11L, 1, "Préparer le dossier"));
        demarche.getEtapes().add(etape(12L, 2, "Déposer la demande"));
        demarche.getEtapes().add(etape(13L, 3, "Récupérer le titre"));

        LienOfficiel lien = new LienOfficiel("ANEF", "https://anef.example", null, demarche);
        lien.setId(21L);
        demarche.getLiensOfficiels().add(lien);

        when(demarcheRepository.findById(1L)).thenReturn(Optional.of(demarche));
        lenient().when(demarcheRepository.saveAndFlush(any(Demarche.class))).thenAnswer(i -> i.getArgument(0));
    }

    private EtapeDemarche etape(Long id, int numero, String titre) {
        EtapeDemarche e = new EtapeDemarche(numero, titre, null, demarche);
        e.setId(id);
        return e;
    }

    private static List<String> titres(Contenu c) {
        return c.etapes().stream().map(EtapeDemarcheDTO::getTitre).toList();
    }

    private static List<Integer> numeros(Contenu c) {
        return c.etapes().stream().map(EtapeDemarcheDTO::getNumero).toList();
    }

    @Test
    void ajouteUneEtapeALaFin() {
        Contenu c = service.ajouterEtape(1L, new EtapeRequest("  Payer la taxe  ", "  En ligne.  "));

        assertThat(titres(c)).containsExactly(
                "Préparer le dossier", "Déposer la demande", "Récupérer le titre", "Payer la taxe");
        assertThat(numeros(c)).containsExactly(1, 2, 3, 4);
        assertThat(c.etapes().get(3).getDescription()).isEqualTo("En ligne.");
    }

    @Test
    void supprimerRenumeroteSansTrou() {
        Contenu c = service.supprimerEtape(1L, 12L);

        assertThat(titres(c)).containsExactly("Préparer le dossier", "Récupérer le titre");
        assertThat(numeros(c)).containsExactly(1, 2);
    }

    @Test
    void reordonner() {
        Contenu c = service.reordonnerEtapes(1L, List.of(13L, 11L, 12L));

        assertThat(titres(c)).containsExactly("Récupérer le titre", "Préparer le dossier", "Déposer la demande");
        assertThat(numeros(c)).containsExactly(1, 2, 3);
    }

    @Test
    void reordonnerRefuseUneListeIncomplete() {
        assertThatThrownBy(() -> service.reordonnerEtapes(1L, List.of(13L, 11L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void modifierUneEtapeDUneAutreDemarcheEstRefuse() {
        assertThatThrownBy(() -> service.modifierEtape(1L, 99L, new EtapeRequest("X", null)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void modifierEtSupprimerUnLien() {
        Contenu c = service.modifierLien(1L, 21L,
                new LienRequest("Portail ANEF", "https://administration-etrangers-en-france.interieur.gouv.fr/", " "));

        assertThat(c.liens()).hasSize(1);
        assertThat(c.liens().get(0).getNom()).isEqualTo("Portail ANEF");
        assertThat(c.liens().get(0).getDescription()).isNull();

        assertThat(service.supprimerLien(1L, 21L).liens()).isEmpty();
    }
}
