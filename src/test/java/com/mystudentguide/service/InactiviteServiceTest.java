package com.mystudentguide.service;

import com.mystudentguide.entity.User;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Suppression des comptes inactifs : 36 mois sans connexion, préavis de 30 jours.
 */
class InactiviteServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AccountService accountService = mock(AccountService.class);
    private final EmailService emailService = mock(EmailService.class);

    private final InactiviteService service = new InactiviteService(
            userRepository, accountService, emailService, 36, 30, "https://mystudentguide.fr/");

    private final LocalDateTime maintenant = LocalDateTime.of(2029, 10, 16, 4, 15);

    private User etudiant(String email) {
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPrenom("Prisca");
        user.setRole(RoleType.ETUDIANT);
        return user;
    }

    @Test
    void previentLesEtudiantsInactifsAvecLaDateDeSuppression() {
        User inactif = etudiant("prisca@email.com");
        when(userRepository.findInactifsAAvertir(RoleType.ETUDIANT, maintenant.minusMonths(36).plusDays(30)))
                .thenReturn(List.of(inactif));
        when(userRepository.findInactifsASupprimer(any(), any(), any())).thenReturn(List.of());

        service.traiter(maintenant);

        assertThat(inactif.getAvertissementInactiviteLe()).isEqualTo(maintenant);
        verify(userRepository).save(inactif);
        verify(emailService).envoyerAvertissementInactivite(
                "prisca@email.com", "Prisca", "15/11/2029", "https://mystudentguide.fr/login");
        verify(accountService, never()).effacerCompte(any());
    }

    @Test
    void supprimeLesComptesDontLePreavisEstEcoule() {
        User inactif = etudiant("ancien@email.com");
        when(userRepository.findInactifsAAvertir(any(), any())).thenReturn(List.of());
        when(userRepository.findInactifsASupprimer(
                RoleType.ETUDIANT, maintenant.minusMonths(36), maintenant.minusDays(30)))
                .thenReturn(List.of(inactif));

        int supprimes = service.traiter(maintenant);

        assertThat(supprimes).isEqualTo(1);
        verify(accountService).effacerCompte(inactif);
        verify(emailService).envoyerSuppressionInactivite("ancien@email.com", "Prisca");
    }

    @Test
    void neTouchePasAuxAdministrateurs() {
        when(userRepository.findInactifsAAvertir(any(), any())).thenReturn(List.of());
        when(userRepository.findInactifsASupprimer(any(), any(), any())).thenReturn(List.of());

        service.traiter(maintenant);

        // Les deux recherches ne portent que sur les étudiants.
        verify(userRepository).findInactifsAAvertir(eq(RoleType.ETUDIANT), any());
        verify(userRepository).findInactifsASupprimer(eq(RoleType.ETUDIANT), any(), any());
        verify(emailService, never()).envoyerAvertissementInactivite(anyString(), anyString(), anyString(), anyString());
    }
}
