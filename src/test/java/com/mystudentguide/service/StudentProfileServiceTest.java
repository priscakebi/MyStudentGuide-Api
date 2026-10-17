package com.mystudentguide.service;

import com.mystudentguide.entity.StudentDemarche;
import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.StudentProfileRepository;
import com.mystudentguide.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Enregistrement du profil : les démarches déjà commencées ne doivent jamais disparaître. */
@ExtendWith(MockitoExtension.class)
class StudentProfileServiceTest {

    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private SuggestionAutoService suggestionAutoService;

    private StudentProfileService service;
    private User etudiant;

    @BeforeEach
    void setUp() {
        service = new StudentProfileService(
                studentProfileRepository, userRepository, notificationService, suggestionAutoService);

        etudiant = new User();
        etudiant.setEmail("etudiant@email.com");
        etudiant.setRole(RoleType.ETUDIANT);

        when(userRepository.findByEmail("etudiant@email.com")).thenReturn(Optional.of(etudiant));
        when(userRepository.findByRole(RoleType.ADMIN)).thenReturn(List.of());
        when(studentProfileRepository.save(any(StudentProfile.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void modifierLeProfil_conserveLesDemarchesCommencees() {
        // Profil en base avec deux démarches suivies.
        StudentProfile enBase = new StudentProfile();
        enBase.setId(5L);
        List<StudentDemarche> suivies = new ArrayList<>(List.of(new StudentDemarche(), new StudentDemarche()));
        enBase.setStudentDemarches(suivies);
        when(studentProfileRepository.findById(5L)).thenReturn(Optional.of(enBase));

        // Profil reçu du formulaire : la liste des démarches n'est jamais envoyée (vide).
        StudentProfile recu = new StudentProfile();
        recu.setId(5L);
        recu.setVilleEtudes("Lyon");

        service.saveProfile(recu, "etudiant@email.com");

        ArgumentCaptor<StudentProfile> enregistre = ArgumentCaptor.forClass(StudentProfile.class);
        verify(studentProfileRepository).save(enregistre.capture());
        assertThat(enregistre.getValue().getVilleEtudes()).isEqualTo("Lyon");
        assertThat(enregistre.getValue().getStudentDemarches()).isSameAs(suivies).hasSize(2);
    }

    @Test
    void creerLeProfil_neChercheRienEnBase() {
        StudentProfile recu = new StudentProfile();
        recu.setVilleEtudes("Lyon");

        service.saveProfile(recu, "etudiant@email.com");

        verify(studentProfileRepository, never()).findById(any());
        assertThat(recu.getUser()).isSameAs(etudiant);
    }
}
