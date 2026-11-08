package com.studyroom.reservation.auth.service;

import com.studyroom.reservation.auth.entity.EmailVerificationToken;
import com.studyroom.reservation.auth.repository.EmailVerificationTokenRepository;
import com.studyroom.reservation.common.exception.InvalidEmailVerificationTokenException;
import com.studyroom.reservation.user.entity.User;
import com.studyroom.reservation.user.entity.UserStatus;
import com.studyroom.reservation.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class EmailVerificationService {

    @Value("${app.auth.email-verification-url}")
    private String emailVerificationUrl;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final VerificationTokenGenerator tokenGenerator;
    private final Clock clock;
    private final EmailSender emailSender;
    private final UserRepository userRepository;

    @Transactional
    public void issueAndSend(User user) {
        LocalDateTime issuedAt = LocalDateTime.now(clock);
        LocalDateTime expiresAt = issuedAt.plusMinutes(30);

        String rawToken = tokenGenerator.generateRawToken();
        String tokenHash = tokenGenerator.hash(rawToken);

        EmailVerificationToken emailVerificationToken = EmailVerificationToken.issue(user, tokenHash, expiresAt);
        emailVerificationTokenRepository.save(emailVerificationToken);

        String verificationUrl = emailVerificationUrl + "?token=" + rawToken;
        emailSender.sendVerificationEmail(user.getEmail(), verificationUrl);
    }

    @Transactional
    public void verify(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidEmailVerificationTokenException();
        }

        String tokenHash = tokenGenerator.hash(rawToken);

        EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(InvalidEmailVerificationTokenException::new);

        LocalDateTime now = LocalDateTime.now(clock);
        token.markUsed(now);

        token.getUser().verifyEmail();
    }

    @Transactional
    public void resend(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || user.isEmailVerified() || user.getStatus() == UserStatus.INACTIVE) {
            return;
        }

        LocalDateTime invalidatedAt = LocalDateTime.now(clock);

        List<EmailVerificationToken> tokens = emailVerificationTokenRepository.findAllByUserAndUsedAtIsNullAndInvalidatedAtIsNull(user);

        tokens.forEach(token -> token.invalidate(invalidatedAt));

        issueAndSend(user);
    }
}
