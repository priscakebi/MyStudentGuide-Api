package com.mystudentguide.service;

import com.mystudentguide.entity.User;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Suppression automatique des comptes étudiants inactifs (RGPD : durée de
 * conservation limitée, recommandation de la CNIL).
 *
 * Chaque nuit :
 * 1. un étudiant qui ne s'est pas connecté depuis {@code inactiviteMois} mois,
 *    moins le préavis, reçoit un e-mail : son compte sera supprimé dans
 *    {@code preavisJours} jours, sauf s'il se connecte d'ici là
 *    (la connexion remet le compteur à zéro, voir AuthService.login) ;
 * 2. les comptes toujours inactifs à l'issue du préavis sont supprimés avec
 *    toutes leurs données (AccountService.effacerCompte), et l'étudiant en est informé.
 *
 * Les comptes administrateurs ne sont jamais concernés. Pour les comptes créés
 * avant le suivi des connexions, la date de création sert de référence.
 */
@Service
public class InactiviteService {

    private static final Logger log = LoggerFactory.getLogger(InactiviteService.class);
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final EmailService emailService;
    private final int inactiviteMois;
    private final int preavisJours;
    private final String frontendUrl;

    public InactiviteService(
            UserRepository userRepository,
            AccountService accountService,
            EmailService emailService,
            @Value("${app.comptes.inactivite-mois:36}") int inactiviteMois,
            @Value("${app.comptes.preavis-jours:30}") int preavisJours,
            @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl
    ) {
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.emailService = emailService;
        this.inactiviteMois = inactiviteMois;
        this.preavisJours = preavisJours;
        this.frontendUrl = frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
    }

    /** Chaque nuit à 4 h 15. */
    @Scheduled(cron = "${app.comptes.inactivite-cron:0 15 4 * * *}")
    public void traiterComptesInactifs() {
        traiter(LocalDateTime.now());
    }

    /**
     * Prévient puis supprime les comptes inactifs, à la date {@code maintenant}.
     *
     * @return nombre de comptes supprimés
     */
    @Transactional
    public int traiter(LocalDateTime maintenant) {
        LocalDateTime limiteInactivite = maintenant.minusMonths(inactiviteMois);

        // 1. Avertissement : la suppression aura lieu au plus tôt dans preavisJours jours.
        List<User> aAvertir = userRepository.findInactifsAAvertir(
                RoleType.ETUDIANT, limiteInactivite.plusDays(preavisJours));

        String dateSuppression = maintenant.plusDays(preavisJours).format(FORMAT_DATE);
        for (User user : aAvertir) {
            user.setAvertissementInactiviteLe(maintenant);
            userRepository.save(user);
            emailService.envoyerAvertissementInactivite(
                    user.getEmail(), user.getPrenom(), dateSuppression, frontendUrl + "/login");
        }

        // 2. Suppression : inactif depuis la durée complète ET prévenu depuis le préavis.
        List<User> aSupprimer = userRepository.findInactifsASupprimer(
                RoleType.ETUDIANT, limiteInactivite, maintenant.minusDays(preavisJours));

        for (User user : aSupprimer) {
            String email = user.getEmail();
            String prenom = user.getPrenom();
            accountService.effacerCompte(user);
            emailService.envoyerSuppressionInactivite(email, prenom);
        }

        if (!aAvertir.isEmpty() || !aSupprimer.isEmpty()) {
            log.info("Comptes inactifs : {} prévenu(s), {} supprimé(s).", aAvertir.size(), aSupprimer.size());
        }
        return aSupprimer.size();
    }
}
