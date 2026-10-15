package com.mystudentguide.service;

import com.mystudentguide.entity.DocumentRequis;
import com.mystudentguide.entity.StudentDemarche;
import com.mystudentguide.repository.DocumentRequisRepository;
import com.mystudentguide.repository.StudentDemarcheRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Checklist des documents d'un étudiant pour une démarche qu'il a commencée.
 *
 * L'étudiant coche les documents requis qu'il a déjà rassemblés ;
 * les identifiants cochés sont gardés dans StudentDemarche.documentsPrets.
 * Aucun fichier n'est envoyé ni stocké : c'est un simple aide-mémoire.
 */
@Service
public class ChecklistDocumentsService {

    private final StudentDemarcheRepository studentDemarcheRepository;
    private final DocumentRequisRepository documentRequisRepository;

    public ChecklistDocumentsService(StudentDemarcheRepository studentDemarcheRepository,
                                     DocumentRequisRepository documentRequisRepository) {
        this.studentDemarcheRepository = studentDemarcheRepository;
        this.documentRequisRepository = documentRequisRepository;
    }

    /**
     * Coche (pret = true) ou décoche un document de la checklist.
     *
     * @throws EntityNotFoundException si le document n'appartient pas à la démarche suivie
     */
    @Transactional
    public StudentDemarche marquer(StudentDemarche suivi, Long documentId, boolean pret) {
        DocumentRequis document = documentRequisRepository.findById(documentId)
                .filter(d -> Objects.equals(d.getDemarcheId(), suivi.getDemarche().getId()))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ce document ne fait pas partie de la démarche."));

        if (pret) {
            suivi.getDocumentsPrets().add(document.getId());
        } else {
            suivi.getDocumentsPrets().remove(document.getId());
        }

        return studentDemarcheRepository.save(suivi);
    }
}
