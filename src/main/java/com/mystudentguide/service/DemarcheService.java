package com.mystudentguide.service;

import com.mystudentguide.dto.demarche.DemarcheDetailDTO;
import com.mystudentguide.dto.demarche.DemarcheRequest;
import com.mystudentguide.dto.demarche.DocumentRequisDTO;
import com.mystudentguide.dto.demarche.EtapeDemarcheDTO;
import com.mystudentguide.dto.demarche.LienOfficielDTO;
import com.mystudentguide.entity.Categorie;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.DocumentRequis;
import com.mystudentguide.entity.EtapeDemarche;
import com.mystudentguide.entity.LienOfficiel;
import com.mystudentguide.repository.CategorieRepository;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.repository.SuggestionEnvoyeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service permettant de gérer les démarches administratives
 * générales de MyStudentGuide.
 *
 * ============================================================
 * RÔLE DU SERVICE
 * ============================================================
 *
 * Cette classe contient la logique métier concernant les
 * démarches administratives générales.
 *
 * Elle permet notamment de :
 *
 * - récupérer toutes les démarches ;
 * - récupérer une démarche par son identifiant ;
 * - récupérer le détail complet d'une démarche ;
 * - rechercher les démarches d'une catégorie ;
 * - créer une démarche ;
 * - modifier une démarche ;
 * - supprimer une démarche.
 *
 * ============================================================
 * IMPORTANT
 * ============================================================
 *
 * Une Demarche représente une procédure générale.
 *
 * Elle ne contient PAS le statut personnel d'un étudiant.
 *
 * Le suivi personnel d'une démarche est géré par :
 *
 * StudentProfile
 *       ↓
 * StudentDemarche
 *
 * ============================================================
 * DOCUMENTS
 * ============================================================
 *
 * DocumentRequis représente uniquement la liste des documents
 * nécessaires pour effectuer une démarche.
 *
 * MyStudentGuide ne stocke pas les fichiers personnels
 * des étudiants.
 */
@Service
public class DemarcheService {

    /**
     * Repository permettant d'accéder aux démarches
     * enregistrées en base de données.
     */
    private final DemarcheRepository demarcheRepository;

    /**
     * Repository permettant de rechercher les catégories.
     */
    private final CategorieRepository categorieRepository;

    /** Historique des suggestions automatiques (nettoyé à la suppression). */
    private final SuggestionEnvoyeeRepository suggestionEnvoyeeRepository;


    /**
     * Constructeur du service.
     *
     * Spring injecte automatiquement les repositories.
     *
     * @param demarcheRepository repository des démarches
     * @param categorieRepository repository des catégories
     */
    public DemarcheService(
            DemarcheRepository demarcheRepository,
            CategorieRepository categorieRepository,
            SuggestionEnvoyeeRepository suggestionEnvoyeeRepository
    ) {
        this.demarcheRepository = demarcheRepository;
        this.categorieRepository = categorieRepository;
        this.suggestionEnvoyeeRepository = suggestionEnvoyeeRepository;
    }


    // ============================================================
    // RÉCUPÉRER TOUTES LES DÉMARCHES
    // ============================================================

    /**
     * Récupère toutes les démarches disponibles.
     *
     * Cette méthode est utilisée notamment pour afficher
     * la liste générale des démarches dans l'application.
     *
     * Les brouillons (fiches importées pas encore validées)
     * ne sont renvoyés qu'aux administrateurs.
     *
     * @param inclureBrouillons vrai pour un administrateur
     * @return liste des démarches
     */
    public List<Demarche> getAllDemarches(boolean inclureBrouillons) {

        /*
         * findAll() est fourni automatiquement par
         * Spring Data JPA grâce à JpaRepository.
         */
        List<Demarche> demarches = demarcheRepository.findAll();

        return inclureBrouillons
                ? demarches
                : demarches.stream().filter(Demarche::isPubliee).toList();
    }


    // ============================================================
    // RÉCUPÉRER UNE DÉMARCHE PAR SON ID
    // ============================================================

