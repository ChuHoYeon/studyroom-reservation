package com.studyroom.reservation.user.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SignUpRequestDTOTest {

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
    void constructor_normalizesEmailAndName_withoutChangingPassword() {
        String password = "  password123  ";

        SignUpRequestDTO request = new SignUpRequestDTO(
                "  USER@EXAMPLE.COM  ",
                password,
                "  홍길동  "
        );

        assertThat(request.email()).isEqualTo("user@example.com");
        assertThat(request.password()).isEqualTo(password);
        assertThat(request.name()).isEqualTo("홍길동");
    }

    @Test
    void validation_acceptsValuesThatAreValidAfterNormalization() {
        SignUpRequestDTO request = new SignUpRequestDTO(
                "  USER@EXAMPLE.COM  ",
                "password123",
                "  홍길동  "
        );

        Set<ConstraintViolation<SignUpRequestDTO>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void validation_rejectsEmailAndNameThatAreBlankAfterNormalization() {
        SignUpRequestDTO request = new SignUpRequestDTO(
                "   ",
                "password123",
                "   "
        );

        Set<ConstraintViolation<SignUpRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("email", "name");
    }
}
