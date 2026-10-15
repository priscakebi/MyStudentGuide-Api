package com.mystudentguide.service;

import com.mystudentguide.dto.aide.DemandeAideDtos.DemandeAideRequest;
import com.mystudentguide.dto.aide.DemandeAideDtos.DemandeAideResponse;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.DemandeAide;
import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.enums.StatutDemandeAide;
import com.mystudentguide.enums.StatutPublication;
import com.mystudentguide.enums.SujetAide;
import com.mystudentguide.repository.DemandeAideRepository;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemandeAideServiceTest {

    @Mock private DemandeAideRepository demandeAideRepository;
    @Mock private DemarcheRepository demarcheRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private DemandeAideService service;

    private static User etudiant() {
        User u = new User();
        u.setId(5L);
        u.setPrenom("Aïcha");
        u.setNom("Diallo");
        u.setEmail("aicha@mail.fr");
        u.setRole(RoleType.ETUDIANT);
        return u;
    }

    private static DemandeAide demande(Long userId) {
        return DemandeAide.builder()
                .id(1L)
                .sujet(SujetAide.DEMARCHE)
                .demarcheId(10L)
                .demarcheTitre("Validation du VLS-TS")
                .message("Je ne trouve pas où payer la taxe.")
                .userId(userId)
                .auteur("Aïcha Diallo")
                .statut(StatutDemandeAide.OUVERTE)
                .build();
    }

    @Test
    void enregistreLaDemandeEtPrevientLesAdmins() {
        User admin = new User();
        admin.setRole(RoleType.ADMIN);

        Demarche demarche = new Demarche();
        demarche.setId(10L);
        demarche.setTitre("Validation du VLS-TS");
        demarche.setStatutPublication(StatutPublication.PUBLIEE);

        when(userRepository.findByEmail("aicha@mail.fr")).thenReturn(Optional.of(etudiant()));
        when(demandeAideRepository.countByUserIdAndStatut(5L, StatutDemandeAide.OUVERTE)).thenReturn(0L);
        when(demarcheRepository.findById(10L)).thenReturn(Optional.of(demarche));
        when(demandeAideRepository.save(any(DemandeAide.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.findByRole(RoleType.ADMIN)).thenReturn(List.of(admin));

        DemandeAideResponse reponse = service.envoyer(
                new DemandeAideRequest(SujetAide.DEMARCHE, 10L, "  Je ne trouve pas où payer la taxe.  "),
                "aicha@mail.fr");

        assertThat(reponse.message()).isEqualTo("Je ne trouve pas où payer la taxe.");
        assertThat(reponse.demarcheTitre()).isEqualTo("Validation du VLS-TS");
        assertThat(reponse.auteur()).isEqualTo("Aïcha Diallo");
        assertThat(reponse.statut()).isEqualTo(StatutDemandeAide.OUVERTE);

        ArgumentCaptor<Notification> notif = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).saveNotification(notif.capture());
        assertThat(notif.getValue().getUser()).isSameAs(admin);
        assertThat(notif.getValue().getTitre()).isEqualTo("Demande d'aide : Une démarche");
        assertThat(notif.getValue().getMessage()).contains("Validation du VLS-TS", "payer la taxe");
    }

    @Test
    void refuseAuDelaDeTroisDemandesEnAttente() {
        when(userRepository.findByEmail("aicha@mail.fr")).thenReturn(Optional.of(etudiant()));
        when(demandeAideRepository.countByUserIdAndStatut(5L, StatutDemandeAide.OUVERTE)).thenReturn(3L);

        assertThatThrownBy(() -> service.envoyer(
                new DemandeAideRequest(SujetAide.AUTRE, null, "Encore une question pour vous."),
                "aicha@mail.fr"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3 demandes en attente");

        verify(demandeAideRepository, never()).save(any());
    }

    @Test
    void laReponseEstEnvoyeeALEtudiant() {
        User aicha = etudiant();
        when(demandeAideRepository.findById(1L)).thenReturn(Optional.of(demande(5L)));
        when(userRepository.findById(5L)).thenReturn(Optional.of(aicha));
        when(demandeAideRepository.save(any(DemandeAide.class))).thenAnswer(i -> i.getArgument(0));

        DemandeAideResponse reponse = service.repondre(1L, "  Le paiement se fait sur le site de l'ANEF.  ");

        assertThat(reponse.statut()).isEqualTo(StatutDemandeAide.REPONDUE);
        assertThat(reponse.reponse()).isEqualTo("Le paiement se fait sur le site de l'ANEF.");
        assertThat(reponse.dateReponse()).isNotNull();

        ArgumentCaptor<Notification> notif = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).saveNotification(notif.capture());
        assertThat(notif.getValue().getUser()).isSameAs(aicha);
        assertThat(notif.getValue().getMessage()).contains("site de l'ANEF");
    }

    @Test
    void unEtudiantNePeutPasSupprimerLaDemandeDUnAutre() {
        when(demandeAideRepository.findById(1L)).thenReturn(Optional.of(demande(99L)));
        when(userRepository.findByEmail("aicha@mail.fr")).thenReturn(Optional.of(etudiant()));

        assertThatThrownBy(() -> service.supprimer(1L, "aicha@mail.fr"))
                .isInstanceOf(AccessDeniedException.class);

        verify(demandeAideRepository, never()).delete(any());
    }

    @Test
    void leStatutRepondueNePassePasParChangerStatut() {
        assertThatThrownBy(() -> service.changerStatut(1L, StatutDemandeAide.REPONDUE))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
