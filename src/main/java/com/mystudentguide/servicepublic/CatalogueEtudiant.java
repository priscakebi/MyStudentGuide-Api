package com.mystudentguide.servicepublic;

import java.util.List;

/**
 * Fiches Service-Public utiles aux étudiants internationaux,
 * proposées dans le panneau d'import de l'administration.
 *
 * L'admin peut aussi importer n'importe quelle autre fiche
 * en saisissant son identifiant (visible dans l'adresse de la
 * fiche : service-public.gouv.fr/particuliers/vosdroits/F2231).
 */
public final class CatalogueEtudiant {

    private CatalogueEtudiant() {
    }

    public record FicheSuggeree(String id, String titre, String theme) {
    }

    public static final List<FicheSuggeree> FICHES = List.of(
            new FicheSuggeree("F2231", "Étudiant étranger en France : visa de long séjour ou titre de séjour", "Séjour & visa"),
            new FicheSuggeree("F15763", "Qu'est-ce qu'un récépissé de demande de titre de séjour ?", "Séjour & visa"),
            new FicheSuggeree("F31969", "Que faire en cas de difficulté lors d'une demande de titre de séjour ?", "Séjour & visa"),
            new FicheSuggeree("F2861", "Études supérieures : première inscription d'un étudiant", "Études"),
            new FicheSuggeree("F12214", "Étudiant : bourse sur critères sociaux", "Études"),
            new FicheSuggeree("F12216", "DSE (dossier social étudiant) : comment demander une bourse et/ou un logement ?", "Études"),
            new FicheSuggeree("F32456", "Quelles aides peut percevoir un étudiant ?", "Études"),
            new FicheSuggeree("F31912", "Comment demander un logement étudiant au Crous ?", "Logement"),
            new FicheSuggeree("F12006", "Aide personnalisée au logement (APL)", "Logement"),
            new FicheSuggeree("F1563", "Un étudiant peut-il bénéficier d'une aide au logement pour payer son loyer ?", "Logement"),
            new FicheSuggeree("F265", "Carte Vitale", "Santé"),
            new FicheSuggeree("F10027", "Complémentaire santé solidaire (ex-CMU-C)", "Santé"),
            new FicheSuggeree("F2413", "Compte bancaire individuel", "Banque & budget"),
            new FicheSuggeree("F2417", "Refus d'ouverture de compte bancaire : droit au compte", "Banque & budget"),
            new FicheSuggeree("F2713", "Un étudiant non européen peut-il travailler en France ?", "Emploi"),
            new FicheSuggeree("F2229", "Étudiant étranger : comment travailler en France après ses études ?", "Emploi"),
            new FicheSuggeree("F34964", "Transports : quelles réductions pour les jeunes, étudiants et apprentis ?", "Transports")
    );
}
