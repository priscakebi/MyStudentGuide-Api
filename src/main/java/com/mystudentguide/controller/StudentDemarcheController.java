package com.mystudentguide.controller;

import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.StudentDemarche;
import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.DemarcheStatus;
import com.mystudentguide.repository.UserRepository;
import com.mystudentguide.service.StudentDemarcheService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller responsable de la gestion du suivi des démarches
 * par les étudiants.
 *
 * ============================================================
 * RÔLE DU CONTROLLER
 * ============================================================
 *
 * Il faut distinguer deux notions :
 *
 * 1. Demarche
 *    -> représente une démarche administrative générale.
 *
 *    Exemple :
 *    "Renouvellement du titre de séjour"
 *
 * 2. StudentDemarche
 *    -> représente le suivi de cette démarche par un étudiant.
 *
 *    Exemple :
 *
 *    Étudiant A -> Renouvellement du titre de séjour -> EN_COURS
 *    Étudiant B -> Renouvellement du titre de séjour -> A_FAIRE
 *
 * ============================================================
 * SÉCURITÉ
 * ============================================================
 *
 * Un étudiant ne doit JAMAIS pouvoir manipuler le suivi
 * d'un autre étudiant.
 *
 * Exemple :
 *
 * Étudiant A connecté
 *      ↓
 * peut gérer uniquement
 *      ↓
 * ses propres StudentDemarche
 *
 * L'ADMIN peut gérer les StudentDemarche de tous les étudiants.
 *
 * ============================================================
 * ARCHITECTURE
 * ============================================================
 *
 * Angular
 *     ↓
 * JWT
 *     ↓
 * Spring Security
 *     ↓
 * StudentDemarcheController
 *     ↓
 * StudentDemarcheService
 *     ↓
 * StudentDemarcheRepository
 *     ↓
 * MySQL
 */
@RestController
@RequestMapping("/api/student-demarches")
public class StudentDemarcheController {

    /**
     * Service contenant la logique métier
     * concernant les démarches suivies par les étudiants.
     */
    private final StudentDemarcheService studentDemarcheService;

    /**
     * Repository permettant de retrouver l'utilisateur
     * actuellement connecté.
     */
    private final UserRepository userRepository;


    /**
     * ============================================================
     * CONSTRUCTEUR
     * ============================================================
     *
     * Spring injecte automatiquement les dépendances nécessaires.
     *
     * @param studentDemarcheService service de gestion des démarches
     * @param userRepository repository des utilisateurs
     */
    public StudentDemarcheController(
            StudentDemarcheService studentDemarcheService,
            UserRepository userRepository) {

        this.studentDemarcheService = studentDemarcheService;
        this.userRepository = userRepository;
    }


    /**
     * ============================================================
     * RÉCUPÉRER TOUTES LES DÉMARCHES ÉTUDIANTES
     * ============================================================
     *
     * Endpoint :
     *
     * GET /api/student-demarches
     *
     * Cette route est réservée à l'administration.
     *
     * Un étudiant n'a pas besoin de récupérer les démarches
     * de tous les étudiants.
     *
     * @return liste de toutes les StudentDemarche
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentDemarche>> getAllStudentDemarches() {

        List<StudentDemarche> studentDemarches =
                studentDemarcheService.getAllStudentDemarches();

        return ResponseEntity.ok(studentDemarches);
    }


    /**
     * ============================================================
     * RÉCUPÉRER UNE DÉMARCHE ÉTUDIANTE PAR SON ID
     * ============================================================
     *
     * Endpoint :
     *
     * GET /api/student-demarches/{id}
     *
     * Un étudiant peut consulter une démarche uniquement si
     * cette démarche lui appartient.
     *
     * L'ADMIN peut consulter n'importe quelle démarche.
     *
     * @param id identifiant du StudentDemarche
     * @param authentication utilisateur actuellement connecté
     * @return StudentDemarche
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ETUDIANT')")
    public ResponseEntity<StudentDemarche> getStudentDemarcheById(
            @PathVariable Long id,
            Authentication authentication) {

        try {

            /*
             * Recherche de la StudentDemarche.
             */
            StudentDemarche studentDemarche =
                    studentDemarcheService.getStudentDemarcheById(id);

