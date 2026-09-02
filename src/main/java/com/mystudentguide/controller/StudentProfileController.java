package com.mystudentguide.controller;

import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.service.StudentProfileService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

/**
 * Controller responsable de la gestion des profils étudiants.
 *
 * Architecture :
 *
 * Client / Angular
 *        ↓
 * StudentProfileController
 *        ↓
 * StudentProfileService
 *        ↓
 * StudentProfileRepository
 *        ↓
 * MySQL
 */
@RestController
@RequestMapping("/api/student-profiles")
public class StudentProfileController {

    /**
     * Service permettant de gérer les profils étudiants.
     */
    private final StudentProfileService studentProfileService;

    /**
     * Injection du service par constructeur.
     *
     * @param studentProfileService service des profils
     */
    public StudentProfileController(
            StudentProfileService studentProfileService) {

        this.studentProfileService = studentProfileService;
    }

    /**
     * =========================================================
     * GET - RÉCUPÉRER TOUS LES PROFILS
     * =========================================================
     *
     * Endpoint :
     *
     * GET /api/student-profiles
     *
     * @return liste de tous les profils
     */
    @GetMapping
    public ResponseEntity<List<StudentProfile>> getAllProfiles() {

        List<StudentProfile> profiles =
                studentProfileService.getAllProfiles();

        return ResponseEntity.ok(profiles);
    }

