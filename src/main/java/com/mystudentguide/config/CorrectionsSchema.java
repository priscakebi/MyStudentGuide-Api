package com.mystudentguide.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Corrige au démarrage des contraintes laissées par d'anciennes versions
 * des entités (MySQL).
 *
 * spring.jpa.hibernate.ddl-auto=update ajoute les nouvelles colonnes mais
 * ne rend jamais une colonne facultative : une colonne déclarée
 * « nullable = false » autrefois, puis rendue facultative (ou supprimée de
 * l'entité), reste NOT NULL en base et fait échouer chaque insertion.
 *
 * Exemple : un étudiant sans profil qui commence une démarche reçoit un
 * profil vide ; « nationalite », « type_visa » et l'ancienne colonne
 * « date_arrivee_france » (remplacée par date_arrivee) le refusaient,
 * d'où une erreur 500.
 *
 * Chaque colonne n'est modifiée que si elle existe et est encore NOT NULL :
 * sans effet aux démarrages suivants.
 */
@Component
@Order(0)
public class CorrectionsSchema implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CorrectionsSchema.class);

    /** Colonnes (table, colonne) qui doivent accepter NULL. */
    static final List<String[]> COLONNES_FACULTATIVES = List.of(
            new String[] {"student_profiles", "nationalite"},
            new String[] {"student_profiles", "type_visa"},
            new String[] {"student_profiles", "date_arrivee_france"}
    );

    private final JdbcTemplate jdbcTemplate;

    public CorrectionsSchema(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        for (String[] colonne : COLONNES_FACULTATIVES) {
            rendreFacultative(colonne[0], colonne[1]);
        }
    }

    private void rendreFacultative(String table, String colonne) {
        try {
            List<String> types = jdbcTemplate.queryForList(
                    "SELECT COLUMN_TYPE FROM information_schema.COLUMNS"
                            + " WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?"
                            + " AND IS_NULLABLE = 'NO'",
                    String.class, table, colonne);

            if (types.isEmpty()) {
                return;
            }

            // Noms fixes (liste ci-dessus) et type relu en base : pas de saisie extérieure.
            jdbcTemplate.execute("ALTER TABLE `" + table + "` MODIFY `" + colonne + "` " + types.get(0) + " NULL");
            log.info("Colonne {}.{} rendue facultative (contrainte d'une ancienne version).", table, colonne);
        } catch (DataAccessException e) {
            log.warn("Impossible de rendre facultative la colonne {}.{} : {}", table, colonne, e.getMessage());
        }
    }
}
