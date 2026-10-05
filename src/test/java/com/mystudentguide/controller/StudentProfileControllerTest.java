package com.mystudentguide.controller;

import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.entity.User;
import com.mystudentguide.repository.UserRepository;
import com.mystudentguide.service.StudentProfileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

/** Vérifie les règles d'accès aux profils étudiants. */
@ExtendWith(MockitoExtension.class)
class StudentProfileControllerTest {

    @Mock private StudentProfileService profileService;
    @Mock private UserRepository userRepository;

    private StudentProfileController controller;
    private Authentication student;
    private Authentication admin;

    @BeforeEach
    void setUp() {
        controller = new StudentProfileController(profileService, userRepository);

        student = new UsernamePasswordAuthenticationToken(
                "etudiant@email.com", null, List.of(new SimpleGrantedAuthority("ROLE_ETUDIANT")));
        admin = new UsernamePasswordAuthenticationToken(
                "admin@email.com", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // L'étudiant connecté possède le profil n°10 et l'utilisateur n°1.
        StudentProfile monProfil = new StudentProfile();
        monProfil.setId(10L);
        lenient().when(profileService.getProfileByUserEmail("etudiant@email.com"))
                .thenReturn(Optional.of(monProfil));

        User user = new User();
        user.setId(1L);
        lenient().when(userRepository.findByEmail("etudiant@email.com"))
                .thenReturn(Optional.of(user));
    }

    @Test
    void unEtudiantNePeutPasLireLeProfilDUnAutre() {
        assertThat(controller.getProfileById(11L, student).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void unEtudiantPeutLireSonProfil() {
        when(profileService.getProfileById(10L)).thenReturn(Optional.of(new StudentProfile()));

        assertThat(controller.getProfileById(10L, student).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void unAdminPeutLireNImporteQuelProfil() {
        when(profileService.getProfileById(11L)).thenReturn(Optional.of(new StudentProfile()));

        assertThat(controller.getProfileById(11L, admin).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void unEtudiantNePeutPasModifierLeProfilDUnAutre() {
        assertThat(controller.updateProfile(11L, new StudentProfile(), student).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(profileService, never()).saveProfile(any(StudentProfile.class), anyString());
    }

    @Test
    void unEtudiantNePeutPasSupprimerLeProfilDUnAutre() {
        assertThat(controller.deleteProfile(11L, student).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(profileService, never()).deleteProfile(11L);
    }

    @Test
    void unEtudiantNePeutPasLireLeProfilViaUnAutreUserId() {
        assertThat(controller.getProfileByUserId(2L, student).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void creerUnSecondProfilRenvoie409() {
        when(profileService.profileExistsForUser(1L)).thenReturn(true);

        assertThat(controller.createProfile(new StudentProfile(), student).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }
}
