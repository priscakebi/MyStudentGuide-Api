package com.mystudentguide.service;

import com.mystudentguide.entity.Document;
import com.mystudentguide.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service responsable de la gestion des documents
 * associés aux démarches administratives.
 *
 * Un document peut par exemple représenter :
 *
 * - une pièce d'identité ;
 * - un justificatif de domicile ;
 * - un certificat de scolarité ;
 * - une attestation d'assurance ;
 * - un titre de séjour ;
 * - etc.
 *
 * Architecture :
 *
 * Controller
 *      ↓
 * DocumentService
 *      ↓
 * DocumentRepository
 *      ↓
 * MySQL
 */
@Service
public class DocumentService {

    /**
     * Repository permettant d'accéder aux documents
     * enregistrés dans la base de données.
     */
    private final DocumentRepository documentRepository;

    /**
     * Injection du repository par constructeur.
     *
     * @param documentRepository repository des documents
     */
    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /**
     * Récupère tous les documents enregistrés.
     *
     * Cette méthode pourra notamment être utilisée
     * par l'administration.
     *
     * @return liste de tous les documents
     */
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    /**
     * Recherche un document à partir de son identifiant.
     *
     * @param id identifiant du document
     * @return document trouvé ou Optional.empty()
     */
    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    /**
     * Récupère tous les documents associés à une démarche.
     *
     * Exemple :
     *
     * Démarche "Demande CAF"
     *       ↓
     *       ├── Justificatif de domicile
     *       ├── Certificat de scolarité
     *       └── Pièce d'identité
     *
     * @param demarcheId identifiant de la démarche
     * @return liste des documents de la démarche
     */
    public List<Document> getDocumentsByDemarche(Long demarcheId) {
        return documentRepository.findByDemarcheId(demarcheId);
    }

    /**
     * Recherche un document à partir de son nom de stockage.
     *
     * Le nom de stockage peut être un nom généré
     * par l'application afin d'éviter les conflits
     * entre fichiers.
     *
     * @param nomStockage nom utilisé pour stocker le fichier
     * @return document trouvé ou Optional.empty()
     */
    public Optional<Document> getDocumentByNomStockage(
            String nomStockage
    ) {
        return documentRepository.findByNomStockage(nomStockage);
    }

    /**
     * Vérifie si un document existe déjà avec
     * le nom de stockage fourni.
     *
     * @param nomStockage nom de stockage à vérifier
     * @return true si le document existe
     */
    public boolean documentExists(String nomStockage) {
        return documentRepository.existsByNomStockage(nomStockage);
    }

    /**
     * Compte le nombre de documents associés à une démarche.
     *
     * Cette méthode pourra être utilisée pour vérifier
     * si tous les documents nécessaires ont été fournis.
     *
     * @param demarcheId identifiant de la démarche
     * @return nombre de documents associés
     */
    public long countDocumentsByDemarche(Long demarcheId) {
        return documentRepository.countByDemarcheId(demarcheId);
    }

    /**
     * Enregistre un document en base de données.
     *
     * Attention :
     * cette méthode enregistre uniquement les informations
     * du document en base.
     *
     * La gestion physique du fichier
     * (upload, stockage, suppression du fichier)
     * sera traitée séparément.
     *
     * @param document document à enregistrer
     * @return document enregistré
     */
    public Document saveDocument(Document document) {
        return documentRepository.save(document);
    }

    /**
     * Supprime un document à partir de son identifiant.
     *
     * La gestion de la suppression physique du fichier
     * devra également être traitée lorsque nous mettrons
     * en place le système de stockage.
     *
     * @param id identifiant du document
     */
    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }
}