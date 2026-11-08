package com.studyroom.reservation.auth.controller;

import com.studyroom.reservation.auth.dto.request.EmailVerificationResendRequestDTO;
import com.studyroom.reservation.auth.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {

    private final EmailVerificationService emailVerificationService;

    @GetMapping("/email/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@RequestParam("token") String rawToken) {
        emailVerificationService.verify(rawToken);
    }

    @PostMapping("/email/resend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendVerificationEmail(@Valid @RequestBody EmailVerificationResendRequestDTO request) {
        emailVerificationService.resend(request.email());
    }
}
