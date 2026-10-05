package com.mystudentguide.repository;

import com.mystudentguide.entity.DocumentRequis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository permettant d'accéder aux documents requis
 * enregistrés en base de données.
 *
 * Cette interface permet notamment de récupérer
 * les documents requis associés à une démarche.
 */
@Repository
public interface DocumentRequisRepository extends JpaRepository<DocumentRequis, Long> {

    /**
     * Recherche tous les documents requis associés
     * à une démarche donnée.
     *
     * La méthode utilise la propriété "demarche"
     * de l'entité DocumentRequis, puis son identifiant "id".
     *
     * Exemple :
     * si demarcheId = 2, la méthode récupère tous les
     * documents dont la démarche possède l'id 2.
     *
     * @param demarcheId identifiant de la démarche
     * @return liste des documents requis de cette démarche
     */
    List<DocumentRequis> findByDemarche_Id(Long demarcheId);
}