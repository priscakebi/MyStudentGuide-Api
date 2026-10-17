package com.mystudentguide.service;

import com.mystudentguide.entity.Notification;
import com.mystudentguide.entity.StudentProfile;
import com.mystudentguide.entity.User;
import com.mystudentguide.enums.NotificationType;
import com.mystudentguide.enums.RoleType;
import com.mystudentguide.repository.StudentProfileRepository;
import com.mystudentguide.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SuggestionAutoService suggestionAutoService;

    /**
     * Constructeur du service.
     *
     * @param studentProfileRepository permet de gérer les profils étudiants
     * @param userRepository permet de récupérer les utilisateurs
     * @param notificationService permet de créer les notifications
     * @param suggestionAutoService suggestions de démarches d'après le profil
     */
    public StudentProfileService(
            StudentProfileRepository studentProfileRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            SuggestionAutoService suggestionAutoService) {

        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.suggestionAutoService = suggestionAutoService;
    }

    /**
     * Récupère tous les profils étudiants.
     *
     * @return la liste de tous les profils
     */
    public List<StudentProfile> getAllProfiles() {
        return studentProfileRepository.findAll();
    }

    /**
     * Recherche un profil étudiant à partir de son identifiant.
     *
     * @param id identifiant du profil
     * @return le profil s'il existe
     */
    public Optional<StudentProfile> getProfileById(Long id) {
        return studentProfileRepository.findById(id);
    }

    /**
     * Recherche un profil à partir de l'identifiant de l'utilisateur.
     *
     * @param userId identifiant de l'utilisateur
     * @return le profil s'il existe
     */
    public Optional<StudentProfile> getProfileByUserId(Long userId) {
        return studentProfileRepository.findByUserId(userId);
    }

    /**
     * Recherche un profil à partir de l'adresse email de l'utilisateur.
     *
     * @param email adresse email de l'utilisateur
     * @return le profil s'il existe
     */
    public Optional<StudentProfile> getProfileByEmail(String email) {
        return studentProfileRepository.findByUserEmail(email);
    }

    /**
     * Même recherche que getProfileByEmail().
     *
     * Cette méthode est conservée car elle est peut-être utilisée
     * ailleurs dans l'application.
     *
     * @param email adresse email de l'utilisateur
     * @return le profil s'il existe
     */
    public Optional<StudentProfile> getProfileByUserEmail(String email) {
        return studentProfileRepository.findByUserEmail(email);
    }

    /**
     * Vérifie si un profil existe déjà pour un utilisateur.
     *
     * @param userId identifiant de l'utilisateur
     * @return true si le profil existe, sinon false
     */
    public boolean profileExistsForUser(Long userId) {
        return studentProfileRepository.existsByUserId(userId);
    }

    /**
     * Crée ou modifie le profil d'un étudiant.
     *
     * La méthode récupère d'abord l'utilisateur connecté grâce
     * à son adresse email, puis associe le profil à cet utilisateur.
     *
     * Si le profil n'a pas encore d'identifiant, il s'agit d'une création.
     * Si le profil possède déjà un identifiant, il s'agit d'une modification.
     *
     * Une notification est ensuite envoyée à tous les administrateurs.
     *
     * @param studentProfile profil à enregistrer
     * @param email email de l'utilisateur connecté
     * @return le profil enregistré
     */
    @Transactional
    public StudentProfile saveProfile(
            StudentProfile studentProfile,
            String email) {

        // Recherche de l'utilisateur correspondant à l'email fourni.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur non trouvé : " + email
                        )
                );

        // On vérifie si le profil existe déjà avant de sauvegarder.
        boolean profileAlreadyExists = studentProfile.getId() != null;

        // Démarches suivies : jamais envoyées par le formulaire (@JsonIgnore), elles
        // arrivent VIDES. Sans cette ligne, l'enregistrement remplaçait la liste par
        // une liste vide et orphanRemoval supprimait toutes les démarches commencées.
        if (profileAlreadyExists) {
            studentProfileRepository.findById(studentProfile.getId())
                    .ifPresent(existant ->
                            studentProfile.setStudentDemarches(existant.getStudentDemarches()));
        }

        // Association obligatoire entre le profil et son utilisateur.
        studentProfile.setUser(user);

        // Synchronisation du côté User.
        user.setStudentProfile(studentProfile);

        // Sauvegarde du profil dans la base de données.
        StudentProfile savedProfile =
                studentProfileRepository.save(studentProfile);

        // Récupération de tous les administrateurs.
        List<User> admins = userRepository.findByRole(RoleType.ADMIN);

        // Création d'une notification pour chaque administrateur.
        for (User admin : admins) {

            Notification notification;

            // Si le profil existe déjà, l'étudiant vient de le modifier.
            if (profileAlreadyExists) {

                notification = Notification.builder()
                        .titre("Profil étudiant modifié")
                        .message(
                                "L'étudiant "
                                        + user.getPrenom()
                                        + " "
                                        + user.getNom()
                                        + " vient de modifier son profil."
                        )
                        .type(NotificationType.SYSTEME)
                        .lu(false)
                        .user(admin)
                        .build();

            } else {

                // Sinon, il s'agit de la création du profil.
                notification = Notification.builder()
                        .titre("Nouveau profil étudiant créé")
                        .message(
                                "L'étudiant "
                                        + user.getPrenom()
                                        + " "
                                        + user.getNom()
                                        + " vient de créer son profil."
                        )
                        .type(NotificationType.SYSTEME)
                        .lu(false)
                        .user(admin)
                        .build();
            }

            // Enregistrement de la notification.
            notificationService.saveNotification(notification);
        }

        // Démarches qui correspondent au profil : suggestions automatiques
        // (jamais deux fois la même, voir SuggestionAutoService).
        suggestionAutoService.suggererPourProfil(savedProfile);

        // Retourne le profil sauvegardé.
        return savedProfile;
    }

    /**
     * Supprime un profil étudiant.
     *
     * @param id identifiant du profil à supprimer
     */
    public void deleteProfile(Long id) {
        studentProfileRepository.deleteById(id);
    }
}