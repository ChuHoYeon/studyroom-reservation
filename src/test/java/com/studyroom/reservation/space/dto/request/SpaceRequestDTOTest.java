package com.studyroom.reservation.space.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SpaceRequestDTOTest {

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
    void constructor_normalizesName() {
        SpaceRequestDTO request = validRequest("  스터디룸 A  ");

        assertThat(request.name()).isEqualTo("스터디룸 A");
    }

    @Test
    void validation_acceptsValidRequest() {
        Set<ConstraintViolation<SpaceRequestDTO>> violations =
                validator.validate(validRequest("스터디룸 A"));

        assertThat(violations).isEmpty();
    }

    @Test
    void validation_rejectsBlankNameAndMissingRequiredValues() {
        SpaceRequestDTO request = new SpaceRequestDTO(
                "   ",
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<SpaceRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "capacity", "openTime", "closeTime");
    }

    @Test
    void validation_rejectsCapacityLessThanOne() {
        SpaceRequestDTO request = new SpaceRequestDTO(
                "스터디룸 A",
                null,
                0,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0)
        );

        Set<ConstraintViolation<SpaceRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("capacity");
    }

    @Test
    void validation_rejectsInvalidOperatingHours() {
        SpaceRequestDTO request = new SpaceRequestDTO(
                "스터디룸 A",
                null,
                4,
                LocalTime.of(22, 0),
                LocalTime.of(9, 0)
        );

        Set<ConstraintViolation<SpaceRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("operatingHoursValid");
    }

    private SpaceRequestDTO validRequest(String name) {
        return new SpaceRequestDTO(
                name,
                "화이트보드가 있는 공간",
                4,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0)
        );
    }
}
