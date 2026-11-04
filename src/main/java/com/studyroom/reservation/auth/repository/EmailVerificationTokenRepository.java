package com.studyroom.reservation.auth.repository;

import com.studyroom.reservation.auth.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
}
