package com.mystudentguide.service;

import com.mystudentguide.entity.Avis;
import com.mystudentguide.entity.DemandeAide;
import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.SignalementDemarche;
import com.mystudentguide.entity.StudentDemarche;
import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.entity.User;
import com.mystudentguide.repository.AvisRepository;
import com.mystudentguide.repository.DemandeAideRepository;
import com.mystudentguide.repository.NotificationRepository;
import com.mystudentguide.repository.SignalementDemarcheRepository;
import com.mystudentguide.repository.StudentDemarcheRepository;
import com.mystudentguide.repository.StudentProfileRepository;
import com.mystudentguide.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Export des données personnelles d'un utilisateur (RGPD : droit d'accès,
 * article 15, et droit à la portabilité, article 20).
 *
 * Rassemble dans un seul document tout ce que la plateforme conserve sur
 * l'utilisateur : compte, profil, démarches suivies, notifications, avis,
 * signalements et demandes d'aide. Le mot de passe (même chiffré) et les
 * jetons de réinitialisation ne sont jamais exportés.
 *
 * GET /api/account/export (utilisateur connecté).
 */
@Service
public class DonneesPersonnellesService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StudentDemarcheRepository studentDemarcheRepository;
    private final NotificationRepository notificationRepository;
    private final AvisRepository avisRepository;
    private final SignalementDemarcheRepository signalementRepository;
    private final DemandeAideRepository demandeAideRepository;

    public DonneesPersonnellesService(
            UserRepository userRepository,
            StudentProfileRepository studentProfileRepository,
            StudentDemarcheRepository studentDemarcheRepository,
            NotificationRepository notificationRepository,
            AvisRepository avisRepository,
            SignalementDemarcheRepository signalementRepository,
            DemandeAideRepository demandeAideRepository
    ) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.studentDemarcheRepository = studentDemarcheRepository;
        this.notificationRepository = notificationRepository;
        this.avisRepository = avisRepository;
        this.signalementRepository = signalementRepository;
        this.demandeAideRepository = demandeAideRepository;
    }

    /**
     * Toutes les données de l'utilisateur, prêtes à être converties en JSON.
     *
     * @param email adresse de l'utilisateur connecté
     */
    @Transactional(readOnly = true)
    public Map<String, Object> exporter(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Utilisateur connecté introuvable."));

        Map<String, Object> export = new LinkedHashMap<>();
        export.put("application", "MyStudentGuide");
        export.put("dateExport", LocalDateTime.now());
        export.put("compte", compte(user));

        Optional<StudentProfile> profil = studentProfileRepository.findByUserId(user.getId());
        export.put("profil", profil.map(this::profil).orElse(null));
        export.put("demarchesSuivies", profil
                .map(p -> studentDemarcheRepository.findByStudentProfileId(p.getId()).stream().map(this::suivi).toList())
                .orElse(List.of()));

        export.put("notifications", notificationRepository.findByUserIdOrderByDateCreationDesc(user.getId())
                .stream().map(this::notification).toList());
        export.put("avis", avisRepository.findByUserIdOrderByDateCreationDesc(user.getId())
                .stream().map(this::avis).toList());
        export.put("signalements", signalementRepository.findByUserIdOrderByDateCreationDesc(user.getId())
                .stream().map(this::signalement).toList());
        export.put("demandesAide", demandeAideRepository.findByUserIdOrderByDateCreationDesc(user.getId())
                .stream().map(this::demandeAide).toList());

        return export;
    }

    private Map<String, Object> compte(User user) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("prenom", user.getPrenom());
        m.put("nom", user.getNom());
        m.put("email", user.getEmail());
        m.put("role", user.getRole());
        m.put("dateCreation", user.getCreatedAt());
        m.put("derniereModification", user.getUpdatedAt());
        m.put("conditionsAccepteesVersion", user.getConditionsVersion());
        m.put("conditionsAccepteesLe", user.getConditionsAccepteesLe());
        m.put("derniereConnexion", user.getDerniereConnexion());
        return m;
    }

    private Map<String, Object> profil(StudentProfile p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("nationalite", p.getNationalite());
        m.put("telephone", p.getTelephone());
        m.put("niveauEtudes", p.getNiveauEtudes());
        m.put("universite", p.getUniversite());
        m.put("villeEtudes", p.getVilleEtudes());
        m.put("typeVisa", p.getTypeVisa());
        m.put("premiereArrivee", p.getPremiereArrivee());
        m.put("typeLogement", p.getTypeLogement());
        m.put("adresse", p.getAdresse());
        m.put("dateArrivee", p.getDateArrivee());
        m.put("dateFinTitreSejour", p.getDateFinTitreSejour());
        return m;
    }

    private Map<String, Object> suivi(StudentDemarche sd) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("demarche", sd.getDemarche() != null ? sd.getDemarche().getTitre() : null);
        m.put("statut", sd.getStatut());
        m.put("dateDebut", sd.getDateDebut());
        m.put("documentsEnvoyes", sd.isDocumentsEnvoyes());
        m.put("dateEnvoiDocuments", sd.getDateEnvoiDocuments());
        m.put("dateTerminaison", sd.getDateTerminaison());
        m.put("dateLimite", sd.getDateLimitePersonnelle());
        m.put("documentsPrets", new ArrayList<>(sd.getDocumentsPrets()));
        m.put("commentaire", sd.getCommentaire());
        return m;
    }

    private Map<String, Object> notification(Notification n) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("titre", n.getTitre());
        m.put("message", n.getMessage());
        m.put("type", n.getType());
        m.put("lue", n.getLu());
        m.put("date", n.getDateCreation());
        return m;
    }

    private Map<String, Object> avis(Avis a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("demarche", a.getDemarcheTitre());
        m.put("note", a.getNote());
        m.put("commentaire", a.getCommentaire());
        m.put("nomAffiche", a.getAuteur());
        m.put("visible", a.getVisible());
        m.put("date", a.getDateCreation());
        return m;
    }

    private Map<String, Object> signalement(SignalementDemarche s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("demarche", s.getDemarche() != null ? s.getDemarche().getTitre() : null);
        m.put("motif", s.getMotif());
        m.put("commentaire", s.getCommentaire());
        m.put("statut", s.getStatut());
        m.put("date", s.getDateCreation());
        m.put("dateTraitement", s.getDateTraitement());
        return m;
    }

    private Map<String, Object> demandeAide(DemandeAide d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("sujet", d.getSujet());
        m.put("demarche", d.getDemarcheTitre());
        m.put("message", d.getMessage());
        m.put("reponse", d.getReponse());
        m.put("statut", d.getStatut());
        m.put("date", d.getDateCreation());
        m.put("dateReponse", d.getDateReponse());
        return m;
    }
}
