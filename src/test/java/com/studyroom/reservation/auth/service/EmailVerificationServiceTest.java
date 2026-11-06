package com.studyroom.reservation.auth.service;

import com.studyroom.reservation.auth.entity.EmailVerificationToken;
import com.studyroom.reservation.auth.repository.EmailVerificationTokenRepository;
import com.studyroom.reservation.common.exception.InvalidEmailVerificationTokenException;
import com.studyroom.reservation.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    private static final String VERIFICATION_URL =
            "http://localhost:8080/api/v1/auth/email/verify";
    private static final String RAW_TOKEN = "raw-token";
    private static final String TOKEN_HASH = "a".repeat(64);

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private VerificationTokenGenerator tokenGenerator;

    @Mock
    private EmailSender emailSender;

    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-11-04T01:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
        emailVerificationService = new EmailVerificationService(
                tokenRepository,
                tokenGenerator,
                fixedClock,
                emailSender
        );
        ReflectionTestUtils.setField(
                emailVerificationService,
                "emailVerificationUrl",
                VERIFICATION_URL
        );
    }

    @Test
    void issueAndSend_savesHashedTokenAndSendsVerificationEmail() {
        User user = User.create(
                "user@example.com",
                "encoded-password",
                "홍길동"
        );
        when(tokenGenerator.generateRawToken()).thenReturn(RAW_TOKEN);
        when(tokenGenerator.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);

        emailVerificationService.issueAndSend(user);

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        EmailVerificationToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUser()).isSameAs(user);
        assertThat(savedToken.getTokenHash())
                .isEqualTo(TOKEN_HASH)
                .isNotEqualTo(RAW_TOKEN);
        assertThat(savedToken.getExpiresAt())
                .isEqualTo(LocalDateTime.of(2026, 11, 4, 10, 30));
        assertThat(savedToken.getUsedAt()).isNull();
        assertThat(savedToken.getInvalidatedAt()).isNull();

        verify(tokenGenerator).hash(RAW_TOKEN);
        verify(emailSender).sendVerificationEmail(
                "user@example.com",
                VERIFICATION_URL + "?token=" + RAW_TOKEN
        );

        InOrder saveThenSend = inOrder(tokenRepository, emailSender);
        saveThenSend.verify(tokenRepository).save(savedToken);
        saveThenSend.verify(emailSender).sendVerificationEmail(
                "user@example.com",
                VERIFICATION_URL + "?token=" + RAW_TOKEN
        );
    }

    @Test
    void issueAndSend_propagatesMailException() {
        User user = User.create(
                "user@example.com",
                "encoded-password",
                "홍길동"
        );
        MailSendException mailException = new MailSendException("메일 발송 실패");
        when(tokenGenerator.generateRawToken()).thenReturn(RAW_TOKEN);
        when(tokenGenerator.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);
        doThrow(mailException)
                .when(emailSender)
                .sendVerificationEmail(any(String.class), any(String.class));

        assertThatThrownBy(() -> emailVerificationService.issueAndSend(user))
                .isSameAs(mailException);

        verify(tokenRepository).save(any(EmailVerificationToken.class));
    }

    @Test
    void verify_marksTokenUsedAndVerifiesUser() {
        User user = User.create(
                "user@example.com",
                "encoded-password",
                "홍길동"
        );
        EmailVerificationToken token = EmailVerificationToken.issue(
                user,
                TOKEN_HASH,
                LocalDateTime.of(2026, 11, 4, 10, 30)
        );
        when(tokenGenerator.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);
        when(tokenRepository.findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(token));

        emailVerificationService.verify(RAW_TOKEN);

        assertThat(token.getUsedAt())
                .isEqualTo(LocalDateTime.of(2026, 11, 4, 10, 0));
        assertThat(user.isEmailVerified()).isTrue();
        verify(tokenGenerator).hash(RAW_TOKEN);
        verify(tokenRepository).findByTokenHash(TOKEN_HASH);
    }

    @Test
    void verify_rejectsNullOrBlankTokenBeforeHashing() {
        for (String invalidToken : new String[]{null, "", "   "}) {
            assertThatThrownBy(() -> emailVerificationService.verify(invalidToken))
                    .isInstanceOf(InvalidEmailVerificationTokenException.class)
                    .hasMessage("유효하지 않거나 만료된 이메일 인증 링크입니다.");
        }

        verifyNoInteractions(tokenGenerator, tokenRepository);
    }

    @Test
    void verify_rejectsUnknownTokenHash() {
        when(tokenGenerator.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);
        when(tokenRepository.findByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailVerificationService.verify(RAW_TOKEN))
                .isInstanceOf(InvalidEmailVerificationTokenException.class)
                .hasMessage("유효하지 않거나 만료된 이메일 인증 링크입니다.");

        verify(tokenGenerator).hash(RAW_TOKEN);
        verify(tokenRepository).findByTokenHash(TOKEN_HASH);
    }
}