            /*
             * Si l'utilisateur est un étudiant,
             * on vérifie qu'il est bien propriétaire
             * de cette StudentDemarche.
             */
            if (isStudent(authentication)
                    && !belongsToCurrentStudent(
                            studentDemarche,
                            authentication)) {

                /*
                 * On ne révèle pas qu'une autre StudentDemarche
                 * existe.
                 *
                 * HTTP 403 = accès interdit.
                 */
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(studentDemarche);

        } catch (RuntimeException exception) {

            return ResponseEntity.notFound().build();
        }
    }


    /**
     * ============================================================
     * RÉCUPÉRER LES DÉMARCHES D'UN ÉTUDIANT
     * ============================================================
     *
     * Endpoint :
     *
     * GET /api/student-demarches/student/{studentProfileId}
     *
     * ADMIN :
     * peut demander les démarches de n'importe quel étudiant.
     *
     * ETUDIANT :
     * ne peut demander que ses propres démarches.
     *
     * @param studentProfileId identifiant du profil étudiant
     * @param authentication utilisateur connecté
     * @return liste des démarches
     */
    @GetMapping("/student/{studentProfileId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ETUDIANT')")
    public ResponseEntity<List<StudentDemarche>> getDemarchesByStudent(
            @PathVariable Long studentProfileId,
            Authentication authentication) {

        /*
         * Vérification de propriété pour un étudiant.
         */
        if (isStudent(authentication)
                && !isCurrentStudent(studentProfileId, authentication)) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<StudentDemarche> studentDemarches =
                studentDemarcheService.getDemarchesByStudent(
                        studentProfileId
                );

        return ResponseEntity.ok(studentDemarches);
    }


    /**
     * ============================================================
     * RÉCUPÉRER LES DÉMARCHES D'UN ÉTUDIANT PAR STATUT
     * ============================================================
     *
     * Endpoint :
     *
     * GET /api/student-demarches/student/{studentProfileId}/statut/{statut}
     *
     * L'étudiant peut uniquement filtrer ses propres démarches.
     *
     * L'ADMIN peut filtrer celles de n'importe quel étudiant.
     *
     * @param studentProfileId identifiant du profil étudiant
     * @param statut statut recherché
     * @param authentication utilisateur connecté
     * @return démarches correspondant au statut
     */
    @GetMapping("/student/{studentProfileId}/statut/{statut}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ETUDIANT')")
    public ResponseEntity<List<StudentDemarche>>
    getDemarchesByStudentAndStatus(
            @PathVariable Long studentProfileId,
            @PathVariable DemarcheStatus statut,
            Authentication authentication) {

        /*
         * Un étudiant ne peut consulter que son propre profil.
         */
        if (isStudent(authentication)
                && !isCurrentStudent(studentProfileId, authentication)) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<StudentDemarche> studentDemarches =
                studentDemarcheService.getDemarchesByStudentAndStatus(
                        studentProfileId,
                        statut
                );

        return ResponseEntity.ok(studentDemarches);
    }


    /**
     * ============================================================
     * RÉCUPÉRER LES DÉMARCHES PAR STATUT
     * ============================================================
     *
     * Endpoint :
     *
     * GET /api/student-demarches/statut/{statut}
     *
     * Cette route permet de récupérer toutes les démarches
     * d'un statut donné.
     *
     * Elle est réservée à l'administration.
     *
     * @param statut statut recherché
     * @return liste des StudentDemarche
     */
    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentDemarche>>
    getDemarchesByStatus(
            @PathVariable DemarcheStatus statut) {

        List<StudentDemarche> studentDemarches =
                studentDemarcheService.getDemarchesByStatus(statut);

        return ResponseEntity.ok(studentDemarches);
    }


    /**
     * ============================================================
     * MES DÉMARCHES COMMENCÉES
     * ============================================================
     *
     * Endpoint :
     *
     * GET /api/student-demarches/me
     *
     * Retourne uniquement les démarches que l'étudiant connecté
     * a commencées (EN_COURS ou TERMINEE). Liste vide si l'étudiant
     * n'a encore rien commencé ou n'a pas de profil.
     *
     * @param authentication utilisateur connecté
     * @return démarches commencées
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<List<StudentDemarche>> getMyStartedDemarches(
            Authentication authentication) {

        return ResponseEntity.ok(
                studentDemarcheService.getStartedDemarches(
                        authentication.getName()
                )
        );
    }


    /**
     * ============================================================
     * COMMENCER UNE DÉMARCHE
     * ============================================================
     *
     * Endpoint :
     *
     * POST /api/student-demarches/start/{demarcheId}
     *
     * Réservé aux étudiants connectés. Le suivi est toujours
     * créé pour l'étudiant connecté (jamais pour un autre),
     * directement au statut EN_COURS.
     *
     * @param demarcheId démarche à commencer
     * @param authentication utilisateur connecté
     * @return suivi de la démarche
     */
    @PostMapping("/start/{demarcheId}")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<StudentDemarche> startDemarche(
            @PathVariable Long demarcheId,
            Authentication authentication) {

        // IllegalArgumentException (démarche inconnue) → 400 via GlobalExceptionHandler.
        return ResponseEntity.ok(
                studentDemarcheService.startDemarche(
                        authentication.getName(),
                        demarcheId
                )
        );
    }


    /**
     * ============================================================
     * SUGGÉRER UNE DÉMARCHE À UN ÉTUDIANT
     * ============================================================
     *
     * Endpoint :
     *
     * POST /api/student-demarches/suggest
     * ?studentProfileId=1&demarcheId=5
     *
     * Remplace l'ancienne attribution (/assign) : l'étudiant
     * reçoit une notification de suggestion avec les boutons
     * « Voir plus » et « Commencer ». Aucun suivi n'est créé.
     *
     * Réservé à l'ADMIN.
     *
     * @param studentProfileId profil de l'étudiant ciblé
     * @param demarcheId démarche suggérée
     * @return notification créée
     */
    @PostMapping("/suggest")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Notification> suggestDemarche(
            @RequestParam Long studentProfileId,
            @RequestParam Long demarcheId) {

        // IllegalArgumentException (déjà commencée, déjà suggérée...) → 400.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(studentDemarcheService.suggestDemarche(
                        studentProfileId,
                        demarcheId
                ));
    }


    /**
     * ============================================================
     * MODIFIER LE STATUT D'UNE DÉMARCHE
     * ============================================================
     *
     * Endpoint :
     *
     * PATCH /api/student-demarches/{id}/statut?statut=EN_COURS
     *
     * L'étudiant peut modifier uniquement sa propre démarche.
     *
     * L'ADMIN peut modifier n'importe quelle démarche.
     *
     * @param id identifiant de la StudentDemarche
     * @param statut nouveau statut
     * @param authentication utilisateur connecté
     * @return StudentDemarche mise à jour
     */
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'ETUDIANT')")
    public ResponseEntity<StudentDemarche> updateStatus(
            @PathVariable Long id,
            @RequestParam DemarcheStatus statut,
            Authentication authentication) {

        try {

            /*
             * Recherche de la démarche.
             */
            StudentDemarche studentDemarche =
                    studentDemarcheService.getStudentDemarcheById(id);

            /*
             * Vérification de propriété pour un étudiant.
             */
            if (isStudent(authentication)
                    && !belongsToCurrentStudent(
                            studentDemarche,
                            authentication)) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            /*
             * Mise à jour du statut.
             */
            StudentDemarche updatedStudentDemarche =
                    studentDemarcheService.updateStatus(
                            id,
                            statut
                    );

            return ResponseEntity.ok(updatedStudentDemarche);

        } catch (RuntimeException exception) {

            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Réponse de l'étudiant au rappel de J+10.
     *
     * PATCH /api/student-demarches/{id}/documents?envoyes=true|false
     */
    @PatchMapping("/{id}/documents")
    @PreAuthorize("hasAnyRole('ADMIN', 'ETUDIANT')")
    public ResponseEntity<StudentDemarche> confirmerEnvoiDocuments(
            @PathVariable Long id,
            @RequestParam boolean envoyes,
            Authentication authentication) {

        StudentDemarche studentDemarche;

        try {
            studentDemarche =
                    studentDemarcheService.getStudentDemarcheById(id);
        } catch (RuntimeException exception) {
            return ResponseEntity.notFound().build();
        }

        if (isStudent(authentication)
                && !belongsToCurrentStudent(
                        studentDemarche,
                        authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // IllegalArgumentException → 400 via GlobalExceptionHandler.
        return ResponseEntity.ok(
                studentDemarcheService.confirmerEnvoiDocuments(id, envoyes)
        );
    }


    /**
     * ============================================================
     * MODIFIER LE COMMENTAIRE
     * ============================================================
     *
     * Endpoint :
     *
     * PATCH /api/student-demarches/{id}/commentaire
     *
     * L'étudiant peut modifier uniquement le commentaire
     * de sa propre démarche.
     *
     * @param id identifiant de la StudentDemarche
     * @param commentaire nouveau commentaire
     * @param authentication utilisateur connecté
     * @return StudentDemarche mise à jour
     */
    @PatchMapping("/{id}/commentaire")
    @PreAuthorize("hasAnyRole('ADMIN', 'ETUDIANT')")
    public ResponseEntity<StudentDemarche> updateCommentaire(
            @PathVariable Long id,
            @RequestBody String commentaire,
            Authentication authentication) {

        try {

            /*
             * Recherche de la démarche.
             */
            StudentDemarche studentDemarche =
                    studentDemarcheService.getStudentDemarcheById(id);

            /*
             * Vérification de propriété.
             */
            if (isStudent(authentication)
                    && !belongsToCurrentStudent(
                            studentDemarche,
                            authentication)) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            /*
             * Mise à jour du commentaire.
             */
            StudentDemarche updatedStudentDemarche =
                    studentDemarcheService.updateCommentaire(
                            id,
                            commentaire
                    );

            return ResponseEntity.ok(updatedStudentDemarche);

        } catch (RuntimeException exception) {

            return ResponseEntity.notFound().build();
        }
    }


    /**
     * ============================================================
     * COMPTER LES DÉMARCHES D'UN ÉTUDIANT
     * ============================================================
     *
     * Endpoint :
     *
     * GET /api/student-demarches/student/{studentProfileId}/count
     * ?statut=TERMINEE
     *
     * L'étudiant peut uniquement obtenir ses propres statistiques.
     *
     * L'ADMIN peut obtenir les statistiques de n'importe quel étudiant.
     *
     * @param studentProfileId identifiant du profil étudiant
     * @param statut statut à compter
     * @param authentication utilisateur connecté
     * @return nombre de démarches
     */
    @GetMapping("/student/{studentProfileId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'ETUDIANT')")
    public ResponseEntity<Long> countByStudentAndStatus(
            @PathVariable Long studentProfileId,
            @RequestParam DemarcheStatus statut,
            Authentication authentication) {

        /*
         * Vérification de propriété.
         */
        if (isStudent(authentication)
                && !isCurrentStudent(studentProfileId, authentication)) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        long count =
                studentDemarcheService.countByStudentAndStatus(
                        studentProfileId,
                        statut
                );

        return ResponseEntity.ok(count);
    }


    /**
     * ============================================================
     * SUPPRIMER UNE DÉMARCHE ÉTUDIANTE
     * ============================================================
     *
     * Endpoint :
     *
     * DELETE /api/student-demarches/{id}
     *
     * IMPORTANT :
     *
     * Cette méthode supprime uniquement le suivi personnel
     * de la démarche.
     *
     * Elle ne supprime PAS la Demarche générale.
     *
     * Exemple :
     *
     * Demarche :
     * "Renouvellement du titre de séjour"
     *
     * StudentDemarche :
     * Étudiant A -> cette démarche
     *
     * Si l'étudiant supprime sa StudentDemarche,
     * la démarche générale reste disponible pour les autres étudiants.
     *
     * @param id identifiant du StudentDemarche
     * @param authentication utilisateur connecté
     * @return HTTP 204 si suppression réussie
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ETUDIANT')")
    public ResponseEntity<Void> deleteStudentDemarche(
            @PathVariable Long id,
            Authentication authentication) {

        try {

            /*
             * Recherche de la démarche.
             */
            StudentDemarche studentDemarche =
                    studentDemarcheService.getStudentDemarcheById(id);

            /*
             * Vérification de propriété.
             *
             * Un étudiant ne peut supprimer que son propre suivi.
             */
            if (isStudent(authentication)
                    && !belongsToCurrentStudent(
                            studentDemarche,
                            authentication)) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            /*
             * Suppression de l'association étudiant/démarche.
             */
            studentDemarcheService.deleteStudentDemarche(id);

            /*
             * HTTP 204 = suppression réussie.
             */
            return ResponseEntity.noContent().build();

        } catch (RuntimeException exception) {

            return ResponseEntity.notFound().build();
        }
    }


    // ============================================================
    // MÉTHODES PRIVÉES DE SÉCURITÉ
    // ============================================================

    /**
     * Vérifie si l'utilisateur actuellement connecté
     * possède le rôle ETUDIANT.
     *
     * @param authentication contexte de sécurité
     * @return true si l'utilisateur est étudiant
     */
    private boolean isStudent(Authentication authentication) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ETUDIANT"));
    }


    /**
     * Récupère le StudentProfile de l'utilisateur connecté.
     *
     * Spring Security nous donne ici son email grâce à :
     *
     * User.getUsername()
     *
     * qui retourne l'email.
     *
     * @param authentication contexte de sécurité
     * @return profil étudiant connecté
     */
    private StudentProfile getCurrentStudentProfile(
            Authentication authentication) {

        /*
         * Authentication.getName() correspond au username
         * Spring Security.
         *
         * Dans notre User :
         *
         * getUsername() -> email
         */
        String email = authentication.getName();

        /*
         * Recherche de l'utilisateur en base.
         */
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur connecté introuvable"
                        ));

        /*
         * Vérification que l'utilisateur possède bien
         * un profil étudiant.
         */
        if (user.getStudentProfile() == null) {

            throw new RuntimeException(
                    "Aucun profil étudiant associé à cet utilisateur"
            );
        }

        return user.getStudentProfile();
    }


    /**
     * Récupère l'identifiant du profil étudiant connecté.
     *
     * @param authentication contexte de sécurité
     * @return ID du StudentProfile
     */
    private Long getCurrentStudentProfileId(
            Authentication authentication) {

        return getCurrentStudentProfile(authentication).getId();
    }


    /**
     * Vérifie qu'un StudentProfileId correspond
     * à l'utilisateur actuellement connecté.
     *
     * Cette méthode est utilisée lorsqu'un étudiant
     * appelle une URL contenant son studentProfileId.
     *
     * @param studentProfileId identifiant à vérifier
     * @param authentication utilisateur connecté
     * @return true si le profil appartient à l'utilisateur
     */
    private boolean isCurrentStudent(
            Long studentProfileId,
            Authentication authentication) {

        Long currentStudentProfileId =
                getCurrentStudentProfileId(authentication);

        return currentStudentProfileId.equals(studentProfileId);
    }


    /**
     * Vérifie qu'une StudentDemarche appartient
     * bien à l'utilisateur actuellement connecté.
     *
     * Cette vérification est particulièrement importante
     * pour les opérations :
     *
     * - GET
     * - PATCH
     * - DELETE
     *
     * afin qu'un étudiant ne puisse pas manipuler
     * le StudentDemarche d'un autre étudiant.
     *
     * @param studentDemarche démarche à vérifier
     * @param authentication utilisateur connecté
     * @return true si la démarche appartient à l'étudiant
     */
    private boolean belongsToCurrentStudent(
            StudentDemarche studentDemarche,
            Authentication authentication) {

        /*
         * Récupération du profil de l'utilisateur connecté.
         */
        Long currentStudentProfileId =
                getCurrentStudentProfileId(authentication);

        /*
         * Récupération du profil associé à la démarche.
         */
        Long ownerStudentProfileId =
                studentDemarche
                        .getStudentProfile()
                        .getId();

        /*
         * Comparaison des deux identifiants.
         */
        return currentStudentProfileId.equals(ownerStudentProfileId);
    }
}