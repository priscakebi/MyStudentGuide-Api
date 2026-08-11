package com.mystudentguide.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Représente un document associé à une démarche administrative.
 *
 * Exemple :
 *
 * Une démarche "Demande de CAF" peut avoir plusieurs documents :
 * - justificatif de domicile
 * - certificat de scolarité
 * - pièce d'identité
 *
 * Le fichier physique ne sera pas directement stocké dans cette
 * entité. Nous enregistrerons plutôt les informations permettant
 * de retrouver le fichier.
 */
@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    /**
     * Identifiant unique du document.
     *
     * Il est généré automatiquement par la base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom original du fichier envoyé par l'étudiant.
     *
     * Exemple :
     * "certificat_scolarite.pdf"
     */
    @Column(nullable = false, length = 255)
    private String nomFichier;

    /**
     * Nom ou identifiant utilisé pour stocker le fichier.
     *
     * Il peut être différent du nom original.
     *
     * Exemple :
     * "8f52a7c9-2d4a-4f8c.pdf"
     *
     * Cela permet notamment d'éviter les conflits de noms.
     */
    @Column(nullable = false, unique = true, length = 255)
    private String nomStockage;

    /**
     * Type MIME du fichier.
     *
     * Exemple :
     * application/pdf
     * image/jpeg
     * image/png
     *
     * Cette information permettra également de contrôler
     * les types de fichiers autorisés lors de l'upload.
     */
    @Column(nullable = false, length = 100)
    private String typeFichier;

    /**
     * Taille du fichier en octets.
     *
     * Exemple :
     * 524288 = 512 Ko
     */
    @Column(nullable = false)
    private Long taille;

    /**
     * Emplacement du fichier.
     *
     * Selon l'architecture choisie, cela pourra être :
     *
     * - un chemin local ;
     * - une URL ;
     * - une référence vers un stockage cloud.
     *
     * Exemple :
     * "/uploads/documents/8f52a7c9-2d4a-4f8c.pdf"
     */
    @Column(nullable = false, length = 500)
    private String cheminFichier;

    /**
     * Date et heure auxquelles le document
     * a été envoyé.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateUpload;

    /**
     * Démarche à laquelle le document est associé.
     *
     * Relation :
     *
     * Demarche 1 -------- * Document
     *
     * Une démarche peut donc avoir plusieurs documents.
     *
     * Exemple :
     *
     * Demarche :
     * "Demande de CAF"
     *
     *       |
     *       ├── Document : justificatif de domicile
     *       ├── Document : certificat de scolarité
     *       └── Document : pièce d'identité
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demarche_id", nullable = false)
    private Demarche demarche;

    /**
     * Définit automatiquement la date d'upload
     * avant l'enregistrement du document en base.
     */
    @PrePersist
    protected void onCreate() {
        dateUpload = LocalDateTime.now();
    }
}