package com.mystudentguide.service;

import com.mystudentguide.entity.Categorie;
import com.mystudentguide.repository.CategorieRepository;
import com.mystudentguide.servicepublic.ThemesEtudiant;
import com.mystudentguide.servicepublic.ThemesEtudiant.Theme;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Catégories de base (les 8 thèmes de la page d'accueil) et
 * suggestion de catégorie lors d'un import Service-Public.
 */
@Service
public class CategorieDefautService {

    private final CategorieRepository categorieRepository;

    public CategorieDefautService(CategorieRepository categorieRepository) {
        this.categorieRepository = categorieRepository;
    }

    /**
     * Crée les catégories de base qui n'existent pas encore (même nom).
     * Les catégories existantes ne sont jamais modifiées.
     *
     * @return nombre de catégories créées
     */
    @Transactional
    public int creerCategoriesManquantes() {
        int creees = 0;

        for (Theme theme : ThemesEtudiant.THEMES) {
            if (!categorieRepository.existsByNom(theme.nom())) {
                Categorie categorie = new Categorie();
                categorie.setNom(theme.nom());
                categorie.setDescription(theme.description());
                categorie.setNomEn(theme.nomEn());
                categorie.setDescriptionEn(theme.descriptionEn());
                categorie.setIcone(theme.icone());
                categorieRepository.save(categorie);
                creees++;
            }
        }

        return creees;
    }

    /**
     * Ajoute la version anglaise aux catégories de base qui n'en ont pas
     * encore (créées avant la traduction du contenu). Une traduction
     * saisie par l'admin n'est jamais remplacée.
     *
     * @return nombre de catégories complétées
     */
    @Transactional
    public int completerTraductions() {
        int completees = 0;

        for (Categorie categorie : categorieRepository.findAll()) {
            if (categorie.getNomEn() != null && !categorie.getNomEn().isBlank()) {
                continue;
            }
            Optional<Theme> theme = ThemesEtudiant.THEMES.stream()
                    .filter(t -> t.nom().equalsIgnoreCase(categorie.getNom()))
                    .findFirst();
            if (theme.isPresent()) {
                categorie.setNomEn(theme.get().nomEn());
                categorie.setDescriptionEn(theme.get().descriptionEn());
                completees++;
            }
        }

        return completees;
    }

    /**
     * Catégorie existante la plus adaptée à une fiche :
     * 1. une catégorie qui porte exactement le nom du thème ;
     * 2. sinon une catégorie dont le nom contient un mot-clé du thème
     *    (ex. : « Titre de séjour » pour Séjour & visa).
     *
     * Vide si aucune ne correspond : l'admin choisit lui-même.
     */
    @Transactional(readOnly = true)
    public Optional<Categorie> suggerer(String themeCatalogue, String themeOfficiel) {

        Optional<Theme> theme = ThemesEtudiant.trouver(themeCatalogue, themeOfficiel);

        if (theme.isEmpty()) {
            return Optional.empty();
        }

        List<Categorie> categories = categorieRepository.findAll();

        Optional<Categorie> exacte = categories.stream()
                .filter(c -> c.getNom() != null && c.getNom().equalsIgnoreCase(theme.get().nom()))
                .findFirst();

        if (exacte.isPresent()) {
            return exacte;
        }

        return categories.stream()
                .filter(c -> {
                    String nom = ThemesEtudiant.normaliser(c.getNom());
                    return theme.get().motsCles().stream().anyMatch(nom::contains);
                })
                .findFirst();
    }
}
