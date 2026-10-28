package com.studyroom.reservation.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'USER'")
    @Column(name = "role", length = 20, nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", length = 20, nullable = false)
    private UserStatus status;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private User(String email, String encodedPassword, String name) {
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.password = Objects.requireNonNull(encodedPassword);
        this.name = normalizeName(name);
        this.role = UserRole.USER;
        this.status = UserStatus.ACTIVE;
        this.emailVerified = false;
    }

    private String normalizeName(String name) {
        String normalizedName = Objects.requireNonNull(name).trim();

        if (normalizedName.isEmpty() || normalizedName.length() > 50) {
            throw new IllegalArgumentException("이름은 1자 이상 50자 이하이어야 합니다.");
        }

        return normalizedName;
    }

    public static User create(String email,String encodedPassword,String name) {
        if (email == null || encodedPassword == null || name == null) {
            throw new IllegalArgumentException("필수 정보를 입력해 주세요.");
        }

        return new User(email, encodedPassword, name);
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }
}
