package com.mystudentguide.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorrectionsSchemaTest {

    @Mock private JdbcTemplate jdbcTemplate;

    @InjectMocks private CorrectionsSchema corrections;

    @Test
    void rendFacultativesLesColonnesEncoreObligatoires() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), anyString(), anyString())).thenReturn(List.of());
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), eq("student_profiles"), eq("nationalite")))
                .thenReturn(List.of("varchar(255)"));
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), eq("student_profiles"), eq("type_visa")))
                .thenReturn(List.of("enum('AUTRE','VLS_TS_ETUDIANT')"));

        corrections.run();

        verify(jdbcTemplate).execute("ALTER TABLE `student_profiles` MODIFY `nationalite` varchar(255) NULL");
        verify(jdbcTemplate).execute("ALTER TABLE `student_profiles` MODIFY `type_visa` enum('AUTRE','VLS_TS_ETUDIANT') NULL");
        verify(jdbcTemplate, never()).execute(contains("date_arrivee_france"));
    }

    @Test
    void neFaitRienQuandToutEstDejaCorrige() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), anyString(), anyString())).thenReturn(List.of());

        corrections.run();

        verify(jdbcTemplate, never()).execute(anyString());
    }

    @Test
    void uneErreurNEmpechePasLeDemarrage() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), anyString(), anyString()))
                .thenThrow(new DataAccessResourceFailureException("base indisponible"));

        corrections.run();

        verify(jdbcTemplate, times(CorrectionsSchema.COLONNES_FACULTATIVES.size()))
                .queryForList(anyString(), eq(String.class), anyString(), anyString());
        verify(jdbcTemplate, never()).execute(anyString());
    }
}
