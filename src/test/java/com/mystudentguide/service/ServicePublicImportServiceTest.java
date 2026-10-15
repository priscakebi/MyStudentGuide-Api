package com.mystudentguide.service;

import com.mystudentguide.entity.Categorie;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.enums.StatutPublication;
import com.mystudentguide.repository.CategorieRepository;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.repository.UserRepository;
import com.mystudentguide.servicepublic.FicheServicePublic;
import com.mystudentguide.servicepublic.ServicePublicClient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicePublicImportServiceTest {

    @Mock private ServicePublicClient client;
    @Mock private DemarcheRepository demarcheRepository;
    @Mock private CategorieRepository categorieRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private CategorieDefautService categorieDefautService;

    @InjectMocks private ServicePublicImportService service;

    private static FicheServicePublic fiche(LocalDate dateMaj) {
        return new FicheServicePublic(
                "F2231",
                "Étudiant étranger en France : visa de long séjour ou titre de séjour",
                "Description officielle.",
                "https://www.service-public.gouv.fr/particuliers/vosdroits/F2231",
                dateMaj,
                "Étranger - Europe",
                List.of(
                        new FicheServicePublic.Etape("Comment valider le VLS-TS ?", "En ligne."),
                        new FicheServicePublic.Etape("Quelle démarche à la fin ?", "Carte de séjour.")),
                List.of(new FicheServicePublic.Lien("Valider un VLS-TS", "https://anef.example/", "Téléservice")));
    }

    @Test
    void importeUneFicheEnBrouillon() {
        Categorie categorie = new Categorie();
        when(client.getFiche("F2231")).thenReturn(fiche(LocalDate.of(2026, 8, 1)));
        when(demarcheRepository.existsBySourceId("F2231")).thenReturn(false);
        when(categorieRepository.findById(1L)).thenReturn(Optional.of(categorie));
        when(demarcheRepository.save(any(Demarche.class))).thenAnswer(i -> i.getArgument(0));

        Demarche demarche = service.importer("F2231", 1L);

        assertThat(demarche.getStatutPublication()).isEqualTo(StatutPublication.BROUILLON);
        assertThat(demarche.isPubliee()).isFalse();
        assertThat(demarche.getSourceId()).isEqualTo("F2231");
        assertThat(demarche.getSourceDateMaj()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(demarche.getDescription()).isEqualTo("Description officielle.");
        assertThat(demarche.getCategorie()).isSameAs(categorie);

        assertThat(demarche.getEtapes()).hasSize(2);
        assertThat(demarche.getEtapes().get(1).getNumero()).isEqualTo(2);

        // Lien vers la fiche source en premier, puis les liens de la fiche.
        assertThat(demarche.getLiensOfficiels()).hasSize(2);
        assertThat(demarche.getLiensOfficiels().get(0).getNom()).isEqualTo("Fiche officielle Service-Public");
    }

    @Test
    void refuseUneFicheDejaImportee() {
        when(client.getFiche("F2231")).thenReturn(fiche(LocalDate.of(2026, 8, 1)));
        when(demarcheRepository.existsBySourceId("F2231")).thenReturn(true);

        assertThatThrownBy(() -> service.importer("F2231", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("déjà été importée");
    }

    @Test
    void detecteUneMiseAJourEtPrevientLesAdminsUneSeuleFois() {
        Demarche demarche = new Demarche();
        demarche.setTitre("Titre de séjour");
        demarche.setSourceId("F2231");
        demarche.setSourceDateMaj(LocalDate.of(2026, 1, 1));

        User admin = new User();
        admin.setRole(RoleType.ADMIN);

        when(demarcheRepository.findBySourceIdIsNotNull()).thenReturn(List.of(demarche));
        when(client.getFiche("F2231")).thenReturn(fiche(LocalDate.of(2026, 8, 1)));
        when(userRepository.findByRole(RoleType.ADMIN)).thenReturn(List.of(admin));

        assertThat(service.verifierMisesAJour()).isEqualTo(1);
        assertThat(demarche.isMiseAJourDisponible()).isTrue();
        assertThat(demarche.getSourceDateDisponible()).isEqualTo(LocalDate.of(2026, 8, 1));

        // Deuxième passage : même version, pas de nouvelle notification.
        assertThat(service.verifierMisesAJour()).isZero();
        verify(notificationService, times(1)).saveNotification(any(Notification.class));
    }

    @Test
    void ficheAJourRienASignaler() {
        Demarche demarche = new Demarche();
        demarche.setSourceId("F2231");
        demarche.setSourceDateMaj(LocalDate.of(2026, 8, 1));

        when(demarcheRepository.findBySourceIdIsNotNull()).thenReturn(List.of(demarche));
        when(client.getFiche("F2231")).thenReturn(fiche(LocalDate.of(2026, 8, 1)));

        assertThat(service.verifierMisesAJour()).isZero();
        verify(notificationService, never()).saveNotification(any());
    }

    @Test
    void appliquerLaMiseAJourRemplaceLeContenuEtGardeLeTitre() {
        Demarche demarche = new Demarche();
        demarche.setTitre("Mon titre personnalisé");
        demarche.setSourceId("F2231");
        demarche.setSourceDateMaj(LocalDate.of(2026, 1, 1));
        demarche.setSourceDateDisponible(LocalDate.of(2026, 8, 1));

        when(demarcheRepository.findById(5L)).thenReturn(Optional.of(demarche));
        when(client.getFiche("F2231")).thenReturn(fiche(LocalDate.of(2026, 8, 1)));
        when(demarcheRepository.save(any(Demarche.class))).thenAnswer(i -> i.getArgument(0));

        Demarche resultat = service.appliquerMiseAJour(5L);

        assertThat(resultat.getTitre()).isEqualTo("Mon titre personnalisé");
        assertThat(resultat.getSourceDateMaj()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(resultat.isMiseAJourDisponible()).isFalse();
        assertThat(resultat.getEtapes()).hasSize(2);
    }
}
