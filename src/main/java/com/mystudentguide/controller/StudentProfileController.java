package com.mystudentguide.controller;

import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.entity.User;
import com.mystudentguide.repository.UserRepository;
import com.mystudentguide.service.StudentProfileService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller des profils étudiants.
 *
 * Un profil contient des données personnelles (téléphone, adresse,
 * nationalité, visa...). Les règles d'accès sont donc strictes :
 *
 * - liste de tous les profils : ADMIN uniquement ;
 * - lecture d'un profil       : son propriétaire ou un ADMIN ;
 * - modification              : son propriétaire uniquement ;
 * - suppression               : son propriétaire ou un ADMIN.
 *
 * Avant ces contrôles, n'importe quel utilisateur connecté pouvait lire,
 * modifier (et même s'approprier) ou supprimer le profil d'un autre étudiant
 * en changeant l'identifiant dans l'URL.
 */
@RestController
@RequestMapping("/api/student-profiles")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;
    private final UserRepository userRepository;

    public StudentProfileController(
            StudentProfileService studentProfileService,
            UserRepository userRepository) {
        this.studentProfileService = studentProfileService;
        this.userRepository = userRepository;
    }

    /** Liste de tous les profils : réservée aux administrateurs. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentProfile>> getAllProfiles() {
        return ResponseEntity.ok(studentProfileService.getAllProfiles());
    }

    /** Profil de l'utilisateur connecté. */
    @GetMapping("/me")
    public ResponseEntity<StudentProfile> getMyProfile(
            Authentication authentication) {

        return studentProfileService
                .getProfileByUserEmail(authentication.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Profil par identifiant : son propriétaire ou un ADMIN. */
    @GetMapping("/{id}")
    public ResponseEntity<StudentProfile> getProfileById(
            @PathVariable Long id,
            Authentication authentication) {

        if (!isAdmin(authentication) && !ownsProfile(id, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return studentProfileService.getProfileById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Profil d'un utilisateur : cet utilisateur ou un ADMIN. */
    @GetMapping("/user/{userId}")
    public ResponseEntity<StudentProfile> getProfileByUserId(
            @PathVariable Long userId,
            Authentication authentication) {

        if (!isAdmin(authentication) && !isCurrentUser(userId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return studentProfileService.getProfileByUserId(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Indique si un utilisateur possède un profil : lui-même ou un ADMIN. */
    @GetMapping("/user/{userId}/exists")
    public ResponseEntity<Boolean> profileExists(
            @PathVariable Long userId,
            Authentication authentication) {

        if (!isAdmin(authentication) && !isCurrentUser(userId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                studentProfileService.profileExistsForUser(userId));
    }

    /**
     * Crée le profil de l'utilisateur connecté.
     *
     * - l'identifiant envoyé par le client est ignoré ;
     * - 409 si l'utilisateur a déjà un profil (utiliser PUT /me).
     */
    @PostMapping
    public ResponseEntity<StudentProfile> createProfile(
            @RequestBody StudentProfile studentProfile,
            Authentication authentication) {

        Long currentUserId = currentUserId(authentication);

        if (currentUserId != null
                && studentProfileService.profileExistsForUser(currentUserId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        studentProfile.setId(null);

        StudentProfile savedProfile =
                studentProfileService.saveProfile(
                        studentProfile,
                        authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedProfile);
    }

    /** Modifie le profil de l'utilisateur connecté. */
    @PutMapping("/me")
    public ResponseEntity<StudentProfile> updateMyProfile(
            @RequestBody StudentProfile studentProfile,
            Authentication authentication) {

        String email = authentication.getName();

        Optional<StudentProfile> existingProfile =
                studentProfileService.getProfileByUserEmail(email);

        if (existingProfile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        studentProfile.setId(existingProfile.get().getId());

        return ResponseEntity.ok(
                studentProfileService.saveProfile(studentProfile, email));
    }

    /**
     * Modifie un profil par identifiant : uniquement son propriétaire.
     *
     * Un administrateur n'a pas le droit de modifier le profil d'un étudiant
     * ici : l'enregistrement rattache toujours le profil à l'utilisateur
     * connecté, ce qui déplacerait le profil vers le compte de l'admin.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudentProfile> updateProfile(
            @PathVariable Long id,
            @RequestBody StudentProfile studentProfile,
            Authentication authentication) {

        if (!ownsProfile(id, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        studentProfile.setId(id);

        return ResponseEntity.ok(
                studentProfileService.saveProfile(
                        studentProfile,
                        authentication.getName()));
    }

    /** Supprime un profil : son propriétaire ou un ADMIN. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(
            @PathVariable Long id,
            Authentication authentication) {

        if (!isAdmin(authentication) && !ownsProfile(id, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (studentProfileService.getProfileById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        studentProfileService.deleteProfile(id);

        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Méthodes privées
    // ------------------------------------------------------------------

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /** Identifiant de l'utilisateur connecté (null si introuvable). */
    private Long currentUserId(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .map(User::getId)
                .orElse(null);
    }

    private boolean isCurrentUser(Long userId, Authentication authentication) {
        Long currentUserId = currentUserId(authentication);
        return currentUserId != null && currentUserId.equals(userId);
    }

    /** Vrai si le profil demandé est celui de l'utilisateur connecté. */
    private boolean ownsProfile(Long profileId, Authentication authentication) {
        return studentProfileService
                .getProfileByUserEmail(authentication.getName())
                .map(profile -> profile.getId().equals(profileId))
                .orElse(false);
    }
}
