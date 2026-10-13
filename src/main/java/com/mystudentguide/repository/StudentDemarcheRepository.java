package com.mystudentguide.repository;

import com.mystudentguide.entity.StudentDemarche;
import com.mystudentguide.enums.DemarcheStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository permettant de gérer les StudentDemarche en base de données.
 *
 * StudentDemarche représente l'association entre :
 *
 *      StudentProfile
 *             ↓
 *      StudentDemarche
 *             ↓
 *          Demarche
 *
 * Ce repository permet donc de rechercher et de vérifier
 * les démarches suivies par les étudiants.
 */
public interface StudentDemarcheRepository
        extends JpaRepository<StudentDemarche, Long> {


    // ============================================================
    // DÉMARCHES D'UN ÉTUDIANT
    // ============================================================

    /**
     * Recherche toutes les démarches suivies par un étudiant.
     *
     * Le JOIN FETCH permet de charger immédiatement
     * la démarche générale associée.
     *
     * Cela évite les problèmes de LazyInitializationException
     * lorsque la démarche est utilisée après la fermeture
     * de la session Hibernate.
     *
     * @param studentProfileId identifiant du profil étudiant
     * @return liste des démarches suivies
     */
    @Query("""
        SELECT sd
        FROM StudentDemarche sd
        JOIN FETCH sd.demarche
        WHERE sd.studentProfile.id = :studentProfileId
    """)
    List<StudentDemarche> findByStudentProfileId(
            @Param("studentProfileId") Long studentProfileId
    );


    // ============================================================
    // DÉMARCHES D'UN ÉTUDIANT PAR STATUT
    // ============================================================

    /**
     * Recherche les démarches d'un étudiant
     * correspondant à un statut donné.
     *
     * Exemple :
     * récupérer uniquement les démarches EN_COURS.
     *
     * @param studentProfileId identifiant du profil étudiant
     * @param statut statut recherché
     * @return liste des démarches correspondant au statut
     */
    @Query("""
        SELECT sd
        FROM StudentDemarche sd
        JOIN FETCH sd.demarche
        WHERE sd.studentProfile.id = :studentProfileId
        AND sd.statut = :statut
    """)
    List<StudentDemarche> findByStudentProfileIdAndStatut(
            @Param("studentProfileId") Long studentProfileId,
            @Param("statut") DemarcheStatus statut
    );


    // ============================================================
    // DÉMARCHES SELON LE STATUT
    // ============================================================

    /**
     * Recherche toutes les démarches possédant
     * un statut donné.
     *
     * Cette méthode peut notamment être utilisée
     * par l'administration.
     *
     * @param statut statut recherché
     * @return liste des StudentDemarche concernées
     */
    @Query("""
        SELECT sd
        FROM StudentDemarche sd
        JOIN FETCH sd.demarche
        WHERE sd.statut = :statut
    """)
    List<StudentDemarche> findByStatut(
            @Param("statut") DemarcheStatus statut
    );


    // ============================================================
    // COMPTER LES DÉMARCHES D'UN ÉTUDIANT PAR STATUT
    // ============================================================

    /**
     * Compte le nombre de démarches d'un étudiant
     * ayant un statut donné.
     *
     * Cette requête effectue directement un COUNT en base.
     * Elle ne charge donc pas les objets StudentDemarche.
     *
     * @param studentProfileId identifiant du profil étudiant
     * @param statut statut recherché
     * @return nombre de démarches correspondant au statut
     */
    long countByStudentProfile_IdAndStatut(
            Long studentProfileId,
            DemarcheStatus statut
    );


    // ============================================================
    // DÉMARCHES SUIVIES PAR LES ÉTUDIANTS
    // ============================================================

    /**
     * Recherche tous les suivis correspondant
     * à une démarche générale.
     *
     * Exemple :
     *
     * retrouver tous les étudiants qui suivent
     * la démarche "Renouvellement du titre de séjour".
     *
     * @param demarcheId identifiant de la démarche
     * @return liste des StudentDemarche concernées
     */
    @Query("""
        SELECT sd
        FROM StudentDemarche sd
        JOIN FETCH sd.demarche
        WHERE sd.demarche.id = :demarcheId
    """)
    List<StudentDemarche> findByDemarcheId(
            @Param("demarcheId") Long demarcheId
    );

    /**
     * Charge un suivi avec sa démarche et son profil étudiant
     * (évite les erreurs de chargement paresseux lors de la
     * sérialisation JSON, open-in-view étant désactivé).
     */
    @Query("""
        SELECT sd
        FROM StudentDemarche sd
        JOIN FETCH sd.demarche
        JOIN FETCH sd.studentProfile sp
        LEFT JOIN FETCH sp.user
        WHERE sd.id = :id
    """)
    Optional<StudentDemarche> findByIdWithDetails(@Param("id") Long id);


    // ============================================================
    // VÉRIFIER SI LE COUPLE ÉTUDIANT / DÉMARCHE EXISTE
    // ============================================================

    /**
     * Vérifie si un étudiant suit déjà une démarche donnée.
     *
     * Cette méthode est importante pour empêcher la création
     * d'un doublon.
     *
     * Elle correspond à la contrainte d'unicité définie
     * dans StudentDemarche :
     *
     *     student_profile_id + demarche_id
     *
     * Exemple :
     *
     * Alice + Visa étudiant → existe déjà → true
     * Alice + Logement      → n'existe pas → false
     *
     * @param studentProfileId identifiant du profil étudiant
     * @param demarcheId identifiant de la démarche
     * @return true si l'association existe déjà
     */
    boolean existsByStudentProfile_IdAndDemarche_Id(
            Long studentProfileId,
            Long demarcheId
    );


    // ============================================================
    // RECHERCHER UNE ASSOCIATION PRÉCISE
    // ============================================================

    /**
     * Recherche précisément le suivi d'une démarche
     * pour un étudiant donné.
     *
     * Le résultat est retourné sous forme de Optional
     * afin de gérer proprement le cas où l'association
     * n'existe pas.
     *
     * Le JOIN FETCH charge immédiatement la démarche générale.
     *
     * @param studentProfileId identifiant du profil étudiant
     * @param demarcheId identifiant de la démarche
     * @return Optional contenant le suivi s'il existe
     */
    @Query("""
        SELECT sd
        FROM StudentDemarche sd
        JOIN FETCH sd.demarche
        WHERE sd.studentProfile.id = :studentProfileId
        AND sd.demarche.id = :demarcheId
    """)
    Optional<StudentDemarche> findByStudentProfileIdAndDemarcheId(
            @Param("studentProfileId") Long studentProfileId,
            @Param("demarcheId") Long demarcheId
    );
}