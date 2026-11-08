package com.studyroom.reservation.auth.integration;

import com.studyroom.reservation.auth.entity.EmailVerificationToken;
import com.studyroom.reservation.auth.service.EmailSender;
import com.studyroom.reservation.auth.service.VerificationTokenGenerator;
import com.studyroom.reservation.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class EmailVerificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private VerificationTokenGenerator tokenGenerator;

    @Autowired
    private Clock clock;

    @MockitoBean
    private EmailSender emailSender;

    @Test
    void verifyEmail_updatesUserAndTokenAndRejectsTokenReuse() throws Exception {
        String rawToken = "verification-" + UUID.randomUUID();
        User user = User.create(
                "verify-" + UUID.randomUUID() + "@example.com",
                "encoded-password",
                "홍길동"
        );
        entityManager.persist(user);

        EmailVerificationToken token = EmailVerificationToken.issue(
                user,
                tokenGenerator.hash(rawToken),
                LocalDateTime.now(clock).plusMinutes(30)
        );
        entityManager.persist(token);
        entityManager.flush();

        long userId = user.getId();
        long tokenId = token.getId();
        entityManager.clear();

        mockMvc.perform(get("/api/v1/auth/email/verify")
                        .param("token", rawToken))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        User verifiedUser = entityManager.find(User.class, userId);
        EmailVerificationToken usedToken =
                entityManager.find(EmailVerificationToken.class, tokenId);
        assertThat(verifiedUser.isEmailVerified()).isTrue();
        assertThat(usedToken.getUsedAt()).isNotNull();

        entityManager.clear();

        mockMvc.perform(get("/api/v1/auth/email/verify")
                        .param("token", rawToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_EMAIL_VERIFICATION_TOKEN"));
    }

    @Test
    void resendVerificationEmail_invalidatesAllPreviousTokensAndIssuesNewToken() throws Exception {
        String email = "resend-" + UUID.randomUUID() + "@example.com";
        User user = User.create(
                email,
                "encoded-password",
                "홍길동"
        );
        entityManager.persist(user);

        LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(30);
        EmailVerificationToken firstToken = EmailVerificationToken.issue(
                user,
                tokenGenerator.hash("first-old-token"),
                expiresAt
        );
        EmailVerificationToken secondToken = EmailVerificationToken.issue(
                user,
                tokenGenerator.hash("second-old-token"),
                expiresAt
        );
        entityManager.persist(firstToken);
        entityManager.persist(secondToken);
        entityManager.flush();

        long userId = user.getId();
        long firstTokenId = firstToken.getId();
        long secondTokenId = secondToken.getId();
        entityManager.clear();

        mockMvc.perform(post("/api/v1/auth/email/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s"
                                }
                                """.formatted(email)))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        EmailVerificationToken reloadedFirstToken =
                entityManager.find(EmailVerificationToken.class, firstTokenId);
        EmailVerificationToken reloadedSecondToken =
                entityManager.find(EmailVerificationToken.class, secondTokenId);
        assertThat(reloadedFirstToken.getInvalidatedAt()).isNotNull();
        assertThat(reloadedSecondToken.getInvalidatedAt()).isNotNull();

        List<EmailVerificationToken> activeTokens = entityManager.createQuery(
                        """
                        select t from EmailVerificationToken t
                        where t.user.id = :userId
                          and t.usedAt is null
                          and t.invalidatedAt is null
                        """,
                        EmailVerificationToken.class
                )
                .setParameter("userId", userId)
                .getResultList();
        assertThat(activeTokens).hasSize(1);

        org.mockito.ArgumentCaptor<String> verificationUrlCaptor =
                org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendVerificationEmail(
                eq(email),
                verificationUrlCaptor.capture()
        );
        String verificationUrl = verificationUrlCaptor.getValue();
        String urlPrefix =
                "http://localhost:8080/api/v1/auth/email/verify?token=";
        assertThat(verificationUrl).startsWith(urlPrefix);
        String newRawToken = verificationUrl.substring(urlPrefix.length());
        assertThat(activeTokens.getFirst().getTokenHash())
                .isEqualTo(tokenGenerator.hash(newRawToken));
    }
}
