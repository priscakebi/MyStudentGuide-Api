package com.mystudentguide.config;

import com.mystudentguide.repository.CategorieRepository;
import com.mystudentguide.service.CategorieDefautService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Au premier démarrage (aucune catégorie en base), crée les 8
 * catégories de base : Séjour & visa, Santé, Logement, Études,
 * Banque & budget, Emploi, Transports, Vie quotidienne.
 *
 * Si des catégories existent déjà, rien n'est fait : une catégorie
 * supprimée par l'admin ne doit pas réapparaître au redémarrage.
 * L'admin peut ajouter celles qui manquent depuis « Catégories ».
 */
@Component
public class CategorieInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CategorieInitializer.class);

    private final CategorieRepository categorieRepository;
    private final CategorieDefautService categorieDefautService;

    public CategorieInitializer(
            CategorieRepository categorieRepository,
            CategorieDefautService categorieDefautService
    ) {
        this.categorieRepository = categorieRepository;
        this.categorieDefautService = categorieDefautService;
    }

    @Override
    public void run(String... args) {
        if (categorieRepository.count() == 0) {
            int creees = categorieDefautService.creerCategoriesManquantes();
            log.info("{} catégories de base créées.", creees);
        }

        // Version anglaise des catégories de base créées avant la traduction.
        int traduites = categorieDefautService.completerTraductions();
        if (traduites > 0) {
            log.info("{} catégories de base traduites en anglais.", traduites);
        }
    }
}
