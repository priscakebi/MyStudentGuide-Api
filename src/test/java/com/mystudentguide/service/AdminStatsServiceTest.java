package com.mystudentguide.service;

import com.mystudentguide.dto.admin.AdminStatsResponse;
import com.mystudentguide.entity.Avis;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.StudentDemarche;
import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.DemarcheStatus;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.AvisRepository;
import com.mystudentguide.repository.CategorieRepository;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.repository.NotificationRepository;
import com.mystudentguide.repository.DemandeAideRepository;
import com.mystudentguide.repository.SignalementDemarcheRepository;
import com.mystudentguide.repository.StudentDemarcheRepository;
import com.mystudentguide.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStatsServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private DemarcheRepository demarcheRepository;
    @Mock private CategorieRepository categorieRepository;
    @Mock private StudentDemarcheRepository studentDemarcheRepository;
    @Mock private AvisRepository avisRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private SignalementDemarcheRepository signalementRepository;
    @Mock private DemandeAideRepository demandeAideRepository;

    @InjectMocks private AdminStatsService service;

    @Test
    void calculeLesStatistiques() {

        StudentProfile profilA = profil(1L);
        StudentProfile profilB = profil(2L);

        User ancien = etudiant(profilA, LocalDateTime.now().minusDays(30));
        User nouveau = etudiant(profilB, LocalDateTime.now().minusDays(1));
        User sansProfil = etudiant(null, LocalDateTime.now().minusDays(2));

        Demarche titreSejour = demarche(10L, "Titre de séjour");
        Demarche ameli = demarche(20L, "Sécurité sociale");

        when(userRepository.findByRole(RoleType.ETUDIANT))
                .thenReturn(List.of(ancien, nouveau, sansProfil));
        when(studentDemarcheRepository.findByStatut(DemarcheStatus.EN_COURS))
                .thenReturn(new ArrayList<>(List.of(
                        suivi(profilA, titreSejour, DemarcheStatus.EN_COURS),
                        suivi(profilA, ameli, DemarcheStatus.EN_COURS),
                        suivi(profilA, ameli, DemarcheStatus.EN_COURS))));
        when(studentDemarcheRepository.findByStatut(DemarcheStatus.TERMINEE))
                .thenReturn(List.of(suivi(profilA, ameli, DemarcheStatus.TERMINEE)));
        when(demarcheRepository.count()).thenReturn(5L);
        when(categorieRepository.count()).thenReturn(3L);
        when(avisRepository.findAll()).thenReturn(List.of(avis(4), avis(5)));
        when(userRepository.findByEmail("admin@test.fr")).thenReturn(Optional.empty());

        AdminStatsResponse stats = service.getStats("admin@test.fr");

        assertThat(stats.etudiants()).isEqualTo(3);
        assertThat(stats.nouveauxEtudiants7j()).isEqualTo(2);
        // Seul le profil A a commencé une démarche.
        assertThat(stats.etudiantsSansDemarche()).isEqualTo(2);
        assertThat(stats.suivisEnCours()).isEqualTo(3);
        assertThat(stats.suivisTermines()).isEqualTo(1);
        assertThat(stats.tauxCompletion()).isEqualTo(25);
        assertThat(stats.noteMoyenne()).isEqualTo(4.5);

        // Sécurité sociale (3) avant Titre de séjour (1).
        assertThat(stats.topDemarches()).hasSize(2);
        assertThat(stats.topDemarches().get(0).titre()).isEqualTo("Sécurité sociale");
        assertThat(stats.topDemarches().get(0).commencees()).isEqualTo(3);
        assertThat(stats.topDemarches().get(0).terminees()).isEqualTo(1);

        // 8 semaines ; les 3 étudiants sont inscrits dans cette période.
        assertThat(stats.inscriptionsParSemaine()).hasSize(AdminStatsService.SEMAINES);
        assertThat(stats.inscriptionsParSemaine().stream()
                .mapToLong(AdminStatsResponse.InscriptionsSemaine::inscriptions)
                .sum()).isEqualTo(3);
    }

    @Test
    void sansDonneesRienNePlante() {

        when(userRepository.findByRole(RoleType.ETUDIANT)).thenReturn(List.of());
        when(studentDemarcheRepository.findByStatut(DemarcheStatus.EN_COURS))
                .thenReturn(new ArrayList<>());
        when(studentDemarcheRepository.findByStatut(DemarcheStatus.TERMINEE))
                .thenReturn(List.of());
        when(avisRepository.findAll()).thenReturn(List.of());
        when(userRepository.findByEmail("admin@test.fr")).thenReturn(Optional.empty());

        AdminStatsResponse stats = service.getStats("admin@test.fr");

        assertThat(stats.tauxCompletion()).isZero();
        assertThat(stats.noteMoyenne()).isNull();
        assertThat(stats.topDemarches()).isEmpty();
    }

    // ------------------------------------------------------------

    private static StudentProfile profil(Long id) {
        StudentProfile profil = new StudentProfile();
        profil.setId(id);
        return profil;
    }

    private static User etudiant(StudentProfile profil, LocalDateTime createdAt) {
        User user = new User();
        user.setRole(RoleType.ETUDIANT);
        user.setStudentProfile(profil);
        user.setCreatedAt(createdAt);
        return user;
    }

    private static Demarche demarche(Long id, String titre) {
        Demarche demarche = new Demarche();
        demarche.setId(id);
        demarche.setTitre(titre);
        return demarche;
    }

    private static StudentDemarche suivi(StudentProfile profil, Demarche demarche, DemarcheStatus statut) {
        StudentDemarche suivi = new StudentDemarche();
        suivi.setStudentProfile(profil);
        suivi.setDemarche(demarche);
        suivi.setStatut(statut);
        return suivi;
    }

    private static Avis avis(int note) {
        Avis avis = new Avis();
        avis.setNote(note);
        return avis;
    }
}
