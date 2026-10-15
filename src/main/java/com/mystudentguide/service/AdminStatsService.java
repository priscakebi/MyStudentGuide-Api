package com.mystudentguide.service;

import com.mystudentguide.dto.admin.AdminStatsResponse;
import com.mystudentguide.dto.admin.AdminStatsResponse.Activite;
import com.mystudentguide.dto.admin.AdminStatsResponse.InscriptionsSemaine;
import com.mystudentguide.dto.admin.AdminStatsResponse.TopDemarche;
import com.mystudentguide.entity.Avis;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.StudentDemarche;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.DemarcheStatus;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.enums.StatutSignalement;
import com.mystudentguide.repository.AvisRepository;
import com.mystudentguide.repository.CategorieRepository;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.repository.NotificationRepository;
import com.mystudentguide.repository.SignalementDemarcheRepository;
import com.mystudentguide.repository.StudentDemarcheRepository;
import com.mystudentguide.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Calcule les statistiques du tableau de bord administrateur.
 *
 * Les volumes de MyStudentGuide restent modestes (quelques centaines
 * d'étudiants) : les calculs sont faits en mémoire, en une transaction.
 */
@Service
public class AdminStatsService {

    /** Nombre de semaines affichées dans le graphique des inscriptions. */
    static final int SEMAINES = 8;

    private final UserRepository userRepository;
    private final DemarcheRepository demarcheRepository;
    private final CategorieRepository categorieRepository;
    private final StudentDemarcheRepository studentDemarcheRepository;
    private final AvisRepository avisRepository;
    private final NotificationRepository notificationRepository;
    private final SignalementDemarcheRepository signalementRepository;

    public AdminStatsService(
            UserRepository userRepository,
            DemarcheRepository demarcheRepository,
            CategorieRepository categorieRepository,
            StudentDemarcheRepository studentDemarcheRepository,
            AvisRepository avisRepository,
            NotificationRepository notificationRepository,
            SignalementDemarcheRepository signalementRepository
    ) {
        this.userRepository = userRepository;
        this.demarcheRepository = demarcheRepository;
        this.categorieRepository = categorieRepository;
        this.studentDemarcheRepository = studentDemarcheRepository;
        this.avisRepository = avisRepository;
        this.notificationRepository = notificationRepository;
        this.signalementRepository = signalementRepository;
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats(String adminEmail) {

        LocalDate today = LocalDate.now();

        // --------------------------------------------------------
        // Étudiants
        // --------------------------------------------------------

        List<User> etudiants = userRepository.findByRole(RoleType.ETUDIANT);

        LocalDateTime ilYa7Jours = LocalDateTime.now().minusDays(7);
        long nouveaux = etudiants.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(ilYa7Jours))
                .count();

        // --------------------------------------------------------
        // Suivis commencés (EN_COURS + TERMINEE)
        // --------------------------------------------------------

        List<StudentDemarche> commences = new ArrayList<>(
                studentDemarcheRepository.findByStatut(DemarcheStatus.EN_COURS));
        long enCours = commences.size();

        List<StudentDemarche> termines =
                studentDemarcheRepository.findByStatut(DemarcheStatus.TERMINEE);
        commences.addAll(termines);

        int tauxCompletion = commences.isEmpty()
                ? 0
                : (int) Math.round(100.0 * termines.size() / commences.size());

        Set<Long> profilsActifs = commences.stream()
                .map(sd -> sd.getStudentProfile().getId())
                .collect(Collectors.toSet());

        long sansDemarche = etudiants.stream()
                .filter(u -> u.getStudentProfile() == null
                        || !profilsActifs.contains(u.getStudentProfile().getId()))
                .count();

        // --------------------------------------------------------
        // Top 5 des démarches commencées
        // --------------------------------------------------------

        Map<Long, List<StudentDemarche>> parDemarche = commences.stream()
                .collect(Collectors.groupingBy(sd -> sd.getDemarche().getId()));

        Map<Long, Demarche> demarches = commences.stream()
                .map(StudentDemarche::getDemarche)
                .collect(Collectors.toMap(Demarche::getId, Function.identity(), (a, b) -> a));

        List<TopDemarche> top = parDemarche.entrySet().stream()
                .map(e -> new TopDemarche(
                        e.getKey(),
                        demarches.get(e.getKey()).getTitre(),
                        e.getValue().size(),
                        e.getValue().stream()
                                .filter(sd -> sd.getStatut() == DemarcheStatus.TERMINEE)
                                .count()))
                .sorted(Comparator.comparingLong(TopDemarche::commencees).reversed()
                        .thenComparing(TopDemarche::titre))
                .limit(5)
                .toList();

        // --------------------------------------------------------
        // Inscriptions par semaine (8 dernières semaines)
        // --------------------------------------------------------

        LocalDate lundiCourant = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<InscriptionsSemaine> parSemaine = new ArrayList<>();

        for (int i = SEMAINES - 1; i >= 0; i--) {
            LocalDate debut = lundiCourant.minusWeeks(i);
            LocalDate fin = debut.plusWeeks(1);

            long count = etudiants.stream()
                    .map(User::getCreatedAt)
                    .filter(Objects::nonNull)
                    .map(LocalDateTime::toLocalDate)
                    .filter(d -> !d.isBefore(debut) && d.isBefore(fin))
                    .count();

            parSemaine.add(new InscriptionsSemaine(debut, count));
        }

        // --------------------------------------------------------
        // Avis
        // --------------------------------------------------------

        List<Avis> avis = avisRepository.findAll();
        Double noteMoyenne = avis.isEmpty()
                ? null
                : Math.round(avis.stream()
                        .filter(a -> a.getNote() != null)
                        .mapToInt(Avis::getNote)
                        .average()
                        .orElse(0) * 10) / 10.0;

        // --------------------------------------------------------
        // Activité récente (notifications de l'admin connecté)
        // --------------------------------------------------------

        List<Activite> activite = userRepository.findByEmail(adminEmail)
                .map(admin -> notificationRepository
                        .findByUserIdOrderByDateCreationDesc(admin.getId())
                        .stream()
                        .limit(6)
                        .map(n -> new Activite(
                                n.getTitre(),
                                n.getMessage(),
                                n.getDateCreation(),
                                Boolean.TRUE.equals(n.getLu())))
                        .toList())
                .orElse(List.of());

        return new AdminStatsResponse(
                etudiants.size(),
                nouveaux,
                sansDemarche,
                demarcheRepository.count(),
                categorieRepository.count(),
                enCours,
                termines.size(),
                tauxCompletion,
                avis.size(),
                noteMoyenne,
                top,
                parSemaine,
                activite,
                signalementRepository.countByStatut(StatutSignalement.OUVERT)
        );
    }
}
