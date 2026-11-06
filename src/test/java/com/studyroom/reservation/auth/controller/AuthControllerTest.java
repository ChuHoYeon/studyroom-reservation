package com.studyroom.reservation.auth.controller;

import com.studyroom.reservation.auth.security.SecurityConfig;
import com.studyroom.reservation.auth.service.EmailVerificationService;
import com.studyroom.reservation.common.exception.GlobalExceptionHandler;
import com.studyroom.reservation.common.exception.InvalidEmailVerificationTokenException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @Test
    void verifyEmail_allowsUnauthenticatedRequestAndReturnsNoContent() throws Exception {
        mockMvc.perform(get("/api/v1/auth/email/verify")
                        .param("token", "raw-token"))
                .andExpect(status().isNoContent());

        verify(emailVerificationService).verify("raw-token");
    }

    @Test
    void verifyEmail_returnsBadRequestForInvalidToken() throws Exception {
        doThrow(new InvalidEmailVerificationTokenException())
                .when(emailVerificationService)
                .verify("invalid-token");

        mockMvc.perform(get("/api/v1/auth/email/verify")
                        .param("token", "invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_EMAIL_VERIFICATION_TOKEN"))
                .andExpect(jsonPath("$.message")
                        .value("유효하지 않거나 만료된 이메일 인증 링크입니다."));
    }

    @Test
    void verifyEmail_returnsBadRequestWhenTokenParameterIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/auth/email/verify"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(emailVerificationService);
    }
}
