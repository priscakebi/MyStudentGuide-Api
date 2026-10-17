package com.mystudentguide.service;

import com.mystudentguide.dto.demarche.ConseilDtos.AstuceDuJour;
import com.mystudentguide.dto.demarche.ConseilDtos.ConseilDTO;
import com.mystudentguide.dto.demarche.ConseilDtos.ConseilRequest;
import com.mystudentguide.entity.ConseilDemarche;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.StudentDemarche;
import com.mystudentguide.enums.DemarcheStatus;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.repository.StudentDemarcheRepository;
import com.mystudentguide.repository.StudentProfileRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Conseils pratiques des démarches : gestion par l'admin et « Astuce du jour ».
 */
@Service
public class ConseilService {

    private final DemarcheRepository demarcheRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StudentDemarcheRepository studentDemarcheRepository;
    private final Clock clock;

    @Autowired
    public ConseilService(
            DemarcheRepository demarcheRepository,
            StudentProfileRepository studentProfileRepository,
            StudentDemarcheRepository studentDemarcheRepository
    ) {
        this(demarcheRepository, studentProfileRepository, studentDemarcheRepository, Clock.systemDefaultZone());
    }

    /** Constructeur des tests : horloge contrôlée. */
    ConseilService(
            DemarcheRepository demarcheRepository,
            StudentProfileRepository studentProfileRepository,
            StudentDemarcheRepository studentDemarcheRepository,
            Clock clock
    ) {
        this.demarcheRepository = demarcheRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.studentDemarcheRepository = studentDemarcheRepository;
        this.clock = clock;
    }

    // ============================================================
    // GESTION PAR L'ADMIN (renvoie toujours la liste à jour)
    // ============================================================

    @Transactional(readOnly = true)
    public List<ConseilDTO> lister(Long demarcheId) {
        return dtos(getDemarche(demarcheId));
    }

    @Transactional
    public List<ConseilDTO> ajouter(Long demarcheId, ConseilRequest request) {
        Demarche demarche = getDemarche(demarcheId);
        demarche.getConseils().add(new ConseilDemarche(
                request.texte().trim(), nettoyer(request.texteEn()), demarche));
        return dtos(demarcheRepository.saveAndFlush(demarche));
    }

    @Transactional
    public List<ConseilDTO> modifier(Long demarcheId, Long conseilId, ConseilRequest request) {
        Demarche demarche = getDemarche(demarcheId);
        ConseilDemarche conseil = trouver(demarche, conseilId);
        conseil.setTexte(request.texte().trim());
        conseil.setTexteEn(nettoyer(request.texteEn()));
        return dtos(demarcheRepository.saveAndFlush(demarche));
    }

    @Transactional
    public List<ConseilDTO> supprimer(Long demarcheId, Long conseilId) {
        Demarche demarche = getDemarche(demarcheId);
        demarche.getConseils().remove(trouver(demarche, conseilId));
        return dtos(demarcheRepository.saveAndFlush(demarche));
    }

    /** Conseils d'une démarche, pour la fiche publique (DemarcheService). */
    public static List<ConseilDTO> dtos(Demarche demarche) {
        return demarche.getConseils().stream()
                .sorted(Comparator.comparing(ConseilDemarche::getId, Comparator.nullsLast(Long::compare)))
                .map(c -> new ConseilDTO(c.getId(), c.getTexte(), c.getTexteEn()))
                .toList();
    }

    // ============================================================
    // ASTUCE DU JOUR
    // ============================================================

    /**
     * Un conseil tiré des démarches suivies par l'étudiant (en cours d'abord,
     * terminées sinon). Le même toute la journée, un autre le lendemain.
     *
     * @return vide si l'étudiant ne suit aucune démarche ayant des conseils
     */
    @Transactional(readOnly = true)
    public Optional<AstuceDuJour> astuceDuJour(String email) {
        return studentProfileRepository.findByUserEmail(email).flatMap(profil -> {
            List<StudentDemarche> suivies = studentDemarcheRepository.findByStudentProfileId(profil.getId());

            List<ConseilDemarche> conseils = conseilsDe(suivies, DemarcheStatus.EN_COURS);
            if (conseils.isEmpty()) {
                conseils = conseilsDe(suivies, null);
            }
            if (conseils.isEmpty()) {
                return Optional.empty();
            }

            long graine = LocalDate.now(clock).toEpochDay() + profil.getId();
            ConseilDemarche choisi = conseils.get((int) Math.floorMod(graine, conseils.size()));
            Demarche demarche = choisi.getDemarche();

            return Optional.of(new AstuceDuJour(
                    choisi.getTexte(), choisi.getTexteEn(),
                    demarche.getId(), demarche.getTitre(), demarche.getTitreEn()));
        });
    }

    /** Conseils des démarches suivies ayant ce statut (null : toutes), triés par identifiant. */
    private static List<ConseilDemarche> conseilsDe(List<StudentDemarche> suivies, DemarcheStatus statut) {
        return suivies.stream()
                .filter(sd -> statut == null || sd.getStatut() == statut)
                .map(StudentDemarche::getDemarche)
                .filter(Objects::nonNull)
                .distinct()
                .flatMap(d -> d.getConseils().stream())
                .sorted(Comparator.comparing(ConseilDemarche::getId, Comparator.nullsLast(Long::compare)))
                .toList();
    }

    // ============================================================
    // OUTILS
    // ============================================================

    private Demarche getDemarche(Long id) {
        return demarcheRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Démarche introuvable avec l'id : " + id));
    }

    private static ConseilDemarche trouver(Demarche demarche, Long conseilId) {
        return demarche.getConseils().stream()
                .filter(c -> Objects.equals(c.getId(), conseilId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Conseil introuvable dans cette démarche."));
    }

    private static String nettoyer(String texte) {
        if (texte == null) {
            return null;
        }
        String t = texte.trim();
        return t.isEmpty() ? null : t;
    }
}
