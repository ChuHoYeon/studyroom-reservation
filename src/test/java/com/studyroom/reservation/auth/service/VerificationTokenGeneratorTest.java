package com.studyroom.reservation.auth.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerificationTokenGeneratorTest {

    private final VerificationTokenGenerator tokenGenerator =
            new VerificationTokenGenerator();

    @Test
    void generateRawToken_returnsUrlSafeTokenWithoutPadding() {
        String rawToken = tokenGenerator.generateRawToken();

        assertThat(rawToken)
                .hasSize(43)
                .matches("[A-Za-z0-9_-]{43}")
                .doesNotContain("=");
    }

    @Test
    void generateRawToken_returnsDifferentTokens() {
        String firstToken = tokenGenerator.generateRawToken();
        String secondToken = tokenGenerator.generateRawToken();

        assertThat(firstToken).isNotEqualTo(secondToken);
    }

    @Test
    void hash_returnsExpectedSha256Hex() {
        String tokenHash = tokenGenerator.hash("test");

        assertThat(tokenHash)
                .isEqualTo(
                        "9f86d081884c7d659a2feaa0c55ad015"
                                + "a3bf4f1b2b0b822cd15d6c15b0f00a08"
                )
                .hasSize(64)
                .matches("[0-9a-f]{64}");
    }

    @Test
    void hash_returnsSameHashForSameTokenAndDifferentHashForDifferentToken() {
        String firstHash = tokenGenerator.hash("raw-token");
        String sameTokenHash = tokenGenerator.hash("raw-token");
        String differentTokenHash = tokenGenerator.hash("different-token");

        assertThat(firstHash).isEqualTo(sameTokenHash);
        assertThat(firstHash).isNotEqualTo(differentTokenHash);
    }

    @Test
    void hash_rejectsNullOrBlankToken() {
        assertThatThrownBy(() -> tokenGenerator.hash(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("원문 토큰이 필요합니다.");
        assertThatThrownBy(() -> tokenGenerator.hash(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("원문 토큰이 필요합니다.");
        assertThatThrownBy(() -> tokenGenerator.hash("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("원문 토큰이 필요합니다.");
    }
}
