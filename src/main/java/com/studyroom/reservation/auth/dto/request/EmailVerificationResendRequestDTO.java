package com.studyroom.reservation.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Locale;

public record EmailVerificationResendRequestDTO(
        @NotBlank
        @Email
        String email
) {
    public EmailVerificationResendRequestDTO {
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }
}
