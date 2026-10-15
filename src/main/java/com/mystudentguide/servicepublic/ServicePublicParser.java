package com.mystudentguide.servicepublic;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transforme le XML d'une fiche Service-Public (flux 3.5 de la DILA)
 * en {@link FicheServicePublic}.
 *
 * Correspondance :
 * - dc:title                         → titre
 * - dc:description + Introduction    → description
 * - Chapitre (titre + texte)         → étapes
 * - ServiceEnLigne, PourEnSavoirPlus,
 *   OuSAdresser (RessourceWeb)       → liens officiels
 * - dateDerniereModificationImportante → date de mise à jour
 *
 * Les pièces justificatives ne sont PAS extraites : elles sont
 * réparties dans des sections au format variable, et une liste
 * approximative serait trompeuse. Elles restent gérées par l'admin.
 *
 * Sécurité : le XML vient d'un serveur externe, l'analyseur
 * refuse les DOCTYPE et les entités externes (attaques XXE).
 */
public class ServicePublicParser {

    static final int ETAPES_MAX = 20;
    static final int LIENS_MAX = 15;
    static final int DESCRIPTION_ETAPE_MAX = 2000;

    /** Balises après lesquelles on passe à la ligne dans le texte extrait. */
    private static final Set<String> BLOCS =
            Set.of("Paragraphe", "Item", "Titre", "Rang", "Cellule");

    /** Blocs exclus du texte : ils sont importés à part comme liens officiels. */
    private static final Set<String> EXCLUS =
            Set.of("ServiceEnLigne", "PourEnSavoirPlus", "OuSAdresser", "Reference", "RessourceWeb");

    private static final Pattern DATE = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");

    public FicheServicePublic parse(String xml) {

        Document doc = lireXmlSecurise(xml);
        Element racine = doc.getDocumentElement();

        String id = racine.getAttribute("ID");
        String titre = texteDe(premier(racine, "dc:title"));

        // ----------------------------------------------------------
        // Description : résumé (dc:description) + introduction
        // ----------------------------------------------------------

        String resume = texteDe(premier(racine, "dc:description"));
        String intro = texteDe(premier(racine, "Introduction"));
        String description = intro.isBlank() || intro.equals(resume)
                ? resume
                : (resume.isBlank() ? intro : resume + "\n\n" + intro);

        // ----------------------------------------------------------
        // Date de mise à jour
        // ----------------------------------------------------------

        LocalDate dateMaj = extraireDate(racine.getAttribute("dateDerniereModificationImportante"));
        if (dateMaj == null) {
            dateMaj = extraireDate(texteDe(premier(racine, "dc:date")));
        }

        // ----------------------------------------------------------
        // Étapes : un chapitre = une étape (sans doublon de titre)
        // ----------------------------------------------------------

        Map<String, FicheServicePublic.Etape> etapes = new LinkedHashMap<>();
        NodeList chapitres = racine.getElementsByTagName("Chapitre");

        for (int i = 0; i < chapitres.getLength() && etapes.size() < ETAPES_MAX; i++) {
            Element chapitre = (Element) chapitres.item(i);
            Element titreChapitre = enfantDirect(chapitre, "Titre");
            String titreEtape = texteDe(titreChapitre);

            if (titreEtape.isBlank() || etapes.containsKey(titreEtape)) {
                continue;
            }

            String texte = texteSans(chapitre, titreChapitre);
            etapes.put(titreEtape, new FicheServicePublic.Etape(
                    tronquer(titreEtape, 150),
                    tronquer(texte, DESCRIPTION_ETAPE_MAX)));
        }

        // ----------------------------------------------------------
        // Liens officiels
        // ----------------------------------------------------------

        Map<String, FicheServicePublic.Lien> liens = new LinkedHashMap<>();
        ajouterLiens(racine, "ServiceEnLigne", liens);
        ajouterLiens(racine, "PourEnSavoirPlus", liens);
        ajouterLiensOuSAdresser(racine, liens);

        // Thème officiel (ex. : « Étranger - Europe ») pour suggérer une catégorie.
        Element theme = premier(racine, "Theme");
        String themeOfficiel = theme == null ? "" : texteDe(enfantDirect(theme, "Titre"));

        return new FicheServicePublic(
                id,
                titre,
                description,
                racine.getAttribute("spUrl"),
                dateMaj,
                themeOfficiel,
                new ArrayList<>(etapes.values()),
                new ArrayList<>(liens.values()).subList(0, Math.min(liens.size(), LIENS_MAX))
        );
    }

    // ==============================================================
    // LIENS
    // ==============================================================

