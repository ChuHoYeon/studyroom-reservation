package com.studyroom.reservation.auth.repository;

import com.studyroom.reservation.auth.entity.EmailVerificationToken;
import com.studyroom.reservation.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);
    List<EmailVerificationToken> findAllByUserAndUsedAtIsNullAndInvalidatedAtIsNull(User user);
}
