package com.mystudentguide.service;

import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.DocumentRequis;
import com.mystudentguide.entity.StudentDemarche;
import com.mystudentguide.enums.DocumentType;
import com.mystudentguide.repository.DocumentRequisRepository;
import com.mystudentguide.repository.StudentDemarcheRepository;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChecklistDocumentsServiceTest {

    @Mock private StudentDemarcheRepository studentDemarcheRepository;
    @Mock private DocumentRequisRepository documentRequisRepository;

    @InjectMocks private ChecklistDocumentsService service;

    private StudentDemarche suivi;

    @BeforeEach
    void setUp() {
        Demarche demarche = new Demarche();
        demarche.setId(1L);

        Demarche autre = new Demarche();
        autre.setId(2L);

        suivi = new StudentDemarche();
        suivi.setDemarche(demarche);

        DocumentRequis passeport = new DocumentRequis("Passeport", null, DocumentType.IDENTITE, demarche);
        passeport.setId(10L);
        DocumentRequis horsDemarche = new DocumentRequis("Bail", null, DocumentType.DOMICILE, autre);
        horsDemarche.setId(20L);

        lenient().when(documentRequisRepository.findById(10L)).thenReturn(Optional.of(passeport));
        lenient().when(documentRequisRepository.findById(20L)).thenReturn(Optional.of(horsDemarche));
        lenient().when(studentDemarcheRepository.save(any(StudentDemarche.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void cocheEtDecocheUnDocument() {
        assertThat(service.marquer(suivi, 10L, true).getDocumentsPrets()).containsExactly(10L);

        // Cocher deux fois ne crée pas de doublon.
        assertThat(service.marquer(suivi, 10L, true).getDocumentsPrets()).containsExactly(10L);

        assertThat(service.marquer(suivi, 10L, false).getDocumentsPrets()).isEmpty();
    }

    @Test
    void refuseUnDocumentDUneAutreDemarche() {
        assertThatThrownBy(() -> service.marquer(suivi, 20L, true))
                .isInstanceOf(EntityNotFoundException.class);
        verify(studentDemarcheRepository, never()).save(any());
    }

    @Test
    void refuseUnDocumentInconnu() {
        when(documentRequisRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.marquer(suivi, 99L, true))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
