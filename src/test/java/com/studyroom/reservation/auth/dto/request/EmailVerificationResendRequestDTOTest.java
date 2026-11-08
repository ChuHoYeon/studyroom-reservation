package com.studyroom.reservation.auth.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EmailVerificationResendRequestDTOTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void constructor_normalizesEmail() {
        EmailVerificationResendRequestDTO request =
                new EmailVerificationResendRequestDTO("  USER@EXAMPLE.COM  ");

        assertThat(request.email()).isEqualTo("user@example.com");
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void validation_rejectsBlankOrInvalidEmail() {
        EmailVerificationResendRequestDTO blankRequest =
                new EmailVerificationResendRequestDTO("   ");
        EmailVerificationResendRequestDTO invalidRequest =
                new EmailVerificationResendRequestDTO("invalid-email");

        Set<ConstraintViolation<EmailVerificationResendRequestDTO>> blankViolations =
                validator.validate(blankRequest);
        Set<ConstraintViolation<EmailVerificationResendRequestDTO>> invalidViolations =
                validator.validate(invalidRequest);

        assertThat(blankViolations).isNotEmpty();
        assertThat(invalidViolations).isNotEmpty();
    }
}
