package com.mystudentguide.service;

import com.mystudentguide.dto.document.DocumentRequisRequest;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.DocumentRequis;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.repository.DocumentRequisRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service permettant de gérer les documents requis.
 *
 * Un DocumentRequis représente uniquement un document
 * nécessaire pour effectuer une démarche administrative.
 *
 * L'application ne stocke pas les fichiers personnels des étudiants.
 * Elle affiche simplement la liste des documents nécessaires
 * pour chaque démarche.
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
@Service
public class DocumentRequisService {

    /**
     * Repository permettant d'accéder aux documents requis.
     */
    private final DocumentRequisRepository documentRequisRepository;

    /**
     * Repository permettant de rechercher les démarches
     * auxquelles les documents sont associés.
     */
    private final DemarcheRepository demarcheRepository;

    /**
     * Constructeur du service.
     *
     * Spring injecte automatiquement les repositories nécessaires.
     *
     * @param documentRequisRepository repository des documents requis
     * @param demarcheRepository repository des démarches
     */
    public DocumentRequisService(
            DocumentRequisRepository documentRequisRepository,
            DemarcheRepository demarcheRepository
    ) {
        this.documentRequisRepository = documentRequisRepository;
        this.demarcheRepository = demarcheRepository;
    }

    /**
     * Récupère tous les documents requis.
     *
     * Cette méthode permet de consulter l'ensemble
     * des documents enregistrés dans l'application.
     *
     * @return liste de tous les documents requis
     */
    public List<DocumentRequis> getAllDocumentsRequis() {
        return documentRequisRepository.findAll();
    }

    /**
     * Recherche un document requis à partir de son identifiant.
     *
     * @param id identifiant du document requis
     * @return document requis trouvé
     * @throws RuntimeException si le document n'existe pas
     */
    public DocumentRequis getDocumentRequisById(Long id) {

        // Recherche le document dans la base de données.
        return documentRequisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Document requis introuvable avec l'id : " + id
                ));
    }

    /**
     * Récupère tous les documents requis d'une démarche.
     *
     * Exemple :
     *
     * Démarche : "Demande de titre de séjour étudiant"
     *
     * Résultat :
     * - Passeport
     * - Visa
     * - Certificat de scolarité
     *
     * @param demarcheId identifiant de la démarche
     * @return liste des documents requis de la démarche
     */
    public List<DocumentRequis> getDocumentsByDemarche(Long demarcheId) {

        // Vérifie d'abord que la démarche existe.
        if (!demarcheRepository.existsById(demarcheId)) {
            throw new RuntimeException(
                    "Démarche introuvable avec l'id : " + demarcheId
            );
        }

        // Récupère ensuite les documents associés à cette démarche.
        return documentRequisRepository.findByDemarche_Id(demarcheId);
    }

    /**
     * Crée un nouveau document requis.
     *
     * Le document est associé à une démarche existante
     * grâce à l'identifiant envoyé dans la requête.
     *
     * @param request données du document à créer
     * @return document requis créé
     */
    public DocumentRequis createDocumentRequis(
            DocumentRequisRequest request
    ) {

        // Recherche la démarche concernée.
        Demarche demarche = demarcheRepository.findById(
                request.getDemarcheId()
        ).orElseThrow(() -> new RuntimeException(
                "Démarche introuvable avec l'id : "
                        + request.getDemarcheId()
        ));

        // Création d'un nouveau document requis.
        DocumentRequis documentRequis = new DocumentRequis();

        // Définit le nom du document.
        documentRequis.setNom(request.getNom());

        // Définit la description du document.
        documentRequis.setDescription(request.getDescription());

        // Définit le type du document.
        documentRequis.setType(request.getType());

        // Associe le document à la démarche.
        documentRequis.setDemarche(demarche);

        // Enregistre le document en base de données.
        return documentRequisRepository.save(documentRequis);
    }

    /**
     * Modifie un document requis existant.
     *
     * La démarche associée peut également être modifiée.
     *
     * @param id identifiant du document à modifier
     * @param request nouvelles données du document
     * @return document requis modifié
     */
    public DocumentRequis updateDocumentRequis(
            Long id,
            DocumentRequisRequest request
    ) {

        // Recherche le document existant.
        DocumentRequis documentRequis =
                getDocumentRequisById(id);

        // Recherche la nouvelle démarche.
        Demarche demarche = demarcheRepository.findById(
                request.getDemarcheId()
        ).orElseThrow(() -> new RuntimeException(
                "Démarche introuvable avec l'id : "
                        + request.getDemarcheId()
        ));

        // Met à jour le nom.
        documentRequis.setNom(request.getNom());

        // Met à jour la description.
        documentRequis.setDescription(request.getDescription());

        // Met à jour le type.
        documentRequis.setType(request.getType());

        // Met à jour la démarche associée.
        documentRequis.setDemarche(demarche);

        // Enregistre les modifications en base de données.
        return documentRequisRepository.save(documentRequis);
    }

    /**
     * Supprime un document requis.
     *
     * Cette suppression concerne uniquement l'information
     * indiquant qu'un document est nécessaire pour une démarche.
     *
     * Aucun fichier personnel étudiant n'est supprimé,
     * car l'application n'en stocke pas.
     *
     * @param id identifiant du document à supprimer
     */
    public void deleteDocumentRequis(Long id) {

        // Vérifie que le document existe.
        DocumentRequis documentRequis =
                getDocumentRequisById(id);

        // Supprime le document requis de la base de données.
        documentRequisRepository.delete(documentRequis);
    }
}