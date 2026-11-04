package com.studyroom.reservation.user.controller;

import com.studyroom.reservation.auth.security.SecurityConfig;
import com.studyroom.reservation.common.exception.DuplicateEmailException;
import com.studyroom.reservation.common.exception.GlobalExceptionHandler;
import com.studyroom.reservation.user.dto.request.SignUpRequestDTO;
import com.studyroom.reservation.user.dto.response.SignUpResponseDTO;
import com.studyroom.reservation.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void signUp_allowsUnauthenticatedRequestAndReturnsCreatedResponse() throws Exception {
        SignUpResponseDTO response = SignUpResponseDTO.builder()
                .id(1L)
                .email("user@example.com")
                .name("홍길동")
                .emailVerified(false)
                .build();
        when(userService.signUp(any(SignUpRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "  USER@EXAMPLE.COM  ",
                                  "password": "password123",
                                  "name": "  홍길동  "
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.emailVerified").value(false))
                .andExpect(jsonPath("$.password").doesNotExist());

        ArgumentCaptor<SignUpRequestDTO> requestCaptor =
                ArgumentCaptor.forClass(SignUpRequestDTO.class);
        verify(userService).signUp(requestCaptor.capture());
        assertThat(requestCaptor.getValue().email()).isEqualTo("user@example.com");
        assertThat(requestCaptor.getValue().password()).isEqualTo("password123");
        assertThat(requestCaptor.getValue().name()).isEqualTo("홍길동");
    }

    @Test
    void signUp_returnsConflictForDuplicateEmail() throws Exception {
        when(userService.signUp(any(SignUpRequestDTO.class)))
                .thenThrow(new DuplicateEmailException());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123",
                                  "name": "홍길동"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    void signUp_returnsBadRequestForInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email",
                                  "password": "password123",
                                  "name": "홍길동"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signUp(any(SignUpRequestDTO.class));
    }

    @Test
    void signUp_returnsBadRequestForShortPassword() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "short",
                                  "name": "홍길동"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signUp(any(SignUpRequestDTO.class));
    }

    @Test
    void signUp_returnsBadRequestForNameBlankAfterNormalization() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123",
                                  "name": "   "
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signUp(any(SignUpRequestDTO.class));
    }
}
