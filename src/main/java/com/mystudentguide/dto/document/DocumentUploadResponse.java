package com.mystudentguide.dto.document;

/**
 * DTO utilisé comme réponse après l'upload
 * d'un document.
 *
 * Il permet de confirmer au frontend que le fichier
 * a bien été enregistré.
 */
public class DocumentUploadResponse {

    /**
     * Identifiant du document créé en base.
     */
    private Long documentId;

    /**
     * Nom original du fichier envoyé.
     */
    private String nomOriginal;

    /**
     * Message de confirmation.
     */
    private String message;

    /**
     * Constructeur vide.
     */
    public DocumentUploadResponse() {
    }

    /**
     * Constructeur complet.
     *
     * @param documentId identifiant du document
     * @param nomOriginal nom original du fichier
     * @param message message de confirmation
     */
    public DocumentUploadResponse(
            Long documentId,
            String nomOriginal,
            String message
    ) {
        this.documentId = documentId;
        this.nomOriginal = nomOriginal;
        this.message = message;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getNomOriginal() {
        return nomOriginal;
    }

    public void setNomOriginal(String nomOriginal) {
        this.nomOriginal = nomOriginal;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}