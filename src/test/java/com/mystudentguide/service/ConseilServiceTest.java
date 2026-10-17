package com.mystudentguide.service;

import com.mystudentguide.dto.demarche.ConseilDtos.AstuceDuJour;
import com.mystudentguide.dto.demarche.ConseilDtos.ConseilDTO;
import com.mystudentguide.dto.demarche.ConseilDtos.ConseilRequest;
import com.mystudentguide.entity.ConseilDemarche;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.StudentDemarche;
import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.enums.DemarcheStatus;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.repository.StudentDemarcheRepository;
import com.mystudentguide.repository.StudentProfileRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConseilServiceTest {

    @Mock private DemarcheRepository demarcheRepository;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private StudentDemarcheRepository studentDemarcheRepository;

    private ConseilService service;

    @BeforeEach
    void setUp() {
        service = avecDate(LocalDate.of(2026, 10, 17));
    }

    private ConseilService avecDate(LocalDate date) {
        Clock clock = Clock.fixed(date.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        return new ConseilService(demarcheRepository, studentProfileRepository, studentDemarcheRepository, clock);
    }

    private static Demarche demarche(long id, String titre, String... conseils) {
        Demarche demarche = new Demarche();
        demarche.setId(id);
        demarche.setTitre(titre);
        long conseilId = id * 10;
        for (String texte : conseils) {
            ConseilDemarche conseil = new ConseilDemarche(texte, null, demarche);
            conseil.setId(conseilId++);
            demarche.getConseils().add(conseil);
        }
        return demarche;
    }

    private static StudentDemarche suivi(Demarche demarche, DemarcheStatus statut) {
        StudentDemarche sd = new StudentDemarche();
        sd.setDemarche(demarche);
        sd.setStatut(statut);
        return sd;
    }

    private void etudiantQuiSuit(StudentDemarche... suivis) {
        StudentProfile profil = new StudentProfile();
        profil.setId(3L);
        when(studentProfileRepository.findByUserEmail("aicha@mail.com")).thenReturn(Optional.of(profil));
        when(studentDemarcheRepository.findByStudentProfileId(3L)).thenReturn(List.of(suivis));
    }

    @Test
    void ajouter_nettoieLesTextes() {
        Demarche demarche = demarche(1, "VLS-TS");
        when(demarcheRepository.findById(1L)).thenReturn(Optional.of(demarche));
        when(demarcheRepository.saveAndFlush(any(Demarche.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ConseilDTO> conseils = service.ajouter(1L, new ConseilRequest("  Gardez l'attestation.  ", "  "));

        assertThat(conseils).hasSize(1);
        assertThat(conseils.get(0).texte()).isEqualTo("Gardez l'attestation.");
        assertThat(conseils.get(0).texteEn()).isNull();
    }

    @Test
    void astuce_vientDUneDemarcheEnCours_etChangeChaqueJour() {
        Demarche enCours = demarche(1, "VLS-TS", "Conseil A", "Conseil B");
        Demarche terminee = demarche(2, "CAF", "Conseil CAF");
        etudiantQuiSuit(suivi(enCours, DemarcheStatus.EN_COURS), suivi(terminee, DemarcheStatus.TERMINEE));

        AstuceDuJour jour1 = service.astuceDuJour("aicha@mail.com").orElseThrow();
        AstuceDuJour jour1Bis = service.astuceDuJour("aicha@mail.com").orElseThrow();
        AstuceDuJour jour2 = avecDate(LocalDate.of(2026, 10, 18)).astuceDuJour("aicha@mail.com").orElseThrow();

        assertThat(jour1.demarcheId()).isEqualTo(1L);
        assertThat(jour1.demarcheTitre()).isEqualTo("VLS-TS");
        assertThat(jour1).isEqualTo(jour1Bis);
        assertThat(jour2.texte()).isNotEqualTo(jour1.texte());
    }

    @Test
    void astuce_sansDemarcheEnCours_prendLesTerminees() {
        etudiantQuiSuit(suivi(demarche(2, "CAF", "Conseil CAF"), DemarcheStatus.TERMINEE));

        assertThat(service.astuceDuJour("aicha@mail.com")).get()
                .extracting(AstuceDuJour::texte).isEqualTo("Conseil CAF");
    }

    @Test
    void astuce_aucunConseil_vide() {
        etudiantQuiSuit(suivi(demarche(1, "VLS-TS"), DemarcheStatus.EN_COURS));

        assertThat(service.astuceDuJour("aicha@mail.com")).isEmpty();
    }
}
