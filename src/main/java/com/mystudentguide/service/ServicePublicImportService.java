package com.mystudentguide.service;

import com.mystudentguide.entity.Categorie;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.EtapeDemarche;
import com.mystudentguide.entity.LienOfficiel;
import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.NotificationType;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.enums.StatutPublication;
import com.mystudentguide.repository.CategorieRepository;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.repository.UserRepository;
import com.mystudentguide.servicepublic.CatalogueEtudiant;
import com.mystudentguide.servicepublic.FicheServicePublic;
import com.mystudentguide.servicepublic.ServicePublicClient;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Import des démarches depuis les fiches officielles de Service-Public
 * (flux open data de la DILA, Licence Ouverte : la source et la date de
 * mise à jour doivent être affichées, voir DemarcheDetailDTO).
 *
 * Parcours :
 * 1. l'admin choisit une fiche et une catégorie → démarche en BROUILLON
 *    (titre, description, étapes, liens officiels remplis automatiquement) ;
 * 2. il la relit, complète les documents requis, puis la publie ;
 * 3. chaque nuit, les fiches importées sont comparées à Service-Public :
 *    si une fiche a été modifiée, les admins sont prévenus et peuvent
 *    appliquer la mise à jour en un clic.
 */
@Service
public class ServicePublicImportService {

    private static final Logger log = LoggerFactory.getLogger(ServicePublicImportService.class);

    private final ServicePublicClient client;
    private final DemarcheRepository demarcheRepository;
    private final CategorieRepository categorieRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final CategorieDefautService categorieDefautService;
    private final SuggestionAutoService suggestionAutoService;

    public ServicePublicImportService(
            ServicePublicClient client,
            DemarcheRepository demarcheRepository,
            CategorieRepository categorieRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            CategorieDefautService categorieDefautService,
            SuggestionAutoService suggestionAutoService
    ) {
        this.client = client;
        this.demarcheRepository = demarcheRepository;
        this.categorieRepository = categorieRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.categorieDefautService = categorieDefautService;
        this.suggestionAutoService = suggestionAutoService;
    }


    // ============================================================
    // CATALOGUE ET APERÇU
    // ============================================================

    public record FicheCatalogue(String id, String titre, String theme, boolean dejaImportee) {
    }

    /** Fiches suggérées, en indiquant celles déjà importées. */
    @Transactional(readOnly = true)
    public List<FicheCatalogue> getCatalogue() {

        Set<String> importees = demarcheRepository.findBySourceIdIsNotNull().stream()
                .map(Demarche::getSourceId)
                .collect(Collectors.toSet());

        return CatalogueEtudiant.FICHES.stream()
                .map(f -> new FicheCatalogue(f.id(), f.titre(), f.theme(), importees.contains(f.id())))
                .toList();
    }

    /**
     * Aperçu avant import, avec la catégorie suggérée.
     *
     * @param categorieSuggereeId catégorie pré-sélectionnée (null si aucune ne correspond)
     */
    public record ApercuFiche(FicheServicePublic fiche, Long categorieSuggereeId) {
    }

    /**
     * Contenu d'une fiche tel qu'il serait importé (rien n'est enregistré)
     * et catégorie suggérée : d'après le thème du catalogue s'il s'agit
     * d'une fiche suggérée, sinon d'après le thème officiel de la fiche.
     */
    public ApercuFiche apercu(String ficheId) {

        FicheServicePublic fiche = client.getFiche(ficheId);

        String themeCatalogue = CatalogueEtudiant.FICHES.stream()
                .filter(f -> f.id().equals(fiche.id()))
                .map(CatalogueEtudiant.FicheSuggeree::theme)
                .findFirst()
                .orElse(null);

        Long categorieId = categorieDefautService.suggerer(themeCatalogue, fiche.theme())
                .map(Categorie::getId)
                .orElse(null);

        return new ApercuFiche(fiche, categorieId);
    }


    // ============================================================
    // IMPORT
    // ============================================================

    /**
     * Crée une démarche en BROUILLON à partir d'une fiche.
     *
     * @throws IllegalArgumentException fiche déjà importée ou catégorie inconnue
     */
    @Transactional
    public Demarche importer(String ficheId, Long categorieId) {

        FicheServicePublic fiche = client.getFiche(ficheId);

        if (demarcheRepository.existsBySourceId(fiche.id())) {
            throw new IllegalArgumentException(
                    "La fiche " + fiche.id() + " a déjà été importée.");
        }

        Categorie categorie = categorieRepository.findById(categorieId)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable."));

        Demarche demarche = new Demarche();
        demarche.setTitre(tronquer(fiche.titre(), 150));
        demarche.setCategorie(categorie);
        demarche.setDateCreation(LocalDate.now());
        demarche.setStatutPublication(StatutPublication.BROUILLON);
        demarche.setSourceId(fiche.id());

        // Ciblage par défaut des fiches du catalogue (suggestions automatiques
        // une fois publiée ; modifiable par l'admin).
        CatalogueEtudiant.trouver(fiche.id())
                .map(CatalogueEtudiant.FicheSuggeree::ciblage)
                .ifPresent(ciblage -> ciblage.appliquerA(demarche));

        appliquerContenu(demarche, fiche);

        return demarcheRepository.save(demarche);
    }


    // ============================================================
    // PUBLICATION
    // ============================================================

