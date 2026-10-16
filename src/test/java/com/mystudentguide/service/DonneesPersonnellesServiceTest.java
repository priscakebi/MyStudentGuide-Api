package com.mystudentguide.service;

import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.AvisRepository;
import com.mystudentguide.repository.DemandeAideRepository;
import com.mystudentguide.repository.NotificationRepository;
import com.mystudentguide.repository.SignalementDemarcheRepository;
import com.mystudentguide.repository.StudentDemarcheRepository;
import com.mystudentguide.repository.StudentProfileRepository;
import com.mystudentguide.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Export des données personnelles (RGPD, droit d'accès et de portabilité).
 */
class DonneesPersonnellesServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final StudentProfileRepository profileRepository = mock(StudentProfileRepository.class);
    private final StudentDemarcheRepository studentDemarcheRepository = mock(StudentDemarcheRepository.class);
    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final AvisRepository avisRepository = mock(AvisRepository.class);
    private final SignalementDemarcheRepository signalementRepository = mock(SignalementDemarcheRepository.class);
    private final DemandeAideRepository demandeAideRepository = mock(DemandeAideRepository.class);

    private final DonneesPersonnellesService service = new DonneesPersonnellesService(
            userRepository, profileRepository, studentDemarcheRepository,
            notificationRepository, avisRepository, signalementRepository, demandeAideRepository);

    private User etudiant() {
        User user = new User();
        user.setId(7L);
        user.setPrenom("Prisca");
        user.setNom("Kouenin");
        user.setEmail("prisca@email.com");
        user.setPassword("$2a$10$HASH-SECRET");
        user.setRole(RoleType.ETUDIANT);
        user.setConditionsVersion("2026-10");
        return user;
    }

    @Test
    @SuppressWarnings("unchecked")
    void exporteLeCompteEtLeProfilSansJamaisLeMotDePasse() {
        when(userRepository.findByEmail("prisca@email.com")).thenReturn(Optional.of(etudiant()));
        StudentProfile profil = new StudentProfile();
        profil.setId(3L);
        profil.setNationalite("Ivoirienne");
        profil.setTelephone("0612345678");
        when(profileRepository.findByUserId(7L)).thenReturn(Optional.of(profil));
        when(studentDemarcheRepository.findByStudentProfileId(3L)).thenReturn(List.of());

        Map<String, Object> export = service.exporter("prisca@email.com");

        Map<String, Object> compte = (Map<String, Object>) export.get("compte");
        assertThat(compte).containsEntry("email", "prisca@email.com")
                .containsEntry("conditionsAccepteesVersion", "2026-10")
                .doesNotContainKey("password");
        assertThat(export.toString()).doesNotContain("HASH-SECRET");

        Map<String, Object> profilExporte = (Map<String, Object>) export.get("profil");
        assertThat(profilExporte).containsEntry("nationalite", "Ivoirienne")
                .containsEntry("telephone", "0612345678");

        assertThat(export).containsKeys("demarchesSuivies", "notifications", "avis", "signalements", "demandesAide");
    }

    @Test
    void exporteUnCompteSansProfil() {
        when(userRepository.findByEmail("prisca@email.com")).thenReturn(Optional.of(etudiant()));
        when(profileRepository.findByUserId(7L)).thenReturn(Optional.empty());

        Map<String, Object> export = service.exporter("prisca@email.com");

        assertThat(export.get("profil")).isNull();
        assertThat((List<?>) export.get("demarchesSuivies")).isEmpty();
    }

    @Test
    void refuseUnUtilisateurInconnu() {
        when(userRepository.findByEmail("inconnu@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.exporter("inconnu@email.com"))
                .isInstanceOf(IllegalStateException.class);
    }
}