    /**
     * =========================================================
     * GET - RÉCUPÉRER UN PROFIL PAR SON ID
     * =========================================================
     *
     * Endpoint :
     *
     * GET /api/student-profiles/{id}
     *
     * Exemple :
     *
     * GET /api/student-profiles/1
     *
     * @param id identifiant du profil
     * @return profil ou HTTP 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentProfile> getProfileById(
            @PathVariable Long id) {

        Optional<StudentProfile> profile =
                studentProfileService.getProfileById(id);

        /*
         * Si le profil existe :
         *
         * HTTP 200
         *
         * Sinon :
         *
         * HTTP 404
         */
        return profile
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound().build()
                );
    }

    /**
     * =========================================================
     * GET - RÉCUPÉRER UN PROFIL PAR USER ID
     * =========================================================
     *
     * Endpoint :
     *
     * GET /api/student-profiles/user/{userId}
     *
     * Exemple :
     *
     * GET /api/student-profiles/user/1
     *
     * @param userId identifiant de l'utilisateur
     * @return profil ou HTTP 404
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<StudentProfile> getProfileByUserId(
            @PathVariable Long userId) {

        Optional<StudentProfile> profile =
                studentProfileService.getProfileByUserId(userId);

        return profile
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound().build()
                );
    }

    /**
     * =========================================================
     * GET - VÉRIFIER SI UN PROFIL EXISTE
     * =========================================================
     *
     * Endpoint :
     *
     * GET /api/student-profiles/user/{userId}/exists
     *
     * Exemple :
     *
     * GET /api/student-profiles/user/1/exists
     *
     * @param userId identifiant de l'utilisateur
     * @return true ou false
     */
    @GetMapping("/user/{userId}/exists")
    public ResponseEntity<Boolean> profileExists(
            @PathVariable Long userId) {

        boolean exists =
                studentProfileService.profileExistsForUser(userId);

        return ResponseEntity.ok(exists);
    }

    /**
     * =========================================================
     * POST - CRÉER UN PROFIL
     * =========================================================
     *
     * Endpoint :
     *
     * POST /api/student-profiles
     *
     * IMPORTANT :
     *
     * Le client n'envoie PAS l'utilisateur dans le JSON.
     *
     * L'utilisateur est automatiquement récupéré
     * grâce au JWT.
     *
     * Exemple :
     *
     * JWT
     * ↓
     * test@gmail.com
     * ↓
     * authentication.getName()
     * ↓
     * StudentProfileService
     * ↓
     * UserRepository.findByEmail()
     * ↓
     * studentProfile.setUser(user)
     * ↓
     * INSERT avec user_id
     *
     * @param studentProfile profil envoyé par le client
     * @param authentication utilisateur authentifié
     * @return profil créé
     */
    @PostMapping
    public ResponseEntity<StudentProfile> createProfile(
            @RequestBody StudentProfile studentProfile,
            Authentication authentication) {

        /*
         * Spring Security connaît l'utilisateur connecté
         * grâce au JWT.
         *
         * Dans ton application, getName() retourne
         * l'adresse email.
         *
         * Exemple :
         *
         * test@gmail.com
         */
        String email = authentication.getName();

        /*
         * On transmet le profil et l'email au service.
         *
         * Le service va retrouver le User
         * et l'associer au StudentProfile.
         */
        StudentProfile savedProfile =
                studentProfileService.saveProfile(
                        studentProfile,
                        email
                );

        /*
         * HTTP 201 = création réussie.
         */
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedProfile);
    }

    /**
     * =========================================================
     * PUT - MODIFIER UN PROFIL
     * =========================================================
     *
     * Endpoint :
     *
     * PUT /api/student-profiles/{id}
     *
     * Exemple :
     *
     * PUT /api/student-profiles/1
     *
     * @param id identifiant du profil
     * @param studentProfile nouvelles données
     * @param authentication utilisateur connecté
     * @return profil modifié
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudentProfile> updateProfile(
            @PathVariable Long id,
            @RequestBody StudentProfile studentProfile,
            Authentication authentication) {

        /*
         * Vérifie que le profil existe.
         */
        Optional<StudentProfile> existingProfile =
                studentProfileService.getProfileById(id);

        if (existingProfile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        /*
         * On conserve l'ID du profil existant.
         */
        studentProfile.setId(id);

        /*
         * Récupération de l'email depuis le JWT.
         */
        String email = authentication.getName();

        /*
         * Le service va retrouver l'utilisateur
         * et associer le profil à cet utilisateur.
         */
        StudentProfile updatedProfile =
                studentProfileService.saveProfile(
                        studentProfile,
                        email
                );

        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * =========================================================
     * GET - RÉCUPÉRER MON PROFIL
     * =========================================================
     *
     * Endpoint :
     *
     * GET /api/student-profiles/me
     *
     * Cette route est particulièrement intéressante
     * pour Angular.
     *
     * Le frontend n'a pas besoin de connaître le userId.
     *
     * Le JWT permet automatiquement de savoir
     * quel utilisateur fait la requête.
     *
     * Exemple :
     *
     * Authorization: Bearer JWT
     *
     * JWT
     * ↓
     * test@gmail.com
     * ↓
     * recherche du StudentProfile
     *
     * @param authentication utilisateur connecté
     * @return profil ou HTTP 404
     */
    @GetMapping("/me")
    public ResponseEntity<StudentProfile> getMyProfile(
            Authentication authentication) {

        /*
         * Récupération de l'email depuis le JWT.
         */
        String email = authentication.getName();

        /*
         * Recherche du profil correspondant
         * à cet utilisateur.
         */
        Optional<StudentProfile> profile =
                studentProfileService.getProfileByUserEmail(email);

        /*
         * Si le profil existe :
         *
         * HTTP 200
         *
         * Sinon :
         *
         * HTTP 404
         */
        return profile
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound().build()
                );
    }

    /**
     * =========================================================
     * DELETE - SUPPRIMER UN PROFIL
     * =========================================================
     *
     * Endpoint :
     *
     * DELETE /api/student-profiles/{id}
     *
     * Exemple :
     *
     * DELETE /api/student-profiles/1
     *
     * @param id identifiant du profil
     * @return HTTP 204 si suppression réussie
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(
            @PathVariable Long id) {

        /*
         * Vérifie que le profil existe.
         */
        Optional<StudentProfile> existingProfile =
                studentProfileService.getProfileById(id);

        if (existingProfile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        /*
         * Suppression du profil.
         */
        studentProfileService.deleteProfile(id);

        /*
         * HTTP 204 = suppression réussie
         * sans contenu dans la réponse.
         */
        return ResponseEntity.noContent().build();
    }
}