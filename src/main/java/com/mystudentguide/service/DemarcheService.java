package com.mystudentguide.service;

import com.mystudentguide.dto.demarche.DemarcheRequest;
import com.mystudentguide.entity.Categorie;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.enums.DemarcheStatus;
import com.mystudentguide.repository.CategorieRepository;
import com.mystudentguide.repository.DemarcheRepository;
import com.mystudentguide.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DemarcheService {

    private final DemarcheRepository demarcheRepository;
    private final CategorieRepository categorieRepository;
    private final StudentProfileRepository studentProfileRepository;

    public DemarcheService(
            DemarcheRepository demarcheRepository,
            CategorieRepository categorieRepository,
            StudentProfileRepository studentProfileRepository) {

        this.demarcheRepository = demarcheRepository;
        this.categorieRepository = categorieRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    public List<Demarche> getAllDemarches() {
        return demarcheRepository.findAll();
    }

    public Optional<Demarche> getDemarcheById(Long id) {
        return demarcheRepository.findById(id);
    }

    public List<Demarche> getDemarchesByStudentProfile(
            Long studentProfileId) {

        return demarcheRepository
                .findByStudentProfileId(studentProfileId);
    }

    public List<Demarche> getDemarchesByStatus(
            Long studentProfileId,
            DemarcheStatus statut) {

        return demarcheRepository
                .findByStudentProfileIdAndStatut(
                        studentProfileId,
                        statut);
    }

    public List<Demarche> getDemarchesByCategorie(
            Long categorieId) {

        return demarcheRepository
                .findByCategorieId(categorieId);
    }

    public long countDemarchesByStatus(
            Long studentProfileId,
            DemarcheStatus statut) {

        return demarcheRepository
                .countByStudentProfileIdAndStatut(
                        studentProfileId,
                        statut);
    }

    /**
     * Création d'une nouvelle démarche.
     */
    public Demarche createDemarche(DemarcheRequest request) {

        // Recherche de la catégorie
        Categorie categorie = categorieRepository
                .findById(request.getCategorieId())
                .orElseThrow(() -> new RuntimeException(
                        "Catégorie introuvable avec l'identifiant : "
                                + request.getCategorieId()));

        // Recherche du profil étudiant
        StudentProfile studentProfile = studentProfileRepository
                .findById(request.getStudentProfileId())
                .orElseThrow(() -> new RuntimeException(
                        "Profil étudiant introuvable avec l'identifiant : "
                                + request.getStudentProfileId()));

        // Création de l'entité
        Demarche demarche = new Demarche();

        demarche.setTitre(request.getTitre());
        demarche.setDescription(request.getDescription());

        // Statut par défaut
        if (request.getStatut() != null) {
            demarche.setStatut(request.getStatut());
        } else {
            demarche.setStatut(DemarcheStatus.A_FAIRE);
        }

        demarche.setDateLimite(request.getDateLimite());

        /*
         * IMPORTANT :
         * On associe les objets récupérés à la démarche.
         */
        demarche.setCategorie(categorie);
        demarche.setStudentProfile(studentProfile);

        return demarcheRepository.save(demarche);
    }

    /**
     * Modification d'une démarche existante.
     */
    public Demarche updateDemarche(
            Long id,
            DemarcheRequest request) {

        Demarche demarche = demarcheRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Démarche introuvable avec l'identifiant : "
                                + id));

        Categorie categorie = categorieRepository
                .findById(request.getCategorieId())
                .orElseThrow(() -> new RuntimeException(
                        "Catégorie introuvable avec l'identifiant : "
                                + request.getCategorieId()));

        StudentProfile studentProfile = studentProfileRepository
                .findById(request.getStudentProfileId())
                .orElseThrow(() -> new RuntimeException(
                        "Profil étudiant introuvable avec l'identifiant : "
                                + request.getStudentProfileId()));

        demarche.setTitre(request.getTitre());
        demarche.setDescription(request.getDescription());
        demarche.setCategorie(categorie);
        demarche.setStudentProfile(studentProfile);
        demarche.setDateLimite(request.getDateLimite());

        if (request.getStatut() != null) {
            demarche.setStatut(request.getStatut());
        }

        return demarcheRepository.save(demarche);
    }

    /**
     * Modification du statut uniquement.
     */
    public Demarche updateStatus(
            Long id,
            DemarcheStatus nouveauStatut) {

        Demarche demarche = demarcheRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Démarche introuvable avec l'identifiant : "
                                + id));

        demarche.setStatut(nouveauStatut);

        return demarcheRepository.save(demarche);
    }

    /**
     * Suppression d'une démarche.
     */
    public void deleteDemarche(Long id) {
        demarcheRepository.deleteById(id);
    }
}