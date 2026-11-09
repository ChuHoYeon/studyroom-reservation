package com.studyroom.reservation.space.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalTime;

public record SpaceRequestDTO(
        @NotBlank
        @Size(max = 100)
        String name,

        String description,

        @NotNull
        @Min(1)
        Integer capacity,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime openTime,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime closeTime
) {
    public SpaceRequestDTO {
        if (name != null) {
            name = name.trim();
        }
    }

    @AssertTrue
    public boolean isOperatingHoursValid() {
        return openTime == null || closeTime == null || openTime.isBefore(closeTime);
    }
}
