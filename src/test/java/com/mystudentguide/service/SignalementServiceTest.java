package com.mystudentguide.service;

import com.mystudentguide.dto.signalement.SignalementDtos.SignalementRequest;
import com.mystudentguide.dto.signalement.SignalementDtos.SignalementResponse;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.SignalementDemarche;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.MotifSignalement;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.enums.StatutPublication;
import com.mystudentguide.enums.StatutSignalement;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.repository.SignalementDemarcheRepository;
import com.mystudentguide.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignalementServiceTest {

    @Mock private SignalementDemarcheRepository signalementRepository;
    @Mock private DemarcheRepository demarcheRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private SignalementService service;

    private static User etudiant() {
        User u = new User();
        u.setId(5L);
        u.setPrenom("Aïcha");
        u.setRole(RoleType.ETUDIANT);
        return u;
    }

    private static Demarche demarche(StatutPublication statut) {
        Demarche d = new Demarche();
        d.setId(10L);
        d.setTitre("Validation du VLS-TS");
        d.setStatutPublication(statut);
        return d;
    }

    @Test
    void enregistreLeSignalementEtPrevientLesAdmins() {
        User admin = new User();
        admin.setRole(RoleType.ADMIN);

        when(userRepository.findByEmail("aicha@mail.fr")).thenReturn(Optional.of(etudiant()));
        when(demarcheRepository.findById(10L)).thenReturn(Optional.of(demarche(StatutPublication.PUBLIEE)));
        when(signalementRepository.existsByDemarche_IdAndUserIdAndStatut(10L, 5L, StatutSignalement.OUVERT))
                .thenReturn(false);
        when(signalementRepository.save(any(SignalementDemarche.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.findByRole(RoleType.ADMIN)).thenReturn(List.of(admin));

        SignalementResponse reponse = service.signaler(
                10L, new SignalementRequest(MotifSignalement.LIEN_CASSE, "  Le lien ANEF ne marche plus  "), "aicha@mail.fr");

        assertThat(reponse.motif()).isEqualTo(MotifSignalement.LIEN_CASSE);
        assertThat(reponse.commentaire()).isEqualTo("Le lien ANEF ne marche plus");
        assertThat(reponse.auteur()).isEqualTo("Aïcha");
        assertThat(reponse.statut()).isEqualTo(StatutSignalement.OUVERT);

        ArgumentCaptor<Notification> notif = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).saveNotification(notif.capture());
        assertThat(notif.getValue().getTitre()).isEqualTo("Signalement : Lien cassé");
        assertThat(notif.getValue().getMessage()).contains("Validation du VLS-TS", "Le lien ANEF ne marche plus");
    }

    @Test
    void refuseUnDeuxiemeSignalementOuvert() {
        when(userRepository.findByEmail("aicha@mail.fr")).thenReturn(Optional.of(etudiant()));
        when(demarcheRepository.findById(10L)).thenReturn(Optional.of(demarche(StatutPublication.PUBLIEE)));
        when(signalementRepository.existsByDemarche_IdAndUserIdAndStatut(10L, 5L, StatutSignalement.OUVERT))
                .thenReturn(true);

        assertThatThrownBy(() -> service.signaler(
                10L, new SignalementRequest(MotifSignalement.AUTRE, null), "aicha@mail.fr"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("déjà signalé");

        verify(signalementRepository, never()).save(any());
    }

    @Test
    void unBrouillonNePeutPasEtreSignale() {
        when(userRepository.findByEmail("aicha@mail.fr")).thenReturn(Optional.of(etudiant()));
        when(demarcheRepository.findById(10L)).thenReturn(Optional.of(demarche(StatutPublication.BROUILLON)));

        assertThatThrownBy(() -> service.signaler(
                10L, new SignalementRequest(MotifSignalement.AUTRE, null), "aicha@mail.fr"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void traiterRemercieLEtudiant() {
        SignalementDemarche signalement = SignalementDemarche.builder()
                .id(1L)
                .demarche(demarche(StatutPublication.PUBLIEE))
                .motif(MotifSignalement.INFO_OBSOLETE)
                .userId(5L)
                .auteur("Aïcha")
                .build();

        when(signalementRepository.findById(1L)).thenReturn(Optional.of(signalement));
        when(userRepository.findById(5L)).thenReturn(Optional.of(etudiant()));
        when(signalementRepository.save(any(SignalementDemarche.class))).thenAnswer(i -> i.getArgument(0));

        SignalementResponse reponse = service.changerStatut(1L, StatutSignalement.TRAITE);

        assertThat(reponse.statut()).isEqualTo(StatutSignalement.TRAITE);
        assertThat(reponse.dateTraitement()).isNotNull();

        ArgumentCaptor<Notification> notif = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).saveNotification(notif.capture());
        assertThat(notif.getValue().getTitre()).isEqualTo("Merci pour votre signalement");
    }

    @Test
    void rejeterNePrevientPasLEtudiant() {
        SignalementDemarche signalement = SignalementDemarche.builder()
                .id(1L)
                .demarche(demarche(StatutPublication.PUBLIEE))
                .motif(MotifSignalement.AUTRE)
                .userId(5L)
                .build();

        when(signalementRepository.findById(1L)).thenReturn(Optional.of(signalement));
        when(signalementRepository.save(any(SignalementDemarche.class))).thenAnswer(i -> i.getArgument(0));

        service.changerStatut(1L, StatutSignalement.REJETE);

        verify(notificationService, never()).saveNotification(any());
    }
}
