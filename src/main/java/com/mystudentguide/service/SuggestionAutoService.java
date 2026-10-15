package com.mystudentguide.service;

import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.entity.SuggestionEnvoyee;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.LogementType;
import com.mystudentguide.enums.NotificationType;
import com.mystudentguide.enums.VisaType;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.repository.StudentDemarcheRepository;
import com.mystudentguide.repository.StudentProfileRepository;
import com.mystudentguide.repository.SuggestionEnvoyeeRepository;
import com.mystudentguide.servicepublic.Nationalites;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Suggestions automatiques de démarches d'après le profil de l'étudiant.
 *
 * Chaque démarche peut porter un ciblage (Demarche.cible*) défini par
 * l'admin : hors UE, première arrivée, types de visa, types de logement.
 * Un étudiant dont le profil remplit tous les critères renseignés reçoit
 * une notification de suggestion (boutons « Voir plus » / « Commencer »),
 * avec la raison (« parce que vous arrivez pour la première fois »).
 *
 * Déclenché :
 * - quand l'étudiant enregistre son profil ;
 * - quand l'admin publie une démarche ou modifie son ciblage.
 *
 * Une suggestion n'est jamais envoyée deux fois (SuggestionEnvoyee), ni pour
 * une démarche déjà commencée, ni si une suggestion de l'admin est en attente.
 * Un critère dont l'information manque dans le profil n'est pas rempli :
 * mieux vaut ne rien suggérer que suggérer à tort.
 */
@Service
public class SuggestionAutoService {

    private final DemarcheRepository demarcheRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StudentDemarcheRepository studentDemarcheRepository;
    private final SuggestionEnvoyeeRepository suggestionEnvoyeeRepository;
    private final NotificationService notificationService;

    public SuggestionAutoService(
            DemarcheRepository demarcheRepository,
            StudentProfileRepository studentProfileRepository,
            StudentDemarcheRepository studentDemarcheRepository,
            SuggestionEnvoyeeRepository suggestionEnvoyeeRepository,
            NotificationService notificationService
    ) {
        this.demarcheRepository = demarcheRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.studentDemarcheRepository = studentDemarcheRepository;
        this.suggestionEnvoyeeRepository = suggestionEnvoyeeRepository;
        this.notificationService = notificationService;
    }


    // ============================================================
    // DÉCLENCHEURS
    // ============================================================

    /** Profil enregistré : toutes les démarches ciblées sont évaluées. */
    @Transactional
    public int suggererPourProfil(StudentProfile profil) {
        int envoyees = 0;
        for (Demarche demarche : demarcheRepository.findByCibleActiveTrue()) {
            if (suggererSiPertinent(profil, demarche)) {
                envoyees++;
            }
        }
        return envoyees;
    }

    /** Démarche publiée ou ciblage modifié : tous les profils sont évalués. */
    @Transactional
    public int suggererPourDemarche(Demarche demarche) {
        if (!demarche.isPubliee() || !demarche.isCiblageActif()) {
            return 0;
        }
        int envoyees = 0;
        for (StudentProfile profil : studentProfileRepository.findAll()) {
            if (suggererSiPertinent(profil, demarche)) {
                envoyees++;
            }
        }
        return envoyees;
    }


    // ============================================================
    // RÈGLES
    // ============================================================

