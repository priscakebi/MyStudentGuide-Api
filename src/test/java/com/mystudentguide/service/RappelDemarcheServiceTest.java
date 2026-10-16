package com.mystudentguide.service;

import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.StudentDemarche;
import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.DemarcheStatus;
import com.mystudentguide.enums.NotificationType;
import com.mystudentguide.repository.StudentDemarcheRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Rappels d'échéance doublés par e-mail (7 et 3 jours avant la date limite).
 */
class RappelDemarcheServiceTest {

    private final StudentDemarcheRepository repository = mock(StudentDemarcheRepository.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final EmailService emailService = mock(EmailService.class);

    private final RappelDemarcheService service = new RappelDemarcheService(
            repository, notificationService, emailService, "https://mystudentguide.fr");

    /** Démarche en cours dont la date limite tombe dans {@code jours} jours. */
    private StudentDemarche suiviEcheanceDans(long jours) {
        User user = new User();
        user.setId(5L);
        user.setEmail("prisca@email.com");
        user.setPrenom("Prisca");

        StudentProfile profil = new StudentProfile();
        profil.setId(2L);
        profil.setUser(user);

        Demarche demarche = new Demarche();
        demarche.setId(30L);
        demarche.setTitre("Titre de séjour");
        demarche.setDateLimite(LocalDate.now().plusDays(jours));

        StudentDemarche suivi = new StudentDemarche();
        suivi.setId(9L);
        suivi.setStatut(DemarcheStatus.EN_COURS);
        suivi.setStudentProfile(profil);
        suivi.setDemarche(demarche);
        return suivi;
    }

    @Test
    void envoieUnEmailSeptJoursAvantLaDateLimite() {
        StudentDemarche suivi = suiviEcheanceDans(7);
        when(repository.findByStatut(DemarcheStatus.EN_COURS)).thenReturn(List.of(suivi));
        when(notificationService.existsNotification(anyLong(), eq(NotificationType.ECHEANCE), anyString()))
                .thenReturn(false);

        service.envoyerRappelsAutomatiques();

        verify(emailService).envoyerRappelEcheance(
                "prisca@email.com", "Prisca", "Titre de séjour", 7,
                suivi.getDemarche().getDateLimite(), "https://mystudentguide.fr/demarches/30");
    }

    @Test
    void envoieUnEmailTroisJoursAvantLaDateLimite() {
        StudentDemarche suivi = suiviEcheanceDans(3);
        when(repository.findByStatut(DemarcheStatus.EN_COURS)).thenReturn(List.of(suivi));
        when(notificationService.existsNotification(anyLong(), eq(NotificationType.ECHEANCE), anyString()))
                .thenReturn(false);

        service.envoyerRappelsAutomatiques();

        verify(emailService).envoyerRappelEcheance(
                eq("prisca@email.com"), eq("Prisca"), eq("Titre de séjour"), eq(3L), any(), anyString());
    }

    @Test
    void nEnvoiePasDeuxFoisLeMemeRappel() {
        StudentDemarche suivi = suiviEcheanceDans(7);
        when(repository.findByStatut(DemarcheStatus.EN_COURS)).thenReturn(List.of(suivi));
        // Le rappel (notification) existe déjà : la tâche tourne toutes les heures.
        when(notificationService.existsNotification(anyLong(), eq(NotificationType.ECHEANCE), anyString()))
                .thenReturn(true);

        service.envoyerRappelsAutomatiques();

        verify(emailService, never()).envoyerRappelEcheance(
                anyString(), anyString(), anyString(), anyLong(), any(), anyString());
    }

    @Test
    void nEnvoieRienEnDehorsDesJoursDeRappel() {
        when(repository.findByStatut(DemarcheStatus.EN_COURS)).thenReturn(List.of(suiviEcheanceDans(5)));

        service.envoyerRappelsAutomatiques();

        verify(emailService, never()).envoyerRappelEcheance(
                anyString(), anyString(), anyString(), anyLong(), any(), anyString());
    }
}
