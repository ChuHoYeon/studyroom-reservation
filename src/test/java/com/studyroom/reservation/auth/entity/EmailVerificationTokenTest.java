package com.studyroom.reservation.auth.entity;

import com.studyroom.reservation.common.exception.InvalidEmailVerificationTokenException;
import com.studyroom.reservation.user.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailVerificationTokenTest {

    private static final String TOKEN_HASH = "a".repeat(64);
    private static final LocalDateTime EXPIRES_AT =
            LocalDateTime.of(2026, 11, 4, 10, 30);

    @Test
    void markUsed_recordsUsedAtBeforeExpiration() {
        EmailVerificationToken token = createToken();
        LocalDateTime verifiedAt = EXPIRES_AT.minusSeconds(1);

        token.markUsed(verifiedAt);

        assertThat(token.getUsedAt()).isEqualTo(verifiedAt);
    }

    @Test
    void markUsed_rejectsTokenAtExpirationBoundary() {
        EmailVerificationToken token = createToken();

        assertThatThrownBy(() -> token.markUsed(EXPIRES_AT))
                .isInstanceOf(InvalidEmailVerificationTokenException.class);

        assertThat(token.getUsedAt()).isNull();
    }

    @Test
    void markUsed_rejectsAlreadyUsedToken() {
        EmailVerificationToken token = createToken();
        token.markUsed(EXPIRES_AT.minusMinutes(1));

        assertThatThrownBy(() -> token.markUsed(EXPIRES_AT.minusSeconds(1)))
                .isInstanceOf(InvalidEmailVerificationTokenException.class);
    }

    @Test
    void markUsed_rejectsInvalidatedToken() {
        EmailVerificationToken token = createToken();
        token.invalidate(EXPIRES_AT.minusMinutes(1));

        assertThatThrownBy(() -> token.markUsed(EXPIRES_AT.minusSeconds(1)))
                .isInstanceOf(InvalidEmailVerificationTokenException.class);

        assertThat(token.getUsedAt()).isNull();
    }

    private EmailVerificationToken createToken() {
        User user = User.create(
                "user@example.com",
                "encoded-password",
                "홍길동"
        );
        return EmailVerificationToken.issue(user, TOKEN_HASH, EXPIRES_AT);
    }
}