    /**
     * Raisons pour lesquelles la démarche concerne ce profil,
     * ou vide si un critère n'est pas rempli.
     */
    static Optional<List<String>> correspondance(StudentProfile profil, Demarche demarche) {

        if (!demarche.isPubliee() || !demarche.isCiblageActif()) {
            return Optional.empty();
        }

        List<String> raisons = new ArrayList<>();

        // Nationalité (hors UE / UE)
        if (demarche.getCibleHorsUE() != null) {
            Boolean horsUE = Nationalites.estHorsUE(profil.getNationalite());
            if (horsUE == null || !horsUE.equals(demarche.getCibleHorsUE())) {
                return Optional.empty();
            }
            raisons.add(horsUE
                    ? "vous venez d'un pays hors Union européenne"
                    : "vous êtes ressortissant européen");
        }

        // Première arrivée
        if (demarche.getCiblePremiereArrivee() != null) {
            if (profil.getPremiereArrivee() == null
                    || !profil.getPremiereArrivee().equals(demarche.getCiblePremiereArrivee())) {
                return Optional.empty();
            }
            raisons.add(profil.getPremiereArrivee()
                    ? "vous arrivez pour la première fois en France"
                    : "vous êtes déjà installé en France");
        }

        // Visa
        if (!demarche.getCibleVisas().isEmpty()) {
            if (!demarche.getCibleVisas().contains(profil.getTypeVisa())) {
                return Optional.empty();
            }
            raisons.add("votre situation : " + libelle(profil.getTypeVisa()));
        }

        // Logement
        if (!demarche.getCibleLogements().isEmpty()) {
            if (!demarche.getCibleLogements().contains(profil.getTypeLogement())) {
                return Optional.empty();
            }
            raisons.add("votre logement : " + libelle(profil.getTypeLogement()));
        }

        return Optional.of(raisons);
    }

    static String libelle(VisaType visa) {
        return switch (visa) {
            case VLS_TS_ETUDIANT -> "visa long séjour étudiant (VLS-TS)";
            case COURT_SEJOUR -> "visa court séjour";
            case TITRE_SEJOUR_ETUDIANT -> "titre de séjour étudiant";
            case AUTRE -> "autre titre";
            case NON_RENSEIGNE -> "non renseigné";
        };
    }

    static String libelle(LogementType logement) {
        return switch (logement) {
            case CROUS -> "logement Crous";
            case LOCATION_PRIVEE -> "location privée";
            case HEBERGEMENT_CHEZ_UN_PROCHE -> "hébergé chez un proche";
            case HEBERGEMENT_FAMILLE -> "hébergé en famille";
            case RESIDENCE_ETUDIANTE -> "résidence étudiante";
            case SANS_LOGEMENT -> "pas encore de logement";
            case AUTRE -> "autre";
            case NON_RENSEIGNE -> "non renseigné";
        };
    }


    // ============================================================
    // ENVOI
    // ============================================================

    private boolean suggererSiPertinent(StudentProfile profil, Demarche demarche) {

        User etudiant = profil.getUser();
        if (etudiant == null) {
            return false;
        }

        Optional<List<String>> raisons = correspondance(profil, demarche);
        if (raisons.isEmpty()) {
            return false;
        }

        // Jamais deux fois, ni pour une démarche déjà suivie,
        // ni si l'admin a déjà suggéré cette démarche.
        if (suggestionEnvoyeeRepository.existsByUserIdAndDemarcheId(etudiant.getId(), demarche.getId())
                || studentDemarcheRepository.existsByStudentProfile_IdAndDemarche_Id(profil.getId(), demarche.getId())
                || notificationService.hasPendingSuggestion(etudiant.getId(), demarche.getId())) {
            return false;
        }

        String pourquoi = raisons.get().isEmpty()
                ? "Cette démarche concerne la plupart des étudiants internationaux"
                : "D'après votre profil (" + String.join(", ", raisons.get()) + ")";

        notificationService.saveNotification(
                Notification.builder()
                        .titre("Démarche suggérée pour vous")
                        .message(pourquoi + ", la démarche \"" + demarche.getTitre()
                                + "\" pourrait vous concerner. Consultez-la ou commencez-la dès maintenant.")
                        .type(NotificationType.DEMARCHE)
                        .lu(false)
                        .user(etudiant)
                        .demarcheId(demarche.getId())
                        .action(Notification.ACTION_SUGGESTION_DEMARCHE)
                        .actionTraitee(false)
                        .build());

        suggestionEnvoyeeRepository.save(
                SuggestionEnvoyee.builder()
                        .userId(etudiant.getId())
                        .demarcheId(demarche.getId())
                        .build());

        return true;
    }
}