    private void ajouterLiens(Element racine, String balise, Map<String, FicheServicePublic.Lien> liens) {
        NodeList noeuds = racine.getElementsByTagName(balise);

        for (int i = 0; i < noeuds.getLength(); i++) {
            Element e = (Element) noeuds.item(i);
            String url = e.getAttribute("URL");
            String nom = texteDe(enfantDirect(e, "Titre"));
            String source = texteDe(enfantDirect(e, "Source"));
            String type = e.getAttribute("type");

            String description = String.join(" · ",
                    List.of(type, source).stream().filter(s -> !s.isBlank()).toList());

            ajouterLien(liens, nom, url, description);
        }
    }

    /** « Où s'adresser » : on garde le lien de l'annuaire (ex. : Préfecture). */
    private void ajouterLiensOuSAdresser(Element racine, Map<String, FicheServicePublic.Lien> liens) {
        NodeList noeuds = racine.getElementsByTagName("OuSAdresser");

        for (int i = 0; i < noeuds.getLength(); i++) {
            Element e = (Element) noeuds.item(i);
            Element ressource = enfantDirect(e, "RessourceWeb");

            if (ressource != null) {
                ajouterLien(liens,
                        "Où s'adresser : " + texteDe(enfantDirect(e, "Titre")),
                        ressource.getAttribute("URL"),
                        texteDe(enfantDirect(e, "Source")));
            }
        }
    }

    private void ajouterLien(Map<String, FicheServicePublic.Lien> liens, String nom, String url, String description) {
        // Uniquement des adresses web, sans doublon, et compatibles avec la base.
        if (nom.isBlank()
                || url == null
                || !(url.startsWith("https://") || url.startsWith("http://"))
                || url.length() > 500) {
            return;
        }
        liens.putIfAbsent(nom + "|" + url,
                new FicheServicePublic.Lien(tronquer(nom, 150), url, description));
    }

    // ==============================================================
    // OUTILS XML
    // ==============================================================

    static Document lireXmlSecurise(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xml)));

        } catch (Exception e) {
            throw new IllegalArgumentException("Le contenu reçu n'est pas une fiche Service-Public valide.", e);
        }
    }

    private static Element premier(Element racine, String balise) {
        NodeList noeuds = racine.getElementsByTagName(balise);
        return noeuds.getLength() > 0 ? (Element) noeuds.item(0) : null;
    }

    private static Element enfantDirect(Element parent, String balise) {
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element e && e.getTagName().equals(balise)) {
                return e;
            }
        }
        return null;
    }

    /** Texte d'un élément, avec des retours à la ligne entre les paragraphes. */
    static String texteDe(Node noeud) {
        if (noeud == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        collecter(noeud, null, sb);
        return nettoyer(sb.toString());
    }

    /** Texte d'un élément sans l'un de ses enfants (ex. : le titre du chapitre). */
    private static String texteSans(Element parent, Node exclu) {
        StringBuilder sb = new StringBuilder();
        collecter(parent, exclu, sb);
        return nettoyer(sb.toString());
    }

    private static void collecter(Node noeud, Node exclu, StringBuilder sb) {
        if (noeud == exclu
                || (noeud instanceof Element el && EXCLUS.contains(el.getTagName()))) {
            return;
        }
        if (noeud.getNodeType() == Node.TEXT_NODE || noeud.getNodeType() == Node.CDATA_SECTION_NODE) {
            sb.append(noeud.getNodeValue());
            return;
        }
        for (Node n = noeud.getFirstChild(); n != null; n = n.getNextSibling()) {
            collecter(n, exclu, sb);
        }
        if (noeud instanceof Element e && BLOCS.contains(e.getTagName())) {
            sb.append('\n');
        }
    }

    /**
     * Espaces multiples réduits, lignes vides supprimées, et lignes
     * identiques consécutives fusionnées (les fiches répètent parfois
     * le titre d'un « cas » juste avant son contenu).
     */
    static String nettoyer(String texte) {
        List<String> lignes = new ArrayList<>();

        texte.lines()
                .map(l -> l.replaceAll("[\\s\\u00A0]+", " ").trim())
                .filter(l -> !l.isEmpty())
                .forEach(l -> {
                    if (lignes.isEmpty() || !lignes.get(lignes.size() - 1).equals(l)) {
                        lignes.add(l);
                    }
                });

        return String.join("\n", lignes);
    }

    static String tronquer(String texte, int max) {
        return texte.length() <= max ? texte : texte.substring(0, max - 1).trim() + "…";
    }

    private static LocalDate extraireDate(String valeur) {
        if (valeur == null) {
            return null;
        }
        Matcher m = DATE.matcher(valeur);
        return m.find() ? LocalDate.parse(m.group(1)) : null;
    }
}
