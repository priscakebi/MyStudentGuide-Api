package com.mystudentguide.service;

import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service responsable de la gestion des profils étudiants.
 *
 * Le StudentProfile contient les informations nécessaires
 * pour déterminer les démarches administratives adaptées
 * à la situation de l'étudiant.
 *
 * Architecture :
 *
 * Controller
 *      ↓
 * StudentProfileService
 *      ↓
 * StudentProfileRepository
 *      ↓
 * MySQL
 */
@Service
public class StudentProfileService {

    /**
     * Repository permettant d'accéder aux profils étudiants
     * enregistrés dans la base de données.
     */
    private final StudentProfileRepository studentProfileRepository;

    /**
     * Injection du repository par constructeur.
     *
     * @param studentProfileRepository repository des profils étudiants
     */
    public StudentProfileService(
            StudentProfileRepository studentProfileRepository
    ) {
        this.studentProfileRepository = studentProfileRepository;
    }

    /**
     * Récupère tous les profils étudiants.
     *
     * Cette méthode pourra notamment être utilisée
     * par l'administration.
     *
     * @return liste de tous les profils étudiants
     */
    public List<StudentProfile> getAllProfiles() {
        return studentProfileRepository.findAll();
    }

    /**
     * Recherche un profil étudiant à partir de son identifiant.
     *
     * @param id identifiant du profil
     * @return profil trouvé ou Optional.empty()
     */
    public Optional<StudentProfile> getProfileById(Long id) {
        return studentProfileRepository.findById(id);
    }

    /**
     * Recherche le profil étudiant associé à un utilisateur.
     *
     * Un utilisateur possède au maximum un profil étudiant.
     *
     * @param userId identifiant de l'utilisateur
     * @return profil étudiant s'il existe
     */
    public Optional<StudentProfile> getProfileByUserId(Long userId) {
        return studentProfileRepository.findByUserId(userId);
    }

    /**
     * Vérifie si un utilisateur possède déjà un profil étudiant.
     *
     * Cette vérification est importante avant de créer
     * un nouveau profil.
     *
     * @param userId identifiant de l'utilisateur
     * @return true si le profil existe déjà
     */
    public boolean profileExistsForUser(Long userId) {
        return studentProfileRepository.existsByUserId(userId);
    }

    /**
     * Enregistre un nouveau profil ou met à jour
     * un profil existant.
     *
     * @param studentProfile profil à sauvegarder
     * @return profil enregistré
     */
    public StudentProfile saveProfile(StudentProfile studentProfile) {
        return studentProfileRepository.save(studentProfile);
    }

    /**
     * Supprime un profil étudiant à partir de son identifiant.
     *
     * @param id identifiant du profil
     */
    public void deleteProfile(Long id) {
        studentProfileRepository.deleteById(id);
    }
}