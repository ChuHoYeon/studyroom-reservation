package com.studyroom.reservation.auth.service;

import com.studyroom.reservation.auth.entity.EmailVerificationToken;
import com.studyroom.reservation.auth.repository.EmailVerificationTokenRepository;
import com.studyroom.reservation.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class EmailVerificationService {

    @Value("${app.auth.email-verification-url}")
    private String emailVerificationUrl;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final VerificationTokenGenerator tokenGenerator;
    private final Clock clock;
    private final EmailSender emailSender;

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
}
