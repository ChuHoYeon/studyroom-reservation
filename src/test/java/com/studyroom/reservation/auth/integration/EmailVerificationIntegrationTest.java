package com.studyroom.reservation.auth.integration;

import com.studyroom.reservation.auth.entity.EmailVerificationToken;
import com.studyroom.reservation.auth.service.VerificationTokenGenerator;
import com.studyroom.reservation.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
