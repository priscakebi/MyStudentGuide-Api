package com.mystudentguide.service;

import com.mystudentguide.dto.demarche.CiblageDTO;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.entity.SuggestionEnvoyee;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.LogementType;
import com.mystudentguide.enums.StatutPublication;
import com.mystudentguide.enums.VisaType;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.repository.StudentDemarcheRepository;
import com.mystudentguide.repository.StudentProfileRepository;
import com.mystudentguide.repository.SuggestionEnvoyeeRepository;
import com.mystudentguide.servicepublic.Nationalites;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuggestionAutoServiceTest {

    @Mock private DemarcheRepository demarcheRepository;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private StudentDemarcheRepository studentDemarcheRepository;
    @Mock private SuggestionEnvoyeeRepository suggestionEnvoyeeRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private SuggestionAutoService service;

    // ------------------------------------------------------------

    private static StudentProfile profil(String nationalite, Boolean premiere, VisaType visa, LogementType logement) {
        User user = new User();
        user.setId(5L);

        StudentProfile p = new StudentProfile();
        p.setId(50L);
        p.setNationalite(nationalite);
        p.setPremiereArrivee(premiere);
        p.setTypeVisa(visa);
        p.setTypeLogement(logement);
        p.setUser(user);
        return p;
    }

    private static Demarche demarche(CiblageDTO ciblage) {
        Demarche d = new Demarche();
        d.setId(10L);
        d.setTitre("Validation du VLS-TS");
        d.setStatutPublication(StatutPublication.PUBLIEE);
        ciblage.appliquerA(d);
        return d;
    }

    // ------------------------------------------------------------
    // Règles
    // ------------------------------------------------------------

    @Test
    void nationalitesReconnues() {
        assertThat(Nationalites.estHorsUE("Ivoirienne")).isTrue();
        assertThat(Nationalites.estHorsUE("Camerounais")).isTrue();
        assertThat(Nationalites.estHorsUE("Allemande")).isFalse();
        assertThat(Nationalites.estHorsUE("française")).isFalse();
        assertThat(Nationalites.estHorsUE("Suisse")).isFalse();
        assertThat(Nationalites.estHorsUE("  ")).isNull();
    }

    @Test
    void horsUE_seulementLesEtudiantsHorsUE() {
        Demarche titreSejour = demarche(CiblageDTO.actif().pourHorsUE());

        assertThat(SuggestionAutoService.correspondance(
                profil("Ivoirienne", true, VisaType.VLS_TS_ETUDIANT, LogementType.CROUS), titreSejour))
                .get().asList().containsExactly("vous venez d'un pays hors Union européenne");

        assertThat(SuggestionAutoService.correspondance(
                profil("Italienne", true, VisaType.NON_RENSEIGNE, LogementType.CROUS), titreSejour))
                .isEmpty();

        // Nationalité inconnue : on ne suggère pas.
        assertThat(SuggestionAutoService.correspondance(
                profil(null, true, VisaType.NON_RENSEIGNE, LogementType.CROUS), titreSejour))
                .isEmpty();
    }

    @Test
    void tousLesCriteresDoiventEtreRemplis() {
        Demarche apl = demarche(CiblageDTO.actif().pourPremiereArrivee()
                .pourLogements(LogementType.LOCATION_PRIVEE, LogementType.RESIDENCE_ETUDIANTE));

        assertThat(SuggestionAutoService.correspondance(
                profil("Sénégalaise", true, VisaType.VLS_TS_ETUDIANT, LogementType.LOCATION_PRIVEE), apl))
                .get().asList().containsExactly(
                        "vous arrivez pour la première fois en France",
                        "votre logement : location privée");

        // Déjà installé : premier critère non rempli.
        assertThat(SuggestionAutoService.correspondance(
                profil("Sénégalaise", false, VisaType.VLS_TS_ETUDIANT, LogementType.LOCATION_PRIVEE), apl))
                .isEmpty();

        // Logement Crous : pas dans la liste.
        assertThat(SuggestionAutoService.correspondance(
                profil("Sénégalaise", true, VisaType.VLS_TS_ETUDIANT, LogementType.CROUS), apl))
                .isEmpty();

        // Première arrivée non renseignée : on ne suggère pas.
        assertThat(SuggestionAutoService.correspondance(
                profil("Sénégalaise", null, VisaType.VLS_TS_ETUDIANT, LogementType.LOCATION_PRIVEE), apl))
                .isEmpty();
    }

    @Test
    void ciblageParVisa() {
        Demarche renouvellement = demarche(new CiblageDTO(true, null, null,
                Set.of(VisaType.TITRE_SEJOUR_ETUDIANT), Set.of()));

        assertThat(SuggestionAutoService.correspondance(
                profil("Marocaine", false, VisaType.TITRE_SEJOUR_ETUDIANT, LogementType.CROUS), renouvellement))
                .get().asList().containsExactly("votre situation : titre de séjour étudiant");

        assertThat(SuggestionAutoService.correspondance(
                profil("Marocaine", false, VisaType.VLS_TS_ETUDIANT, LogementType.CROUS), renouvellement))
                .isEmpty();
    }

    @Test
    void brouillonOuCiblageInactifJamaisSuggere() {
        Demarche brouillon = demarche(CiblageDTO.actif());
        brouillon.setStatutPublication(StatutPublication.BROUILLON);

        Demarche inactive = demarche(CiblageDTO.AUCUN);

        StudentProfile p = profil("Ivoirienne", true, VisaType.VLS_TS_ETUDIANT, LogementType.CROUS);
        assertThat(SuggestionAutoService.correspondance(p, brouillon)).isEmpty();
        assertThat(SuggestionAutoService.correspondance(p, inactive)).isEmpty();
    }

    // ------------------------------------------------------------
    // Envoi
    // ------------------------------------------------------------

    @Test
    void envoieLaSuggestionAvecSaRaisonEtLaMemorise() {
        StudentProfile p = profil("Ivoirienne", true, VisaType.VLS_TS_ETUDIANT, LogementType.CROUS);
        Demarche d = demarche(CiblageDTO.actif().pourHorsUE());

        when(demarcheRepository.findByCibleActiveTrue()).thenReturn(List.of(d));

        assertThat(service.suggererPourProfil(p)).isEqualTo(1);

        ArgumentCaptor<Notification> notif = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).saveNotification(notif.capture());
        assertThat(notif.getValue().getMessage())
                .startsWith("D'après votre profil (vous venez d'un pays hors Union européenne)")
                .contains("Validation du VLS-TS");
        assertThat(notif.getValue().getAction()).isEqualTo(Notification.ACTION_SUGGESTION_DEMARCHE);
        assertThat(notif.getValue().getDemarcheId()).isEqualTo(10L);

        ArgumentCaptor<SuggestionEnvoyee> trace = ArgumentCaptor.forClass(SuggestionEnvoyee.class);
        verify(suggestionEnvoyeeRepository).save(trace.capture());
        assertThat(trace.getValue().getUserId()).isEqualTo(5L);
        assertThat(trace.getValue().getDemarcheId()).isEqualTo(10L);
    }

    @Test
    void jamaisDeuxFois() {
        StudentProfile p = profil("Ivoirienne", true, VisaType.VLS_TS_ETUDIANT, LogementType.CROUS);
        Demarche d = demarche(CiblageDTO.actif().pourHorsUE());

        when(demarcheRepository.findByCibleActiveTrue()).thenReturn(List.of(d));
        when(suggestionEnvoyeeRepository.existsByUserIdAndDemarcheId(5L, 10L)).thenReturn(true);

        assertThat(service.suggererPourProfil(p)).isZero();
        verify(notificationService, never()).saveNotification(any());
    }

    @Test
    void pasPourUneDemarcheDejaCommencee() {
        StudentProfile p = profil("Ivoirienne", true, VisaType.VLS_TS_ETUDIANT, LogementType.CROUS);
        Demarche d = demarche(CiblageDTO.actif().pourHorsUE());

        when(demarcheRepository.findByCibleActiveTrue()).thenReturn(List.of(d));
        when(studentDemarcheRepository.existsByStudentProfile_IdAndDemarche_Id(50L, 10L)).thenReturn(true);

        assertThat(service.suggererPourProfil(p)).isZero();
        verify(notificationService, never()).saveNotification(any());
    }

    @Test
    void publicationEvalueTousLesProfils() {
        Demarche d = demarche(CiblageDTO.actif().pourHorsUE());

        StudentProfile ivoirienne = profil("Ivoirienne", true, VisaType.VLS_TS_ETUDIANT, LogementType.CROUS);
        StudentProfile italienne = profil("Italienne", true, VisaType.NON_RENSEIGNE, LogementType.CROUS);
        italienne.getUser().setId(6L);
        italienne.setId(60L);

        when(studentProfileRepository.findAll()).thenReturn(List.of(ivoirienne, italienne));

        assertThat(service.suggererPourDemarche(d)).isEqualTo(1);
    }
}
