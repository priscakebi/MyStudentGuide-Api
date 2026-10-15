package com.mystudentguide.service;

import com.mystudentguide.dto.signalement.SignalementDtos.SignalementRequest;
import com.mystudentguide.dto.signalement.SignalementDtos.SignalementResponse;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.SignalementDemarche;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.NotificationType;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.enums.StatutSignalement;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.repository.SignalementDemarcheRepository;
import com.mystudentguide.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Signalements d'informations erronées ou obsolètes par les étudiants.
 *
 * - L'étudiant signale un problème sur une démarche publiée
 *   (un seul signalement ouvert par étudiant et par démarche).
 * - Les administrateurs sont prévenus par notification.
 * - Quand l'admin marque le signalement « traité », l'étudiant
 *   est remercié par notification.
 */
@Service
public class SignalementService {

    private final SignalementDemarcheRepository signalementRepository;
    private final DemarcheRepository demarcheRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public SignalementService(
            SignalementDemarcheRepository signalementRepository,
            DemarcheRepository demarcheRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.signalementRepository = signalementRepository;
        this.demarcheRepository = demarcheRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Enregistre le signalement d'un étudiant.
     *
     * @throws IllegalArgumentException démarche non publiée ou déjà signalée
     */
    @Transactional
    public SignalementResponse signaler(Long demarcheId, SignalementRequest request, String email) {

        User etudiant = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable."));

        Demarche demarche = demarcheRepository.findById(demarcheId)
                .filter(Demarche::isPubliee)
                .orElseThrow(() -> new EntityNotFoundException("Démarche introuvable avec l'id : " + demarcheId));

        if (signalementRepository.existsByDemarche_IdAndUserIdAndStatut(
                demarcheId, etudiant.getId(), StatutSignalement.OUVERT)) {
            throw new IllegalArgumentException(
                    "Vous avez déjà signalé cette démarche : l'équipe est en train de la vérifier.");
        }

        String commentaire = request.commentaire() == null ? null : request.commentaire().trim();

        SignalementDemarche signalement = signalementRepository.save(
                SignalementDemarche.builder()
                        .demarche(demarche)
                        .motif(request.motif())
                        .commentaire(commentaire == null || commentaire.isEmpty() ? null : commentaire)
                        .userId(etudiant.getId())
                        .auteur(etudiant.getPrenom())
                        .statut(StatutSignalement.OUVERT)
                        .build());

        for (User admin : userRepository.findByRole(RoleType.ADMIN)) {
            notificationService.saveNotification(
                    Notification.builder()
                            .titre("Signalement : " + request.motif().getLibelle())
                            .message(etudiant.getPrenom() + " signale un problème sur la démarche \""
                                    + demarche.getTitre() + "\""
                                    + (signalement.getCommentaire() == null ? "." : " : " + signalement.getCommentaire()))
                            .type(NotificationType.INFORMATION)
                            .lu(false)
                            .user(admin)
                            .demarcheId(demarcheId)
                            .build());
        }

        return SignalementResponse.of(signalement);
    }

    @Transactional(readOnly = true)
    public List<SignalementResponse> lister(StatutSignalement statut) {
        return signalementRepository.findAvecDemarche(statut).stream()
                .map(SignalementResponse::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public long compterOuverts() {
        return signalementRepository.countByStatut(StatutSignalement.OUVERT);
    }

    /**
     * Change le statut d'un signalement. TRAITE : l'étudiant est remercié.
     */
    @Transactional
    public SignalementResponse changerStatut(Long id, StatutSignalement statut) {

        SignalementDemarche signalement = signalementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Signalement introuvable."));

        signalement.setStatut(statut);
        signalement.setDateTraitement(statut == StatutSignalement.OUVERT ? null : LocalDateTime.now());

        if (statut == StatutSignalement.TRAITE) {
            userRepository.findById(signalement.getUserId()).ifPresent(etudiant ->
                    notificationService.saveNotification(
                            Notification.builder()
                                    .titre("Merci pour votre signalement")
                                    .message("La démarche \"" + signalement.getDemarche().getTitre()
                                            + "\" a été vérifiée et mise à jour grâce à vous.")
                                    .type(NotificationType.INFORMATION)
                                    .lu(false)
                                    .user(etudiant)
                                    .demarcheId(signalement.getDemarche().getId())
                                    .build()));
        }

        return SignalementResponse.of(signalementRepository.save(signalement));
    }
}