    /**
     * Récupère une démarche à partir de son identifiant.
     *
     * Cette méthode est utilisée par plusieurs autres méthodes
     * du service lorsqu'elles ont besoin de récupérer
     * une démarche existante.
     *
     * @param id identifiant de la démarche
     * @return la démarche correspondante
     * @throws RuntimeException si la démarche n'existe pas
     */
    public Demarche getDemarcheById(Long id) {

        // EntityNotFoundException → 404 (GlobalExceptionHandler) au lieu d'une 500.
        return demarcheRepository.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                "Démarche introuvable avec l'id : " + id
                        )
                );
    }


    // ============================================================
    // RÉCUPÉRER LES DÉMARCHES D'UNE CATÉGORIE
    // ============================================================

    /**
     * Récupère toutes les démarches appartenant
     * à une catégorie donnée.
     *
     * Exemple :
     *
     * GET /api/demarches/categorie/1
     *
     * @param categorieId identifiant de la catégorie
     * @return liste des démarches de cette catégorie
     * @throws RuntimeException si la catégorie n'existe pas
     */
    public List<Demarche> getDemarchesByCategorie(
            Long categorieId
    ) {

        /*
         * Vérification de l'existence de la catégorie.
         *
         * Cela permet d'éviter de rechercher des démarches
         * pour une catégorie inexistante.
         */
        if (!categorieRepository.existsById(categorieId)) {

            throw new RuntimeException(
                    "Catégorie introuvable avec l'id : "
                            + categorieId
            );
        }

        /*
         * Recherche des démarches appartenant
         * à cette catégorie.
         *
         * Cette méthode doit être présente dans
         * DemarcheRepository.
         */
        return demarcheRepository
                .findByCategorieId(categorieId);
    }


    // ============================================================
    // RÉCUPÉRER LE DÉTAIL COMPLET D'UNE DÉMARCHE
    // ============================================================

    /**
     * Récupère le détail complet d'une démarche.
     *
     * Le détail contient :
     *
     * - les informations générales ;
     * - la catégorie ;
     * - les documents requis ;
     * - les étapes ;
     * - les liens officiels.
     *
     * La transaction reste ouverte pendant toute la conversion
     * de l'entité vers le DTO.
     *
     * Cette annotation est particulièrement importante si
     * les relations suivantes sont configurées en LAZY :
     *
     * - documentsRequis ;
     * - etapes ;
     * - liensOfficiels ;
     * - categorie.
     *
     * @param id identifiant de la démarche
     * @param inclureBrouillons vrai pour un administrateur
     * @return détail complet de la démarche
     */
    @Transactional(readOnly = true)
    public DemarcheDetailDTO getDemarcheDetailById(
            Long id,
            boolean inclureBrouillons
    ) {

        /*
         * Récupération de la démarche principale.
         */
        Demarche demarche = getDemarcheById(id);

        /*
         * Un brouillon n'existe pas pour un étudiant ou un visiteur.
         */
        if (!inclureBrouillons && !demarche.isPubliee()) {
            throw new EntityNotFoundException(
                    "Démarche introuvable avec l'id : " + id
            );
        }


        // --------------------------------------------------------
        // DOCUMENTS REQUIS
        // --------------------------------------------------------

        /*
         * Récupération des documents nécessaires
         * à la réalisation de la démarche.
         *
         * Chaque entité DocumentRequis est transformée
         * en DocumentRequisDTO.
         */
        List<DocumentRequisDTO> documentsRequis =
                demarche.getDocumentsRequis()
                        .stream()
                        .map(this::convertirDocumentRequisDTO)
                        .toList();


        // --------------------------------------------------------
        // ÉTAPES
        // --------------------------------------------------------

        /*
         * Récupération des différentes étapes de la démarche.
         *
         * Chaque étape est transformée en DTO afin de ne pas
         * exposer directement l'entité JPA au frontend.
         */
        List<EtapeDemarcheDTO> etapes =
                demarche.getEtapes()
                        .stream()
                        .map(this::convertirEtapeDemarcheDTO)
                        .toList();


        // --------------------------------------------------------
        // LIENS OFFICIELS
        // --------------------------------------------------------

        /*
         * Récupération des liens vers les sites officiels.
         */
        List<LienOfficielDTO> liensOfficiels =
                demarche.getLiensOfficiels()
                        .stream()
                        .map(this::convertirLienOfficielDTO)
                        .toList();


        // --------------------------------------------------------
        // CONSTRUCTION DU DTO FINAL
        // --------------------------------------------------------

        /*
         * Construction de l'objet envoyé au frontend.
         *
         * Le frontend recevra toutes les informations
         * nécessaires à la page :
         *
         * /demarches/{id}
         */
        return DemarcheDetailDTO.builder()
                .id(demarche.getId())
                .titre(demarche.getTitre())
                .description(demarche.getDescription())
                .dateCreation(demarche.getDateCreation())
                .dateLimite(demarche.getDateLimite())
                .categorie(
                        demarche.getCategorie() != null
                                ? demarche.getCategorie().getNom()
                                : null
                )
                .documentsRequis(documentsRequis)
                .etapes(etapes)
                .liensOfficiels(liensOfficiels)
                .sourceUrl(demarche.getSourceUrl())
                .sourceDateMaj(demarche.getSourceDateMaj())
                .echeanceReference(demarche.getEcheanceReference())
                .echeanceDecalageJours(demarche.getEcheanceDecalageJours())
                .build();
    }


    // ============================================================
    // CRÉER UNE DÉMARCHE
    // ============================================================

    /**
     * Crée une nouvelle démarche.
     *
     * Cette méthode est normalement appelée uniquement
     * par un administrateur via :
     *
     * POST /api/demarches
     *
     * @param request informations de la nouvelle démarche
     * @return démarche créée
     */
    @Transactional
    public Demarche createDemarche(
            DemarcheRequest request
    ) {

        /*
         * Recherche de la catégorie correspondant
         * à l'identifiant envoyé dans la requête.
         */
        Categorie categorie =
                categorieRepository.findById(
                        request.getCategorieId()
                ).orElseThrow(
                        () -> new RuntimeException(
                                "Catégorie introuvable avec l'id : "
                                        + request.getCategorieId()
                        )
                );


        /*
         * Création de la nouvelle entité Demarche.
         */
        Demarche demarche = new Demarche();

        /*
         * Copie des informations provenant du DTO.
         */
        demarche.setTitre(request.getTitre());
        demarche.setDescription(request.getDescription());

        /*
         * Association de la catégorie.
         */
        demarche.setCategorie(categorie);

        /*
         * Date limite éventuelle.
         */
        demarche.setDateLimite(
                request.getDateLimite()
        );


        /*
         * La date de création est générée automatiquement
         * par le service lors de la création.
         */
        demarche.setDateCreation(
                java.time.LocalDate.now()
        );


        /*
         * Sauvegarde de la démarche en base de données.
         */
        return demarcheRepository.save(demarche);
    }


    // ============================================================
    // MODIFIER UNE DÉMARCHE
    // ============================================================

    /**
     * Modifie une démarche existante.
     *
     * Seul un administrateur peut appeler cette méthode
     * via le controller.
     *
     * @param id identifiant de la démarche à modifier
     * @param request nouvelles informations
     * @return démarche modifiée
     */
    @Transactional
    public Demarche updateDemarche(
            Long id,
            DemarcheRequest request
    ) {

        /*
         * Recherche de la démarche existante.
         */
        Demarche demarche =
                getDemarcheById(id);


        /*
         * Recherche de la nouvelle catégorie.
         */
        Categorie categorie =
                categorieRepository.findById(
                        request.getCategorieId()
                ).orElseThrow(
                        () -> new RuntimeException(
                                "Catégorie introuvable avec l'id : "
                                        + request.getCategorieId()
                        )
                );


        /*
         * Mise à jour du titre.
         */
        demarche.setTitre(
                request.getTitre()
        );


        /*
         * Mise à jour de la description.
         */
        demarche.setDescription(
                request.getDescription()
        );


        /*
         * Mise à jour de la catégorie.
         */
        demarche.setCategorie(
                categorie
        );


        /*
         * Mise à jour de la date limite.
         */
        demarche.setDateLimite(
                request.getDateLimite()
        );


        /*
         * Sauvegarde des modifications.
         */
        return demarcheRepository.save(demarche);
    }


    // ============================================================
    // SUPPRIMER UNE DÉMARCHE
    // ============================================================

    /**
     * Supprime une démarche existante.
     *
     * Seul un administrateur peut appeler cette méthode
     * via le controller.
     *
     * @param id identifiant de la démarche
     */
    @Transactional
    public void deleteDemarche(Long id) {

        /*
         * Vérification de l'existence de la démarche
         * avant sa suppression.
         */
        Demarche demarche =
                getDemarcheById(id);


        /*
         * Suppression de la démarche.
         */
        // Historique des suggestions automatiques de cette démarche.
        suggestionEnvoyeeRepository.deleteByDemarcheId(id);

        demarcheRepository.delete(demarche);
    }


    // ============================================================
    // CONVERSION DOCUMENT REQUIS → DTO
    // ============================================================

    /**
     * Convertit une entité DocumentRequis en DTO.
     *
     * L'objectif est de ne transmettre au frontend
     * que les informations nécessaires.
     *
     * @param document document requis
     * @return DTO du document
     */
    private DocumentRequisDTO convertirDocumentRequisDTO(
            DocumentRequis document
    ) {

        return DocumentRequisDTO.builder()
                .id(document.getId())
                .nom(document.getNom())
                .description(document.getDescription())
                .type(document.getType())
                .build();
    }


    // ============================================================
    // CONVERSION ÉTAPE → DTO
    // ============================================================

    /**
     * Convertit une entité EtapeDemarche en DTO.
     *
     * @param etape étape de la démarche
     * @return DTO de l'étape
     */
    private EtapeDemarcheDTO convertirEtapeDemarcheDTO(
            EtapeDemarche etape
    ) {

        return EtapeDemarcheDTO.builder()
                .id(etape.getId())
                .numero(etape.getNumero())
                .titre(etape.getTitre())
                .description(etape.getDescription())
                .build();
    }


    // ============================================================
    // CONVERSION LIEN OFFICIEL → DTO
    // ============================================================

    /**
     * Convertit une entité LienOfficiel en DTO.
     *
     * @param lien lien officiel
     * @return DTO du lien officiel
     */
    private LienOfficielDTO convertirLienOfficielDTO(
            LienOfficiel lien
    ) {

        return LienOfficielDTO.builder()
                .id(lien.getId())
                .nom(lien.getNom())
                .url(lien.getUrl())
                .description(lien.getDescription())
                .build();
    }
}