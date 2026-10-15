package com.mystudentguide.servicepublic;

import com.mystudentguide.dto.demarche.CiblageDTO;
import com.mystudentguide.enums.LogementType;

import java.util.List;
import java.util.Optional;

import static com.mystudentguide.dto.demarche.CiblageDTO.AUCUN;
import static com.mystudentguide.dto.demarche.CiblageDTO.actif;

/**
 * Fiches Service-Public utiles aux étudiants internationaux,
 * proposées dans le panneau d'import de l'administration.
 *
 * Chaque fiche porte un ciblage par défaut pour les suggestions
 * automatiques (à qui la suggérer d'après le profil). L'admin peut le
 * modifier après l'import ; il ne sert qu'une fois la démarche publiée.
 * Les fiches trop spécifiques (bourses, recours...) ne sont pas
 * suggérées automatiquement.
 *
 * L'admin peut aussi importer n'importe quelle autre fiche
 * en saisissant son identifiant (visible dans l'adresse de la
 * fiche : service-public.gouv.fr/particuliers/vosdroits/F2231).
 */
public final class CatalogueEtudiant {

    private CatalogueEtudiant() {
    }

    public record FicheSuggeree(String id, String titre, String theme, CiblageDTO ciblage) {
    }

    public static final List<FicheSuggeree> FICHES = List.of(
            new FicheSuggeree("F2231", "Étudiant étranger en France : visa de long séjour ou titre de séjour", "Séjour & visa",
                    actif().pourHorsUE()),
            new FicheSuggeree("F15763", "Qu'est-ce qu'un récépissé de demande de titre de séjour ?", "Séjour & visa", AUCUN),
            new FicheSuggeree("F31969", "Que faire en cas de difficulté lors d'une demande de titre de séjour ?", "Séjour & visa", AUCUN),
            new FicheSuggeree("F2861", "Études supérieures : première inscription d'un étudiant", "Études",
                    actif().pourPremiereArrivee()),
            new FicheSuggeree("F12214", "Étudiant : bourse sur critères sociaux", "Études", AUCUN),
            new FicheSuggeree("F12216", "DSE (dossier social étudiant) : comment demander une bourse et/ou un logement ?", "Études", AUCUN),
            new FicheSuggeree("F32456", "Quelles aides peut percevoir un étudiant ?", "Études",
                    actif().pourPremiereArrivee()),
            new FicheSuggeree("F31912", "Comment demander un logement étudiant au Crous ?", "Logement",
                    actif().pourLogements(LogementType.SANS_LOGEMENT)),
            new FicheSuggeree("F12006", "Aide personnalisée au logement (APL)", "Logement",
                    actif().pourLogements(LogementType.LOCATION_PRIVEE, LogementType.RESIDENCE_ETUDIANTE, LogementType.CROUS)),
            new FicheSuggeree("F1563", "Un étudiant peut-il bénéficier d'une aide au logement pour payer son loyer ?", "Logement", AUCUN),
            new FicheSuggeree("F265", "Carte Vitale", "Santé",
                    actif().pourPremiereArrivee()),
            new FicheSuggeree("F10027", "Complémentaire santé solidaire (ex-CMU-C)", "Santé", AUCUN),
            new FicheSuggeree("F2413", "Compte bancaire individuel", "Banque & budget",
                    actif().pourPremiereArrivee()),
            new FicheSuggeree("F2417", "Refus d'ouverture de compte bancaire : droit au compte", "Banque & budget", AUCUN),
            new FicheSuggeree("F2713", "Un étudiant non européen peut-il travailler en France ?", "Emploi",
                    actif().pourHorsUE()),
            new FicheSuggeree("F2229", "Étudiant étranger : comment travailler en France après ses études ?", "Emploi", AUCUN),
            new FicheSuggeree("F34964", "Transports : quelles réductions pour les jeunes, étudiants et apprentis ?", "Transports",
                    actif().pourPremiereArrivee())
    );

    /** Fiche du catalogue par identifiant. */
    public static Optional<FicheSuggeree> trouver(String ficheId) {
        return FICHES.stream().filter(f -> f.id().equals(ficheId)).findFirst();
    }
}
