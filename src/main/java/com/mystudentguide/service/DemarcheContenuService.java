package com.mystudentguide.service;

import com.mystudentguide.dto.demarche.ContenuDtos.Contenu;
import com.mystudentguide.dto.demarche.ContenuDtos.EtapeRequest;
import com.mystudentguide.dto.demarche.ContenuDtos.LienRequest;
import com.mystudentguide.dto.demarche.EtapeDemarcheDTO;
import com.mystudentguide.dto.demarche.LienOfficielDTO;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.EtapeDemarche;
import com.mystudentguide.entity.LienOfficiel;
import com.mystudentguide.repository.DemarcheRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Édition des étapes et des liens officiels d'une démarche par l'admin.
 *
 * Avant, seules les fiches importées de Service-Public avaient des étapes
 * et des liens : une démarche saisie à la main restait vide.
 *
 * Les étapes sont numérotées 1, 2, 3... sans trou : la numérotation est
 * recalculée après chaque ajout, suppression ou réordonnancement.
 *
 * Toutes les opérations passent par la démarche (cascade + orphanRemoval)
 * et renvoient le contenu complet à jour.
 */
@Service
public class DemarcheContenuService {

    private final DemarcheRepository demarcheRepository;

    public DemarcheContenuService(DemarcheRepository demarcheRepository) {
        this.demarcheRepository = demarcheRepository;
    }

    @Transactional(readOnly = true)
    public Contenu lire(Long demarcheId) {
        return contenu(getDemarche(demarcheId));
    }


    // ============================================================
    // ÉTAPES
    // ============================================================

    @Transactional
    public Contenu ajouterEtape(Long demarcheId, EtapeRequest request) {
        Demarche demarche = getDemarche(demarcheId);

        int prochain = demarche.getEtapes().stream()
                .map(EtapeDemarche::getNumero)
                .filter(Objects::nonNull)
                .max(Integer::compare)
                .orElse(0) + 1;

        demarche.getEtapes().add(new EtapeDemarche(
                prochain, request.titre().trim(), nettoyer(request.description()), demarche));

        return sauver(demarche);
    }

    @Transactional
    public Contenu modifierEtape(Long demarcheId, Long etapeId, EtapeRequest request) {
        Demarche demarche = getDemarche(demarcheId);
        EtapeDemarche etape = trouverEtape(demarche, etapeId);

        etape.setTitre(request.titre().trim());
        etape.setDescription(nettoyer(request.description()));

        return sauver(demarche);
    }

    @Transactional
    public Contenu supprimerEtape(Long demarcheId, Long etapeId) {
        Demarche demarche = getDemarche(demarcheId);
        demarche.getEtapes().remove(trouverEtape(demarche, etapeId));
        renumeroter(demarche.getEtapes().stream()
                .sorted(Comparator.comparing(EtapeDemarche::getNumero, Comparator.nullsLast(Integer::compare)))
                .toList());
        return sauver(demarche);
    }

    /**
     * Réordonne les étapes.
     *
     * @param ids tous les identifiants des étapes, dans le nouvel ordre
     * @throws IllegalArgumentException si la liste ne correspond pas exactement aux étapes
     */
    @Transactional
    public Contenu reordonnerEtapes(Long demarcheId, List<Long> ids) {
        Demarche demarche = getDemarche(demarcheId);

        Set<Long> attendus = new HashSet<>(demarche.getEtapes().stream().map(EtapeDemarche::getId).toList());
        if (ids.size() != attendus.size() || !attendus.equals(new HashSet<>(ids))) {
            throw new IllegalArgumentException("L'ordre envoyé ne correspond pas aux étapes de la démarche.");
        }

        renumeroter(ids.stream().map(id -> trouverEtape(demarche, id)).toList());
        return sauver(demarche);
    }


    // ============================================================
    // LIENS OFFICIELS
    // ============================================================

    @Transactional
    public Contenu ajouterLien(Long demarcheId, LienRequest request) {
        Demarche demarche = getDemarche(demarcheId);
        demarche.getLiensOfficiels().add(new LienOfficiel(
                request.nom().trim(), request.url().trim(), nettoyer(request.description()), demarche));
        return sauver(demarche);
    }

    @Transactional
    public Contenu modifierLien(Long demarcheId, Long lienId, LienRequest request) {
        Demarche demarche = getDemarche(demarcheId);
        LienOfficiel lien = trouverLien(demarche, lienId);

        lien.setNom(request.nom().trim());
        lien.setUrl(request.url().trim());
        lien.setDescription(nettoyer(request.description()));

        return sauver(demarche);
    }

    @Transactional
    public Contenu supprimerLien(Long demarcheId, Long lienId) {
        Demarche demarche = getDemarche(demarcheId);
        demarche.getLiensOfficiels().remove(trouverLien(demarche, lienId));
        return sauver(demarche);
    }


    // ============================================================
    // OUTILS
    // ============================================================

    private Demarche getDemarche(Long id) {
        return demarcheRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Démarche introuvable avec l'id : " + id));
    }

    private static EtapeDemarche trouverEtape(Demarche demarche, Long etapeId) {
        return demarche.getEtapes().stream()
                .filter(e -> Objects.equals(e.getId(), etapeId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Étape introuvable dans cette démarche."));
    }

    private static LienOfficiel trouverLien(Demarche demarche, Long lienId) {
        return demarche.getLiensOfficiels().stream()
                .filter(l -> Objects.equals(l.getId(), lienId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Lien introuvable dans cette démarche."));
    }

    /** Numérote 1, 2, 3... dans l'ordre donné. */
    private static void renumeroter(List<EtapeDemarche> etapes) {
        for (int i = 0; i < etapes.size(); i++) {
            etapes.get(i).setNumero(i + 1);
        }
    }

    private static String nettoyer(String texte) {
        if (texte == null) {
            return null;
        }
        String t = texte.trim();
        return t.isEmpty() ? null : t;
    }

    /** Enregistre (les nouvelles lignes reçoivent leur identifiant) et renvoie le contenu à jour. */
    private Contenu sauver(Demarche demarche) {
        return contenu(demarcheRepository.saveAndFlush(demarche));
    }

    static Contenu contenu(Demarche demarche) {
        List<EtapeDemarcheDTO> etapes = demarche.getEtapes().stream()
                .sorted(Comparator.comparing(EtapeDemarche::getNumero, Comparator.nullsLast(Integer::compare)))
                .map(e -> EtapeDemarcheDTO.builder()
                        .id(e.getId())
                        .numero(e.getNumero())
                        .titre(e.getTitre())
                        .description(e.getDescription())
                        .build())
                .toList();

        List<LienOfficielDTO> liens = demarche.getLiensOfficiels().stream()
                .sorted(Comparator.comparing(LienOfficiel::getId, Comparator.nullsLast(Long::compare)))
                .map(l -> LienOfficielDTO.builder()
                        .id(l.getId())
                        .nom(l.getNom())
                        .url(l.getUrl())
                        .description(l.getDescription())
                        .build())
                .toList();

        return new Contenu(etapes, liens);
    }
}
