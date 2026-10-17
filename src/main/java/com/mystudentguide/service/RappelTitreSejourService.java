package com.mystudentguide.service;

import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.NotificationType;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.StudentProfileRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Rappels avant la fin du titre de séjour (date saisie dans le profil).
 *
 * Le renouvellement se demande en ligne (ANEF), en général entre 4 et 2 mois
 * avant l'expiration : l'étudiant est prévenu à 120, 60, 30 et 7 jours, puis
 * une dernière fois si la date est dépassée (jusqu'à 30 jours après).
 *
 * Seul le palier le plus proche est envoyé : un étudiant qui saisit sa date
 * à 20 jours de l'échéance reçoit un seul rappel (« moins de 30 jours »).
 * Pas de doublon : le message (palier + date) n'est envoyé qu'une fois, comme
 * pour les rappels de démarches (existsNotification). Si l'étudiant change la
 * date, les rappels repartent pour la nouvelle date.
 *
 * Notification dans l'application, doublée d'un e-mail si l'adresse est confirmée.
 */
@Service
public class RappelTitreSejourService {

    /** Paliers en jours avant l'expiration, du plus lointain au plus proche. */
    static final int[] PALIERS = {120, 60, 30, 7};

    /** Après l'expiration, rappel encore envoyé pendant ce nombre de jours. */
    static final int JOURS_APRES_EXPIRATION = 30;

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final StudentProfileRepository studentProfileRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final String frontendUrl;
    private final Clock clock;

    @Autowired
    public RappelTitreSejourService(
            StudentProfileRepository studentProfileRepository,
            NotificationService notificationService,
            EmailService emailService,
            @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl
    ) {
        this(studentProfileRepository, notificationService, emailService, frontendUrl, Clock.systemDefaultZone());
    }

    /** Constructeur des tests : horloge contrôlée. */
    RappelTitreSejourService(
            StudentProfileRepository studentProfileRepository,
            NotificationService notificationService,
            EmailService emailService,
            String frontendUrl,
            Clock clock
    ) {
        this.studentProfileRepository = studentProfileRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
        this.clock = clock;
    }

    /**
     * Toutes les heures et au démarrage (l'API gratuite de Render se met en veille :
     * une tâche à heure fixe serait souvent manquée).
     *
     * @return nombre de rappels envoyés (tests)
     */
    @Scheduled(cron = "0 15 * * * *")
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public int envoyerRappels() {
        LocalDate aujourdHui = LocalDate.now(clock);

        List<StudentProfile> profils = studentProfileRepository.findByDateFinTitreSejourBetween(
                aujourdHui.minusDays(JOURS_APRES_EXPIRATION),
                aujourdHui.plusDays(PALIERS[0]));

        int envoyes = 0;
        for (StudentProfile profil : profils) {
            if (rappeler(profil, aujourdHui)) {
                envoyes++;
            }
        }
        return envoyes;
    }

    private boolean rappeler(StudentProfile profil, LocalDate aujourdHui) {
        User user = profil.getUser();
        LocalDate fin = profil.getDateFinTitreSejour();
        if (user == null || fin == null || user.getRole() != RoleType.ETUDIANT) {
            return false;
        }

        long jours = ChronoUnit.DAYS.between(aujourdHui, fin);
        String date = fin.format(FORMAT);
        String titre;
        String message;

        if (jours < 0) {
            titre = "Titre de séjour expiré";
            message = "Votre titre de séjour a expiré le " + date + ". "
                    + "Si vous avez obtenu un nouveau titre, mettez à jour sa date dans votre profil ; "
                    + "sinon, contactez au plus vite votre préfecture.";
        } else {
            int palier = palier(jours);
            titre = "Titre de séjour : moins de " + palier + " jours";
            message = palier >= 60
                    ? "Votre titre de séjour expire le " + date + ", dans moins de " + palier + " jours. "
                        + "Pensez à demander son renouvellement en ligne (ANEF), "
                        + "idéalement entre 4 et 2 mois avant cette date."
                    : "Votre titre de séjour expire le " + date + ", dans moins de " + palier + " jours. "
                        + "Si ce n'est pas déjà fait, demandez son renouvellement sans attendre : "
                        + "sans titre valide, vous seriez en situation irrégulière.";
        }

        if (notificationService.existsNotification(user.getId(), NotificationType.ECHEANCE, message)) {
            return false;
        }

        notificationService.saveNotification(Notification.builder()
                .titre(titre)
                .message(message)
                .type(NotificationType.ECHEANCE)
                .lu(false)
                .user(user)
                .build());

        // Adresse non confirmée : la notification dans l'application suffit.
        if (!Boolean.FALSE.equals(user.getEmailVerified())) {
            emailService.envoyerRappelTitreSejour(
                    user.getEmail(), user.getPrenom(), titre, message, frontendUrl + "/dashboard/profile");
        }
        return true;
    }

    /** Plus petit palier contenant le nombre de jours restants. */
    static int palier(long jours) {
        int resultat = PALIERS[0];
        for (int palier : PALIERS) {
            if (jours <= palier) {
                resultat = palier;
            }
        }
        return resultat;
    }
}
