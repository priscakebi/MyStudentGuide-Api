package com.mystudentguide.service;

import com.mystudentguide.entity.Categorie;
import com.mystudentguide.repository.CategorieRepository;
import com.mystudentguide.servicepublic.ThemesEtudiant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategorieDefautServiceTest {

    @Mock private CategorieRepository categorieRepository;

    @InjectMocks private CategorieDefautService service;

    private static Categorie categorie(long id, String nom) {
        Categorie c = new Categorie();
        c.setId(id);
        c.setNom(nom);
        return c;
    }

    @Test
    void creeSeulementLesCategoriesManquantes() {
        when(categorieRepository.existsByNom(anyString())).thenReturn(false);
        when(categorieRepository.existsByNom("Santé")).thenReturn(true);

        int creees = service.creerCategoriesManquantes();

        assertThat(creees).isEqualTo(ThemesEtudiant.THEMES.size() - 1);
        verify(categorieRepository, times(ThemesEtudiant.THEMES.size() - 1)).save(any(Categorie.class));
    }

    @Test
    void suggereLaCategorieDuThemeDuCatalogue() {
        when(categorieRepository.findAll()).thenReturn(List.of(
                categorie(1, "Séjour & visa"), categorie(2, "Santé"), categorie(3, "Banque & budget")));

        assertThat(service.suggerer("Banque & budget", "Argent - Impôts - Consommation"))
                .get().extracting(Categorie::getId).isEqualTo(3L);
    }

    @Test
    void suggereDApresLeThemeOfficielQuandLaFicheNestPasAuCatalogue() {
        when(categorieRepository.findAll()).thenReturn(List.of(
                categorie(1, "Séjour & visa"), categorie(2, "Santé")));

        assertThat(service.suggerer(null, "Social - Santé"))
                .get().extracting(Categorie::getId).isEqualTo(2L);
    }

    @Test
    void reconnaitUneCategorieCreeeParLAdmin() {
        // Pas de « Séjour & visa », mais une catégorie « Titre de séjour ».
        when(categorieRepository.findAll()).thenReturn(List.of(
                categorie(7, "Logement"), categorie(8, "Titre de séjour")));

        assertThat(service.suggerer(null, "Étranger - Europe"))
                .get().extracting(Categorie::getId).isEqualTo(8L);
    }

    @Test
    void aucuneSuggestionSiRienNeCorrespond() {
        assertThat(service.suggerer(null, "Justice")).isEmpty();
    }

    @Test
    void traduitLesCategoriesDeBaseSansToucherAuxTraductionsDeLAdmin() {
        Categorie sante = categorie(1, "Santé");
        Categorie logement = categorie(2, "Logement");
        logement.setNomEn("Accommodation");
        Categorie perso = categorie(3, "Titre de séjour");
        when(categorieRepository.findAll()).thenReturn(List.of(sante, logement, perso));

        assertThat(service.completerTraductions()).isEqualTo(1);
        assertThat(sante.getNomEn()).isEqualTo("Health");
        assertThat(sante.getDescriptionEn()).isNotBlank();
        assertThat(logement.getNomEn()).isEqualTo("Accommodation");
        assertThat(perso.getNomEn()).isNull();
    }
}
