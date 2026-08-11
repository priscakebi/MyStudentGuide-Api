package com.mystudentguide.entity;

import com.mystudentguide.enums.LogementType;
import com.mystudentguide.enums.VisaType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;

import java.util.List;

/**
 * Représente le profil administratif et étudiant d'un utilisateur.
 *
 * Cette entité contient les informations nécessaires à MyStudentGuide
 * pour personnaliser les démarches administratives proposées à l'étudiant.
 *
 * Exemple :
 * - nationalité
 * - date d'arrivée en France
 * - type de visa
 * - établissement
 * - logement
 *
 * Ces informations sont complétées après la création du compte,
 * via le questionnaire de personnalisation.
 */
@Entity
@Table(name = "student_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile {

    /**
     * Identifiant unique du profil.
     *
     * Il est généré automatiquement par la base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nationalité de l'étudiant.
     *
     * Exemple : Ivoirienne, Sénégalaise, Camerounaise...
     *
     * On utilise String pour le moment afin de garder
     * le système simple et flexible.
     */
    @Column(nullable = false, length = 100)
    private String nationalite;

    /**
     * Date d'arrivée de l'étudiant en France.
     *
     * On utilise LocalDate car nous avons uniquement
     * besoin de la date, et non de l'heure.
     */
    @Column(nullable = false)
    private LocalDate dateArriveeFrance;

    /**
     * Type de visa ou de titre de séjour.
     *
     * La valeur sera définie à partir de l'enum VisaType.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VisaType typeVisa;

    /**
     * Date d'expiration du visa ou du titre de séjour.
     *
     * Cette information peut être inconnue au moment
     * du remplissage du profil.
     */
    private LocalDate dateExpirationVisa;

    /**
     * Nom de l'établissement fréquenté par l'étudiant.
     *
     * Exemple : université, école, centre de formation...
     */
    @Column(length = 150)
    private String universite;

    /**
     * Niveau d'études.
     *
     * Exemples :
     * BTS, Licence, Master, Doctorat...
     */
    @Column(length = 100)
    private String niveauEtudes;

    /**
     * Ville dans laquelle l'étudiant étudie.
     */
    @Column(length = 100)
    private String villeEtudes;

    /**
     * Permet de savoir si l'étudiant est arrivé récemment
     * en France pour ses études.
     *
     * Cette information peut influencer les démarches proposées.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean premiereArrivee = false;

    /**
     * Type de logement de l'étudiant.
     *
     * Exemple :
     * - CROUS
     * - logement privé
     * - hébergement chez un proche
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private LogementType typeLogement;

    /**
     * Adresse actuelle de l'étudiant.
     *
     * Elle pourra être utilisée pour certaines démarches
     * administratives et pour personnaliser les informations.
     */
    @Column(length = 255)
    private String adresse;

    /**
     * Numéro de téléphone.
     *
     * Il est placé dans le profil et non dans User,
     * car il fait partie des informations personnelles
     * du profil étudiant.
     */
    @Column(length = 30)
    private String telephone;

    /**
     * Indique si l'étudiant a terminé le questionnaire
     * de personnalisation.
     *
     * false -> le profil doit encore être complété.
     * true -> le profil est suffisamment complet
     * pour générer les démarches.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean profileCompleted = false;

    /**
     * Relation OneToOne avec User.
     *
     * Un utilisateur possède un seul profil étudiant.
     *
     * Cette relation est le côté propriétaire de l'association.
     *
     * @JoinColumn crée la clé étrangère user_id
     *             dans la table student_profiles.
     *
     *             unique = true garantit qu'un utilisateur
     *             ne peut avoir qu'un seul profil.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "studentProfile")
    @Builder.Default
    private List<Demarche> demarches = new ArrayList<>();

}