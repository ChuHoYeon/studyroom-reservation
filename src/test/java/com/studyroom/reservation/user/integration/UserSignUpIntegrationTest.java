package com.studyroom.reservation.user.integration;

import com.studyroom.reservation.auth.entity.EmailVerificationToken;
import com.studyroom.reservation.auth.service.EmailSender;
import com.studyroom.reservation.auth.service.VerificationTokenGenerator;
import com.studyroom.reservation.user.entity.User;
import com.studyroom.reservation.user.entity.UserRole;
import com.studyroom.reservation.user.entity.UserStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserSignUpIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationTokenGenerator tokenGenerator;

    @MockitoBean
    private EmailSender emailSender;

    @Test
    void signUp_persistsNormalizedUserWithEncodedPassword() throws Exception {
        String email = uniqueEmail();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "  %s  ",
                                  "password": "password123",
                                  "name": "  홍길동  "
                                }
                                """.formatted(email.toUpperCase(Locale.ROOT))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.emailVerified").value(false))
                .andExpect(jsonPath("$.password").doesNotExist());

        User savedUser = entityManager.createQuery(
                        "select u from User u where u.email = :email",
                        User.class
                )
                .setParameter("email", email)
                .getSingleResult();

        assertThat(savedUser.getName()).isEqualTo("홍길동");
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
        assertThat(savedUser.getPassword()).isNotEqualTo("password123");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedUser.isEmailVerified()).isFalse();

        EmailVerificationToken savedToken = entityManager.createQuery(
                        "select t from EmailVerificationToken t where t.user = :user",
                        EmailVerificationToken.class
                )
                .setParameter("user", savedUser)
                .getSingleResult();

        ArgumentCaptor<String> verificationUrlCaptor =
                ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendVerificationEmail(
                org.mockito.ArgumentMatchers.eq(email),
                verificationUrlCaptor.capture()
        );

        String verificationUrl = verificationUrlCaptor.getValue();
        String urlPrefix =
                "http://localhost:8080/api/v1/auth/email/verify?token=";
        assertThat(verificationUrl).startsWith(urlPrefix);

        String rawToken = verificationUrl.substring(urlPrefix.length());
        assertThat(savedToken.getUser()).isSameAs(savedUser);
        assertThat(savedToken.getTokenHash())
                .isEqualTo(tokenGenerator.hash(rawToken))
                .isNotEqualTo(rawToken);
        assertThat(savedToken.getUsedAt()).isNull();
        assertThat(savedToken.getInvalidatedAt()).isNull();
    }

    @Test
    void signUp_returnsConflictWithoutCreatingAnotherUserForDuplicateEmail() throws Exception {
        String email = uniqueEmail();
        User existingUser = User.create(
                email,
                passwordEncoder.encode("password123"),
                "기존회원"
        );
        entityManager.persist(existingUser);
        entityManager.flush();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "  %s  ",
                                  "password": "different-password",
                                  "name": "신규회원"
                                }
                                """.formatted(email.toUpperCase(Locale.ROOT))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));

        long userCount = entityManager.createQuery(
                        "select count(u) from User u where u.email = :email",
                        Long.class
                )
                .setParameter("email", email)
                .getSingleResult();

        assertThat(userCount).isEqualTo(1L);
        verifyNoInteractions(emailSender);
    }

    private String uniqueEmail() {
        return "signup-" + UUID.randomUUID() + "@example.com";
    }
}
