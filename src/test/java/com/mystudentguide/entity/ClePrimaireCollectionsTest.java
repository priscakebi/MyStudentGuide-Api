package com.mystudentguide.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Les tables des collections (@ElementCollection) doivent avoir une clé
 * primaire : la base de production (MySQL avec sql_require_primary_key)
 * refuse sinon de les créer, et toute lecture de l'entité échoue
 * (« Table 'demarche_cible_visas' doesn't exist »).
 *
 * Pour un Set, Hibernate crée la clé primaire (clé étrangère + valeur)
 * seulement si la colonne de la valeur est NOT NULL.
 */
class ClePrimaireCollectionsTest {

    private static final List<Class<?>> ENTITES = List.of(
            Avis.class, Categorie.class, DemandeAide.class, Demarche.class, DocumentRequis.class,
            EtapeDemarche.class, LienOfficiel.class, Notification.class, PasswordResetToken.class,
            SignalementDemarche.class, StudentDemarche.class, StudentProfile.class,
            SuggestionEnvoyee.class, User.class
    );

    @Test
    void chaqueCollectionEstUnSetAvecUneColonneObligatoire() {
        List<Field> collections = ENTITES.stream()
                .flatMap(entite -> Arrays.stream(entite.getDeclaredFields()))
                .filter(champ -> champ.isAnnotationPresent(ElementCollection.class))
                .toList();

        assertThat(collections).isNotEmpty();

        for (Field champ : collections) {
            String nom = champ.getDeclaringClass().getSimpleName() + "." + champ.getName();

            assertThat(Set.class.isAssignableFrom(champ.getType()))
                    .as("%s doit être un Set (une List n'a pas de clé primaire)", nom)
                    .isTrue();

            Column colonne = champ.getAnnotation(Column.class);
            assertThat(colonne != null && !colonne.nullable())
                    .as("%s doit avoir @Column(nullable = false) pour que sa table ait une clé primaire", nom)
                    .isTrue();
        }
    }
}
