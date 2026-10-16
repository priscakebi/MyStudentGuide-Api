package com.mystudentguide.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mystudentguide.enums.DemarcheStatus;
import com.mystudentguide.service.EcheancePersonnelle;
import jakarta.persistence.*;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant le suivi d'une démarche pour un étudiant.
 *
 * Une démarche générale est définie dans l'entité Demarche.
 * Cette entité permet de suivre cette démarche pour un étudiant précis.
 *
 * IMPORTANT :
 * Un même étudiant ne peut suivre qu'une seule fois
 * une même démarche.
 *
 * Cette règle est garantie :
 * - au niveau de l'application par le service ;
 * - au niveau de la base de données par une contrainte d'unicité.
 */
@Entity
@Table(
    name = "student_demarches",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_student_demarche",
            columnNames = {
                "student_profile_id",
                "demarche_id"
            }
        )
    }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StudentDemarche {

    /**
     * Identifiant unique du suivi.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Profil étudiant concerné.
     *
     * Un étudiant peut suivre plusieurs démarches différentes.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_profile_id", nullable = false)
    private StudentProfile studentProfile;

    /**
     * Démarche générale suivie.
     *
     * Une même démarche peut être suivie par plusieurs étudiants.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demarche_id", nullable = false)
    private Demarche demarche;

    /**
     * Statut de la démarche pour cet étudiant.
     *
     * Par défaut, une nouvelle démarche est "A_FAIRE".
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DemarcheStatus statut = DemarcheStatus.A_FAIRE;

    /**
     * Date à laquelle la démarche a été attribuée
     * à l'étudiant.
     */
    @Column(name = "date_attribution")
    private LocalDate dateAttribution;

    /**
     * Date à laquelle la démarche a été terminée.
     *
     * Peut rester null tant que la démarche n'est pas terminée.
     */
    @Column(name = "date_terminaison")
    private LocalDate dateTerminaison;

    /**
     * Date du dernier rappel envoyé.
     *
     * Cette information pourra être utilisée par le système
     * de notification pour éviter des rappels trop fréquents.
     */
    @Column(name = "dernier_rappel")
    private LocalDate dernierRappel;

    /**
     * Commentaire associé au suivi.
     */
    @Column(columnDefinition = "TEXT")
    private String commentaire;


    /**
     * Constructeur vide obligatoire pour JPA.
     */
    /**
     * Date à laquelle l'étudiant a cliqué sur « Commencer »
     * (passage au statut EN_COURS).
     *
     * Sert de point de départ au rappel automatique de J+10
     * demandant si les documents ont été envoyés.
     */
    @Column(name = "date_debut")
    private LocalDate dateDebut;

    /**
     * Réponse de l'étudiant à la question
     * « Avez-vous envoyé vos documents sur la plateforme dédiée ? ».
     *
     * null / false : pas encore confirmé ; true : documents envoyés.
     * (Colonne nullable : compatible avec les lignes déjà en base.)
     */
    @Column(name = "documents_envoyes")
    private Boolean documentsEnvoyes;

    /**
     * Date à laquelle l'étudiant a confirmé l'envoi des documents.
     */
    @Column(name = "date_envoi_documents")
    private LocalDate dateEnvoiDocuments;

    /**
     * Vrai lorsque l'étudiant a déjà donné son avis
     * sur le suivi de cette démarche (un seul avis par démarche).
     */
    @Column(name = "avis_donne")
    private Boolean avisDonne;

    /**
     * Documents requis que l'étudiant a cochés comme prêts
     * (identifiants de DocumentRequis) : sa checklist personnelle.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "student_demarche_documents_prets",
        joinColumns = @JoinColumn(name = "student_demarche_id")
    )
    @Column(name = "document_requis_id", nullable = false)
    private Set<Long> documentsPrets = new HashSet<>();

    public StudentDemarche() {
    }


    /**
     * Initialise automatiquement les valeurs par défaut
     * avant l'enregistrement en base.
     */
    @PrePersist
    protected void onCreate() {

        /*
         * Si aucune date d'attribution n'est définie,
         * on utilise la date du jour.
         */
        if (dateAttribution == null) {
            dateAttribution = LocalDate.now();
        }

        /*
         * Si aucun statut n'est défini,
         * la démarche commence avec A_FAIRE.
         */
        if (statut == null) {
            statut = DemarcheStatus.A_FAIRE;
        }
    }


    // ============================================================
    // GETTERS ET SETTERS
    // ============================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StudentProfile getStudentProfile() {
        return studentProfile;
    }

    public void setStudentProfile(StudentProfile studentProfile) {
        this.studentProfile = studentProfile;
    }

    public Demarche getDemarche() {
        return demarche;
    }

    public void setDemarche(Demarche demarche) {
        this.demarche = demarche;
    }

    public DemarcheStatus getStatut() {
        return statut;
    }

    public void setStatut(DemarcheStatus statut) {
        this.statut = statut;
    }

    public LocalDate getDateAttribution() {
        return dateAttribution;
    }

    public void setDateAttribution(LocalDate dateAttribution) {
        this.dateAttribution = dateAttribution;
    }

    public LocalDate getDateTerminaison() {
        return dateTerminaison;
    }

    public void setDateTerminaison(LocalDate dateTerminaison) {
        this.dateTerminaison = dateTerminaison;
    }

    public LocalDate getDernierRappel() {
        return dernierRappel;
    }

    public void setDernierRappel(LocalDate dernierRappel) {
        this.dernierRappel = dernierRappel;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public boolean isDocumentsEnvoyes() {
        return Boolean.TRUE.equals(documentsEnvoyes);
    }

    public void setDocumentsEnvoyes(Boolean documentsEnvoyes) {
        this.documentsEnvoyes = documentsEnvoyes;
    }

    public LocalDate getDateEnvoiDocuments() {
        return dateEnvoiDocuments;
    }

    public void setDateEnvoiDocuments(LocalDate dateEnvoiDocuments) {
        this.dateEnvoiDocuments = dateEnvoiDocuments;
    }

    public boolean isAvisDonne() {
        return Boolean.TRUE.equals(avisDonne);
    }

    public void setAvisDonne(Boolean avisDonne) {
        this.avisDonne = avisDonne;
    }

    public Set<Long> getDocumentsPrets() {
        return documentsPrets;
    }

    public void setDocumentsPrets(Set<Long> documentsPrets) {
        this.documentsPrets = documentsPrets;
    }

    /**
     * Date limite de cette démarche pour CET étudiant (renvoyée dans le JSON),
     * calculée par EcheancePersonnelle à partir de son profil (date d'arrivée,
     * fin du titre de séjour) ; à défaut, la date limite générale.
     *
     * null si le profil ou la démarche n'ont pas été chargés avec le suivi
     * (open-in-view est désactivé) : le front retombe alors sur la date
     * limite générale. GET /api/student-demarches/me les charge toujours.
     */
    @JsonProperty("dateLimitePersonnelle")
    public LocalDate getDateLimitePersonnelle() {
        if (!Hibernate.isInitialized(demarche) || !Hibernate.isInitialized(studentProfile)) {
            return null;
        }
        return EcheancePersonnelle.calculer(demarche, studentProfile);
    }

    /**
     * Progression de la démarche, en pourcentage (renvoyée dans le JSON).
     *
     * - A_FAIRE                         →   0 %
     * - EN_COURS (clic sur Commencer)   →  25 %
     * - EN_COURS + documents envoyés    →  50 %
     * - TERMINEE                        → 100 %
     * - NON_CONCERNEE                   →   0 % (exclue du calcul global côté front)
     */
    public int getProgression() {
        if (statut == null) {
            return 0;
        }
        switch (statut) {
            case EN_COURS:
                return isDocumentsEnvoyes() ? 50 : 25;
            case TERMINEE:
                return 100;
            default:
                return 0;
        }
    }
}