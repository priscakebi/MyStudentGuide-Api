package com.mystudentguide.service;

import com.mystudentguide.dto.demarche.TraductionDtos.Element;
import com.mystudentguide.dto.demarche.TraductionDtos.Traduction;
import com.mystudentguide.entity.Demarche;
import com.mystudentguide.entity.DocumentRequis;
import com.mystudentguide.entity.EtapeDemarche;
import com.mystudentguide.entity.LienOfficiel;
import com.mystudentguide.repository.DemarcheRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Version anglaise du contenu d'une démarche (titre, description,
 * étapes, documents requis, liens officiels), saisie par l'admin.
 *
 * Les étudiants qui utilisent l'interface en anglais voient la
 * traduction quand elle existe, sinon le texte français.
 */
@Service
public class DemarcheTraductionService {

    private final DemarcheRepository demarcheRepository;

    public DemarcheTraductionService(DemarcheRepository demarcheRepository) {
        this.demarcheRepository = demarcheRepository;
    }

    @Transactional(readOnly = true)
    public Traduction lire(Long demarcheId) {
        return traduction(getDemarche(demarcheId));
    }

    /**
     * Enregistre les traductions reçues. Les éléments inconnus (supprimés
     * entre-temps) sont ignorés ; un élément absent garde sa traduction.
     */
    @Transactional
    public Traduction enregistrer(Long demarcheId, Traduction traduction) {
        Demarche demarche = getDemarche(demarcheId);

        if (traduction.demarche() != null) {
            demarche.setTitreEn(nettoyer(traduction.demarche().titre()));
            demarche.setDescriptionEn(nettoyer(traduction.demarche().description()));
        }

        appliquer(traduction.etapes(), demarche.getEtapes(), EtapeDemarche::getId, (etape, e) -> {
            etape.setTitreEn(nettoyer(e.titre()));
            etape.setDescriptionEn(nettoyer(e.description()));
        });
        appliquer(traduction.documents(), demarche.getDocumentsRequis(), DocumentRequis::getId, (document, e) -> {
            document.setNomEn(nettoyer(e.titre()));
            document.setDescriptionEn(nettoyer(e.description()));
        });
        appliquer(traduction.liens(), demarche.getLiensOfficiels(), LienOfficiel::getId, (lien, e) -> {
            lien.setNomEn(nettoyer(e.titre()));
            lien.setDescriptionEn(nettoyer(e.description()));
        });

        return traduction(demarcheRepository.saveAndFlush(demarche));
    }

    private static <T> void appliquer(
            List<Element> recus,
            List<T> existants,
            Function<T, Long> id,
            BiConsumer<T, Element> modifier
    ) {
        if (recus == null) {
            return;
        }
        Map<Long, T> parId = existants.stream()
                .filter(x -> id.apply(x) != null)
                .collect(Collectors.toMap(id, Function.identity()));
        for (Element element : recus) {
            T existant = element == null || element.id() == null ? null : parId.get(element.id());
            if (existant != null) {
                modifier.accept(existant, element);
            }
        }
    }

    static Traduction traduction(Demarche demarche) {
        Element principal = new Element(null, demarche.getTitre(), demarche.getDescription(),
                demarche.getTitreEn(), demarche.getDescriptionEn());

        List<Element> etapes = demarche.getEtapes().stream()
                .sorted(Comparator.comparing(EtapeDemarche::getNumero, Comparator.nullsLast(Integer::compare)))
                .map(e -> new Element(e.getId(), e.getTitre(), e.getDescription(), e.getTitreEn(), e.getDescriptionEn()))
                .toList();

        List<Element> documents = demarche.getDocumentsRequis().stream()
                .sorted(Comparator.comparing(DocumentRequis::getId, Comparator.nullsLast(Long::compare)))
                .map(d -> new Element(d.getId(), d.getNom(), d.getDescription(), d.getNomEn(), d.getDescriptionEn()))
                .toList();

        List<Element> liens = demarche.getLiensOfficiels().stream()
                .sorted(Comparator.comparing(LienOfficiel::getId, Comparator.nullsLast(Long::compare)))
                .map(l -> new Element(l.getId(), l.getNom(), l.getDescription(), l.getNomEn(), l.getDescriptionEn()))
                .toList();

        return new Traduction(principal, etapes, documents, liens);
    }

    private Demarche getDemarche(Long id) {
        return demarcheRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Démarche introuvable avec l'id : " + id));
    }

    private static String nettoyer(String texte) {
        if (texte == null) {
            return null;
        }
        String t = texte.trim();
        return t.isEmpty() ? null : t;
    }
}
