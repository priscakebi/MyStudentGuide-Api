package com.mystudentguide.service;

import com.mystudentguide.dto.avis.AvisRequest;
import com.mystudentguide.dto.avis.AvisResponse;
import com.mystudentguide.entity.Avis;
import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.StudentDemarche;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.DemarcheStatus;
import com.mystudentguide.enums.NotificationType;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.AvisRepository;
import com.mystudentguide.repository.StudentDemarcheRepository;
import com.mystudentguide.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Avis des étudiants sur le suivi de leurs démarches terminées.
 */
@Service
public class AvisService {

    /** Nombre maximal d'avis renvoyés à la page d'accueil. */
    private static final int MAX_AVIS_PUBLICS = 30;

    private final AvisRepository avisRepository;
    private final StudentDemarcheRepository studentDemarcheRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public AvisService(
            AvisRepository avisRepository,
            StudentDemarcheRepository studentDemarcheRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.avisRepository = avisRepository;
        this.studentDemarcheRepository = studentDemarcheRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Avis visibles, pour la page d'accueil (accessible à tous).
     */
    @Transactional(readOnly = true)
    public List<AvisResponse> getAvisPublics(int limit) {

        int taille = Math.max(1, Math.min(limit, MAX_AVIS_PUBLICS));

        return avisRepository
                .findByVisibleTrueOrderByDateCreationDesc(PageRequest.of(0, taille))
                .stream()
                .map(AvisResponse::from)
                .toList();
    }

    /**
     * Tous les avis (administration).
     */
    @Transactional(readOnly = true)
    public List<AvisResponse> getTousLesAvis() {
        return avisRepository.findAllByOrderByDateCreationDesc()
                .stream()
                .map(AvisResponse::from)
                .toList();
    }

    /**
     * Enregistre l'avis d'un étudiant.
     *
     * Conditions :
     * - le suivi appartient à l'étudiant connecté ;
     * - la démarche est TERMINEE ;
     * - aucun avis n'a encore été donné pour ce suivi.
     */
    @Transactional
    public AvisResponse creerAvis(String emailEtudiant, AvisRequest request) {

        User user = userRepository.findByEmail(emailEtudiant)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Utilisateur connecté introuvable."));

        StudentDemarche studentDemarche = studentDemarcheRepository
                .findByIdWithDetails(request.getStudentDemarcheId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Démarche introuvable."));

        if (user.getStudentProfile() == null
                || !user.getStudentProfile().getId()
                        .equals(studentDemarche.getStudentProfile().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Cette démarche ne vous appartient pas.");
        }

        if (studentDemarche.getStatut() != DemarcheStatus.TERMINEE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vous pourrez donner votre avis une fois la démarche terminée.");
        }

        if (studentDemarche.isAvisDonne()
                || avisRepository.existsByStudentDemarcheId(studentDemarche.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Vous avez déjà donné votre avis sur cette démarche.");
        }

        Avis avis = new Avis();
        avis.setNote(request.getNote());
        avis.setCommentaire(request.getCommentaire().trim());
        avis.setStudentDemarcheId(studentDemarche.getId());
        avis.setUserId(user.getId());
        avis.setAuteur(nomPublic(user));
        avis.setDemarcheTitre(studentDemarche.getDemarche().getTitre());
        avis.setVisible(true);

        Avis saved = avisRepository.save(avis);

        studentDemarche.setAvisDonne(true);
        studentDemarcheRepository.save(studentDemarche);

        notifierAdministrateurs(saved);

        return AvisResponse.from(saved);
    }

    /** Masque / ré-affiche un avis (administration). */
    @Transactional
    public AvisResponse changerVisibilite(Long id, boolean visible) {
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Avis introuvable."));
        avis.setVisible(visible);
        return AvisResponse.from(avisRepository.save(avis));
    }

    /** Supprime un avis (administration). */
    @Transactional
    public void supprimerAvis(Long id) {
        if (!avisRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis introuvable.");
        }
        avisRepository.deleteById(id);
    }

    /** « Awa Koné » devient « Awa K. ». */
    private static String nomPublic(User user) {
        String prenom = user.getPrenom() != null ? user.getPrenom().trim() : "";
        String nom = user.getNom() != null ? user.getNom().trim() : "";
        String initiale = nom.isEmpty() ? "" : " " + Character.toUpperCase(nom.charAt(0)) + ".";
        String resultat = (prenom + initiale).trim();
        return resultat.isEmpty() ? "Étudiant" : resultat;
    }

    private void notifierAdministrateurs(Avis avis) {
        for (User admin : userRepository.findByRole(RoleType.ADMIN)) {
            notificationService.saveNotification(
                    Notification.builder()
                            .titre("Nouvel avis publié")
                            .message(avis.getAuteur()
                                    + " a donné un avis (" + avis.getNote() + "/5) sur la démarche \""
                                    + avis.getDemarcheTitre() + "\". Il est visible sur la page d'accueil.")
                            .type(NotificationType.INFORMATION)
                            .lu(false)
                            .user(admin)
                            .build()
            );
        }
    }
}
