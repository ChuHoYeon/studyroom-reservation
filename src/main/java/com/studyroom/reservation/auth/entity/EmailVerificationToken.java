package com.studyroom.reservation.auth.entity;

import com.studyroom.reservation.common.exception.InvalidEmailVerificationTokenException;
import com.studyroom.reservation.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "email_verification_tokens")
@EntityListeners(AuditingEntityListener.class)
@Entity
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "token_hash", columnDefinition = "CHAR(64)", unique = true, nullable = false)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false, comment = "토큰 만료 시각")
    private LocalDateTime expiresAt;

    @Column(name = "used_at", comment = "인증 성공 시각")
    private LocalDateTime usedAt;

    @Column(name = "invalidated_at", comment = "토큰 무효화 시각")
    private LocalDateTime invalidatedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, comment = "토큰 생성 시각")
    private LocalDateTime created_at;

    private EmailVerificationToken(User user, String tokenHash, LocalDateTime expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    // 해시값과 만료 시각을 가진 토큰 엔티티 생성
    public static EmailVerificationToken issue(User user, String tokenHash, LocalDateTime expiresAt) {
        if (user == null) {
            throw new IllegalArgumentException("유저정보가 필요합니다.");
        }

        if (tokenHash == null || !tokenHash.matches("^[0-9a-f]{64}$")) {
            throw new IllegalArgumentException("64자 SHA-256 해시가 필요합니다.");
        }

        if (expiresAt == null) {
            throw new IllegalArgumentException("토큰 만료 시각이 필요합니다.");
        }

        return new EmailVerificationToken(user, tokenHash, expiresAt);
    }

    // 토큰 사용 완료 상태로 변경
    public void markUsed(LocalDateTime usedAt) {
        LocalDateTime verifiedAt = Objects.requireNonNull(usedAt);

        if (this.usedAt != null || invalidatedAt != null) {
            throw new InvalidEmailVerificationTokenException();
        }

        if (!verifiedAt.isBefore(expiresAt)) {
            throw new InvalidEmailVerificationTokenException();
        }

        this.usedAt = verifiedAt;
    }

    // 토큰 사용 불가 상태로 변경
    public void invalidate(LocalDateTime invalidatedAt) {
        if (usedAt != null) {
            throw new IllegalStateException("이미 사용된 토큰입니다.");
        }

        this.invalidatedAt = Objects.requireNonNull(invalidatedAt);
    }
}
