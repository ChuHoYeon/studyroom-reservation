package com.studyroom.reservation.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record SignUpRequestDTO(

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8, max = 64)
        String password,

        @NotBlank
        @Size(min = 1, max = 50)
        String name

) {
    public SignUpRequestDTO {
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }

        if (name != null) {
            name = name.trim();
        }
    }
}