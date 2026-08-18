package com.mystudentguide.repository;

import com.mystudentguide.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository permettant de gérer les documents
 * associés aux démarches administratives.
 *
 * Spring Data JPA fournit automatiquement les opérations CRUD
 * grâce à JpaRepository.
 */
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Récupère tous les documents associés à une démarche.
     *
     * Exemple :
     *
     * Une démarche "Demande CAF" peut posséder :
     *
     * - justificatif de domicile
     * - certificat de scolarité
     * - pièce d'identité
     *
     * @param demarcheId identifiant de la démarche
     * @return liste des documents associés à la démarche
     */
    List<Document> findByDemarcheId(Long demarcheId);

    /**
     * Recherche un document à partir de son nom de stockage.
     *
     * Le nom de stockage est unique dans la base de données.
     *
     * Exemple :
     *
     * "8f52a7c9-2d4a-4f8c.pdf"
     *
     * Cette méthode pourra être utilisée lorsque le backend
     * doit retrouver un fichier précis.
     *
     * @param nomStockage nom utilisé pour stocker le fichier
     * @return le document s'il existe
     */
    Optional<Document> findByNomStockage(String nomStockage);

    /**
     * Vérifie si un document possède déjà le nom de stockage
     * fourni.
     *
     * Cette vérification permet d'éviter les doublons
     * lors de l'enregistrement d'un fichier.
     *
     * @param nomStockage nom de stockage à vérifier
     * @return true si le nom existe déjà, false sinon
     */
    boolean existsByNomStockage(String nomStockage);

    /**
     * Compte le nombre de documents associés à une démarche.
     *
     * Cette méthode pourra notamment être utilisée pour
     * vérifier si une démarche possède déjà des documents.
     *
     * @param demarcheId identifiant de la démarche
     * @return nombre de documents associés
     */
    long countByDemarcheId(Long demarcheId);
}