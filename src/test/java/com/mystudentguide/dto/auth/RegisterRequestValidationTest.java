package com.mystudentguide.dto.auth;

import com.mystudentguide.validation.ConditionsUtilisation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Consentement RGPD à l'inscription : la case « J'accepte les conditions
 * d'utilisation et la politique de confidentialité » est obligatoire.
 */
class RegisterRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private RegisterRequest requete(Boolean accepte) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("prisca@email.com");
        request.setPassword("MotDePasse12!");
        request.setPrenom("Prisca");
        request.setNom("Kouenin");
        request.setAccepteConditions(accepte);
        return request;
    }

    private Set<String> messages(RegisterRequest request) {
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        return violations.stream().map(ConstraintViolation::getMessage).collect(java.util.stream.Collectors.toSet());
    }

    @Test
    void inscriptionAcceptee_quandLesConditionsSontAcceptees() {
        assertThat(messages(requete(true))).isEmpty();
    }

    @Test
    void inscriptionRefusee_sansAccord() {
        assertThat(messages(requete(false))).containsExactly(ConditionsUtilisation.MESSAGE_REFUS);
    }

    @Test
    void inscriptionRefusee_siLaCaseEstAbsente() {
        assertThat(messages(requete(null))).containsExactly(ConditionsUtilisation.MESSAGE_REFUS);
    }
}