    /**
     * @param demarche            démarche mise à jour
     * @param suggestionsEnvoyees étudiants qui viennent de recevoir la suggestion
     */
    public record ResultatPublication(Demarche demarche, int suggestionsEnvoyees) {
    }

    /**
     * Publie ou repasse en brouillon. À la publication, la démarche est
     * suggérée aux étudiants dont le profil correspond à son ciblage.
     */
    @Transactional
    public ResultatPublication changerPublication(Long demarcheId, StatutPublication statut) {
        Demarche demarche = getDemarche(demarcheId);
        demarche.setStatutPublication(statut);
        Demarche sauvee = demarcheRepository.save(demarche);

        int envoyees = statut == StatutPublication.PUBLIEE
                ? suggestionAutoService.suggererPourDemarche(sauvee)
                : 0;

        return new ResultatPublication(sauvee, envoyees);
    }


    // ============================================================
    // MISES À JOUR
    // ============================================================

    /**
     * Vérification quotidienne (4 h 30) des fiches importées.
     *
     * Une fiche modifiée sur Service-Public marque la démarche
     * « mise à jour disponible » et prévient les administrateurs
     * (une seule fois par nouvelle version).
     *
     * @return nombre de démarches pour lesquelles une mise à jour a été détectée
     */
    @Scheduled(cron = "${app.service-public.cron:0 30 4 * * *}")
    @Transactional
    public int verifierMisesAJour() {

        int nouvelles = 0;

        for (Demarche demarche : demarcheRepository.findBySourceIdIsNotNull()) {
            try {
                FicheServicePublic fiche = client.getFiche(demarche.getSourceId());
                LocalDate dateFiche = fiche.dateMaj();

                boolean plusRecente = dateFiche != null
                        && (demarche.getSourceDateMaj() == null || dateFiche.isAfter(demarche.getSourceDateMaj()));
                boolean dejaSignalee = dateFiche != null && dateFiche.equals(demarche.getSourceDateDisponible());

                if (plusRecente && !dejaSignalee) {
                    demarche.setSourceDateDisponible(dateFiche);
                    demarcheRepository.save(demarche);
                    prevenirAdmins(demarche, dateFiche);
                    nouvelles++;
                }

            } catch (RuntimeException e) {
                // Une fiche en erreur (supprimée, réseau...) ne bloque pas les autres.
                log.warn("Vérification de la fiche {} impossible : {}", demarche.getSourceId(), e.getMessage());
            }
        }

        return nouvelles;
    }

    /**
     * Remplace la description, les étapes et les liens officiels par
     * ceux de la version actuelle de la fiche.
     *
     * Le titre, la catégorie, la date limite, les documents requis et
     * le statut de publication sont conservés (choix de l'admin).
     */
    @Transactional
    public Demarche appliquerMiseAJour(Long demarcheId) {

        Demarche demarche = getDemarche(demarcheId);

        if (demarche.getSourceId() == null) {
            throw new IllegalArgumentException("Cette démarche n'a pas été importée depuis Service-Public.");
        }

        appliquerContenu(demarche, client.getFiche(demarche.getSourceId()));
        demarche.setSourceDateDisponible(null);

        return demarcheRepository.save(demarche);
    }


    // ============================================================
    // OUTILS
    // ============================================================

    /** Copie description, étapes, liens et informations de source. */
    private void appliquerContenu(Demarche demarche, FicheServicePublic fiche) {

        demarche.setDescription(fiche.description());
        demarche.setSourceUrl(fiche.url());
        demarche.setSourceDateMaj(fiche.dateMaj());

        // orphanRemoval : les anciennes étapes et liens sont supprimés.
        demarche.getEtapes().clear();
        int numero = 1;
        for (FicheServicePublic.Etape etape : fiche.etapes()) {
            demarche.getEtapes().add(
                    new EtapeDemarche(numero++, etape.titre(), etape.description(), demarche));
        }

        demarche.getLiensOfficiels().clear();
        if (fiche.url() != null && !fiche.url().isBlank()) {
            demarche.getLiensOfficiels().add(new LienOfficiel(
                    "Fiche officielle Service-Public",
                    fiche.url(),
                    "Source : Service-Public.gouv.fr (DILA)",
                    demarche));
        }
        for (FicheServicePublic.Lien lien : fiche.liens()) {
            demarche.getLiensOfficiels().add(
                    new LienOfficiel(lien.nom(), lien.url(), lien.description(), demarche));
        }
    }

    private void prevenirAdmins(Demarche demarche, LocalDate dateFiche) {
        for (User admin : userRepository.findByRole(RoleType.ADMIN)) {
            notificationService.saveNotification(
                    Notification.builder()
                            .titre("Mise à jour officielle disponible")
                            .message("La fiche Service-Public de la démarche \""
                                    + demarche.getTitre()
                                    + "\" a été modifiée le " + dateFiche
                                    + ". Relisez-la et appliquez la mise à jour depuis la gestion des démarches.")
                            .type(NotificationType.INFORMATION)
                            .lu(false)
                            .user(admin)
                            .build());
        }
    }

    private Demarche getDemarche(Long id) {
        return demarcheRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Démarche introuvable avec l'id : " + id));
    }

    private static String tronquer(String texte, int max) {
        return texte.length() <= max ? texte : texte.substring(0, max - 1).trim() + "…";
    }
}
