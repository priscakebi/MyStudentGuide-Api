package com.mystudentguide.controller;

import com.mystudentguide.dto.document.DocumentRequisRequest;
import com.mystudentguide.entity.DocumentRequis;
import com.mystudentguide.service.DocumentRequisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller permettant de gérer les documents requis.
 *
 * Un DocumentRequis représente uniquement un document
 * nécessaire pour effectuer une démarche administrative.
 *
 * L'application ne permet pas d'importer, de stocker
 * ou d'exporter les fichiers personnels des étudiants.
 *
 * Exemple :
 *
 * Démarche :
 * "Demande de titre de séjour étudiant"
 *
 * Documents requis :
 * - Passeport
 * - Visa
 * - Certificat de scolarité
 * - Justificatif de domicile
 */
@RestController
@RequestMapping("/api/documents-requis")
public class DocumentRequisController {

    /**
     * Service utilisé pour gérer les documents requis.
     */
    private final DocumentRequisService documentRequisService;

    /**
     * Constructeur du controller.
     *
     * Spring injecte automatiquement le service.
     *
     * @param documentRequisService service des documents requis
     */
    public DocumentRequisController(
            DocumentRequisService documentRequisService
    ) {
        this.documentRequisService = documentRequisService;
    }

    /**
     * Récupère tous les documents requis.
     *
     * Endpoint :
     * GET /api/documents-requis
     *
     * Cette route est réservée à l'administrateur.
     *
     * @return liste de tous les documents requis
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocumentRequis>> getAllDocumentsRequis() {

        return ResponseEntity.ok(
                documentRequisService.getAllDocumentsRequis()
        );
    }

    /**
     * Récupère un document requis à partir de son identifiant.
     *
     * Endpoint :
     * GET /api/documents-requis/{id}
     *
     * Cette route est réservée à l'administrateur.
     *
     * @param id identifiant du document
     * @return document requis
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentRequis> getDocumentRequisById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                documentRequisService.getDocumentRequisById(id)
        );
    }

    /**
     * Récupère tous les documents requis d'une démarche.
     *
     * Endpoint :
     * GET /api/documents-requis/demarche/{demarcheId}
     *
     * Cette route est publique (voir SecurityConfig) :
     * les visiteurs non inscrits consultent aussi les démarches.
     *
     * C'est cette route qui permet notamment à l'étudiant
     * de consulter les documents nécessaires à une démarche.
     *
     * Exemple :
     *
     * GET /api/documents-requis/demarche/1
     *
     * peut retourner :
     *
     * - Passeport
     * - Visa
     * - Certificat de scolarité
     *
     * @param demarcheId identifiant de la démarche
     * @return liste des documents requis de la démarche
     */
    @GetMapping("/demarche/{demarcheId}")
    public ResponseEntity<List<DocumentRequis>> getDocumentsByDemarche(
            @PathVariable Long demarcheId
    ) {

        return ResponseEntity.ok(
                documentRequisService.getDocumentsByDemarche(demarcheId)
        );
    }

    /**
     * Crée un nouveau document requis.
     *
     * Endpoint :
     * POST /api/documents-requis
     *
     * Seul un administrateur peut créer un document requis.
     *
     * Exemple de requête :
     *
     * {
     *     "nom": "Passeport",
     *     "description": "Passeport en cours de validité",
     *     "type": "PASSEPORT",
     *     "demarcheId": 1
     * }
     *
     * @param request données du document à créer
     * @return document créé
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentRequis> createDocumentRequis(
            @Valid @RequestBody DocumentRequisRequest request
    ) {

        // Création du document requis via le service.
        DocumentRequis documentRequis =
                documentRequisService.createDocumentRequis(request);

        // Retourne le document créé avec le statut HTTP 201.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(documentRequis);
    }

    /**
     * Modifie un document requis existant.
     *
     * Endpoint :
     * PUT /api/documents-requis/{id}
     *
     * Seul un administrateur peut modifier un document requis.
     *
     * @param id identifiant du document
     * @param request nouvelles informations
     * @return document modifié
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentRequis> updateDocumentRequis(
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequisRequest request
    ) {

        // Mise à jour du document requis.
        DocumentRequis documentRequis =
                documentRequisService.updateDocumentRequis(
                        id,
                        request
                );

        // Retourne le document modifié.
        return ResponseEntity.ok(documentRequis);
    }

    /**
     * Supprime un document requis.
     *
     * Endpoint :
     * DELETE /api/documents-requis/{id}
     *
     * Seul un administrateur peut supprimer un document requis.
     *
     * @param id identifiant du document à supprimer
     * @return réponse HTTP 204 si la suppression est réussie
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDocumentRequis(
            @PathVariable Long id
    ) {

        // Suppression du document requis.
        documentRequisService.deleteDocumentRequis(id);

        // Retourne HTTP 204 : suppression effectuée sans contenu.
        return ResponseEntity.noContent().build();
    }
}