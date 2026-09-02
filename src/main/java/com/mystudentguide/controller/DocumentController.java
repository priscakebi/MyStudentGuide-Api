package com.mystudentguide.controller;

import com.mystudentguide.entity.Document;
import com.mystudentguide.service.DocumentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller responsable de la gestion des documents
 * associés aux démarches administratives.
 *
 * Un document peut par exemple représenter :
 *
 * - un passeport ;
 * - une photo d'identité ;
 * - un justificatif de domicile ;
 * - une attestation de scolarité ;
 * - un titre de séjour ;
 * - un justificatif financier ;
 * - etc.
 *
 * Architecture :
 *
 * Angular
 *    ↓
 * DocumentController
 *    ↓
 * DocumentService
 *    ↓
 * DocumentRepository
 *    ↓
 * MySQL
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    /**
     * Service responsable de la gestion des documents.
     */
    private final DocumentService documentService;

    /**
     * Injection du service par constructeur.
     *
     * @param documentService service de gestion des documents
     */
    public DocumentController(
            DocumentService documentService
    ) {
        this.documentService = documentService;
    }

    /**
     * Récupère tous les documents.
     *
     * Endpoint :
     *
     * GET /api/documents
     *
     * @return liste de tous les documents
     */
    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {

        List<Document> documents =
                documentService.getAllDocuments();

        return ResponseEntity.ok(documents);
    }

    /**
     * Récupère un document à partir de son identifiant.
     *
     * Endpoint :
     *
     * GET /api/documents/{id}
     *
     * Exemple :
     *
     * GET /api/documents/1
     *
     * @param id identifiant du document
     * @return document trouvé ou 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(
            @PathVariable Long id
    ) {

        Optional<Document> document =
                documentService.getDocumentById(id);

        return document
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound().build()
                );
    }

    /**
     * Récupère les documents associés à une démarche.
     *
     * Endpoint :
     *
     * GET /api/documents/demarche/{demarcheId}
     *
     * Exemple :
     *
     * GET /api/documents/demarche/1
     *
     * @param demarcheId identifiant de la démarche
     * @return liste des documents associés
     */
    @GetMapping("/demarche/{demarcheId}")
    public ResponseEntity<List<Document>> getDocumentsByDemarche(
            @PathVariable Long demarcheId
    ) {

        List<Document> documents =
                documentService.getDocumentsByDemarche(demarcheId);

        return ResponseEntity.ok(documents);
    }

    /**
     * Crée un nouveau document.
     *
     * Endpoint :
     *
     * POST /api/documents
     *
     * @param document document à créer
     * @return document créé
     */
    @PostMapping
    public ResponseEntity<Document> createDocument(
            @RequestBody Document document
    ) {

        Document savedDocument =
                documentService.saveDocument(document);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedDocument);
    }

    /**
     * Modifie un document existant.
     *
     * Endpoint :
     *
     * PUT /api/documents/{id}
     *
     * @param id identifiant du document
     * @param document nouvelles informations du document
     * @return document mis à jour
     */
    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocument(
            @PathVariable Long id,
            @RequestBody Document document
    ) {

        Optional<Document> existingDocument =
                documentService.getDocumentById(id);

        /*
         * Vérifie que le document existe.
         */
        if (existingDocument.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        /*
         * On conserve l'identifiant du document
         * déjà présent en base.
         */
        document.setId(id);

        Document updatedDocument =
                documentService.saveDocument(document);

        return ResponseEntity.ok(updatedDocument);
    }

    /**
     * Supprime un document.
     *
     * Endpoint :
     *
     * DELETE /api/documents/{id}
     *
     * @param id identifiant du document
     * @return HTTP 204 si la suppression réussit
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id
    ) {

        Optional<Document> existingDocument =
                documentService.getDocumentById(id);

        /*
         * Si le document n'existe pas,
         * on retourne HTTP 404.
         */
        if (existingDocument.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        documentService.deleteDocument(id);

        return ResponseEntity.noContent().build();
    }
}