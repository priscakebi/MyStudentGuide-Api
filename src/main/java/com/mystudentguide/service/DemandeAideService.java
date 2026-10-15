package com.mystudentguide.service;

import com.mystudentguide.dto.aide.DemandeAideDtos.DemandeAideRequest;
import com.mystudentguide.dto.aide.DemandeAideDtos.DemandeAideResponse;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.DemandeAide;
import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.NotificationType;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.enums.StatutDemandeAide;
import com.mystudentguide.repository.DemandeAideRepository;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Demandes d'aide des étudiants à l'administration.
 *
 * - L'étudiant écrit depuis la page « Besoin d'aide ? » ; au plus
 *   MAX_DEMANDES_OUVERTES demandes en attente à la fois (anti-abus).
 * - Les administrateurs sont prévenus par notification.
 * - La réponse de l'administrateur est envoyée à l'étudiant par
 *   notification et reste visible sur sa page.
 * - Les demandes traitées sont effacées après CONSERVATION_MOIS mois.
 */
@Service
public class DemandeAideService {

    private static final Logger log = LoggerFactory.getLogger(DemandeAideService.class);

    static final int MAX_DEMANDES_OUVERTES = 3;

    static final int CONSERVATION_MOIS = 12;

    /** Longueur de l'extrait du message repris dans la notification des admins. */
    private static final int EXTRAIT = 200;

    private final DemandeAideRepository demandeAideRepository;
    private final DemarcheRepository demarcheRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public DemandeAideService(
            DemandeAideRepository demandeAideRepository,
            DemarcheRepository demarcheRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.demandeAideRepository = demandeAideRepository;
        this.demarcheRepository = demarcheRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Enregistre la demande d'un étudiant et prévient les administrateurs.
     *
     * @throws IllegalArgumentException trop de demandes en attente
     */
    @Transactional
    public DemandeAideResponse envoyer(DemandeAideRequest request, String email) {

        User etudiant = etudiant(email);

        if (demandeAideRepository.countByUserIdAndStatut(etudiant.getId(), StatutDemandeAide.OUVERTE)
                >= MAX_DEMANDES_OUVERTES) {
            throw new IllegalArgumentException(
                    "Vous avez déjà " + MAX_DEMANDES_OUVERTES + " demandes en attente. "
                            + "Attendez une réponse avant d'en envoyer une nouvelle.");
        }

        Demarche demarche = null;
        if (request.demarcheId() != null) {
            demarche = demarcheRepository.findById(request.demarcheId())
                    .filter(Demarche::isPubliee)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Démarche introuvable avec l'id : " + request.demarcheId()));
        }

        DemandeAide demande = demandeAideRepository.save(
                DemandeAide.builder()
                        .sujet(request.sujet())
                        .demarcheId(demarche == null ? null : demarche.getId())
                        .demarcheTitre(demarche == null ? null : demarche.getTitre())
                        .message(request.message().trim())
                        .userId(etudiant.getId())
                        .auteur(nomComplet(etudiant))
                        .statut(StatutDemandeAide.OUVERTE)
                        .build());

        String extrait = demande.getMessage().length() > EXTRAIT
                ? demande.getMessage().substring(0, EXTRAIT) + "…"
                : demande.getMessage();

        for (User admin : userRepository.findByRole(RoleType.ADMIN)) {
            notificationService.saveNotification(
                    Notification.builder()
                            .titre("Demande d'aide : " + request.sujet().getLibelle())
                            .message(demande.getAuteur() + " a besoin d'aide"
                                    + (demarche == null ? "" : " sur la démarche \"" + demarche.getTitre() + "\"")
                                    + " : " + extrait)
                            .type(NotificationType.INFORMATION)
                            .lu(false)
                            .user(admin)
                            .demarcheId(demande.getDemarcheId())
                            .build());
        }

        return DemandeAideResponse.of(demande);
    }

    /** Demandes de l'étudiant connecté, les plus récentes d'abord. */
    @Transactional(readOnly = true)
    public List<DemandeAideResponse> mesDemandes(String email) {
        return demandeAideRepository.findByUserIdOrderByDateCreationDesc(etudiant(email).getId()).stream()
                .map(DemandeAideResponse::of)
                .toList();
    }

    /**
     * L'étudiant supprime l'une de ses demandes.
     *
     * @throws AccessDeniedException la demande appartient à un autre étudiant
     */
    @Transactional
    public void supprimer(Long id, String email) {
        DemandeAide demande = demande(id);
        if (!demande.getUserId().equals(etudiant(email).getId())) {
            throw new AccessDeniedException("Cette demande ne vous appartient pas.");
        }
        demandeAideRepository.delete(demande);
    }

    @Transactional(readOnly = true)
    public List<DemandeAideResponse> lister(StatutDemandeAide statut) {
        return demandeAideRepository.findParStatut(statut).stream()
                .map(DemandeAideResponse::of)
                .toList();
    }

    /**
     * L'administrateur répond : la demande passe à REPONDUE
     * et l'étudiant reçoit la réponse en notification.
     */
    @Transactional
    public DemandeAideResponse repondre(Long id, String reponse) {

        DemandeAide demande = demande(id);

        demande.setReponse(reponse.trim());
        demande.setStatut(StatutDemandeAide.REPONDUE);
        demande.setDateReponse(LocalDateTime.now());

        userRepository.findById(demande.getUserId()).ifPresent(etudiant ->
                notificationService.saveNotification(
                        Notification.builder()
                                .titre("Réponse à votre demande d'aide")
                                .message("Sujet : " + demande.getSujet().getLibelle()
                                        + ". Réponse de l'équipe : " + demande.getReponse())
                                .type(NotificationType.INFORMATION)
                                .lu(false)
                                .user(etudiant)
                                .demarcheId(demande.getDemarcheId())
                                .build()));

        return DemandeAideResponse.of(demandeAideRepository.save(demande));
    }

    /** Ferme une demande sans suite, ou la rouvre. */
    @Transactional
    public DemandeAideResponse changerStatut(Long id, StatutDemandeAide statut) {
        if (statut == StatutDemandeAide.REPONDUE) {
            throw new IllegalArgumentException("Utilisez « Répondre » pour envoyer une réponse.");
        }
        DemandeAide demande = demande(id);
        demande.setStatut(statut);
        return DemandeAideResponse.of(demandeAideRepository.save(demande));
    }

    @Transactional(readOnly = true)
    public long compterOuvertes() {
        return demandeAideRepository.countByStatut(StatutDemandeAide.OUVERTE);
    }

    /** Chaque nuit : efface les demandes traitées de plus de CONSERVATION_MOIS mois. */
    @Scheduled(cron = "0 45 3 * * *")
    @Transactional
    public void purgerAnciennes() {
        int supprimees = demandeAideRepository.purgerAnciennes(
                StatutDemandeAide.OUVERTE, LocalDateTime.now().minusMonths(CONSERVATION_MOIS));
        if (supprimees > 0) {
            log.info("{} demande(s) d'aide de plus de {} mois supprimée(s).", supprimees, CONSERVATION_MOIS);
        }
    }

    private User etudiant(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable."));
    }

    private DemandeAide demande(Long id) {
        return demandeAideRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Demande d'aide introuvable."));
    }

    private static String nomComplet(User user) {
        String nom = ((user.getPrenom() == null ? "" : user.getPrenom()) + " "
                + (user.getNom() == null ? "" : user.getNom())).trim();
        return nom.isEmpty() ? "Un étudiant" : nom;
    }
}
