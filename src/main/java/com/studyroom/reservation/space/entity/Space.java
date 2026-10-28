package com.studyroom.reservation.space.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "spaces")
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", length = 20, nullable = false)
    private SpaceStatus status;

    @Column(name = "deactivate_at")
    private LocalDateTime deactivateAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Space(String name, String description, int capacity, LocalTime openTime, LocalTime closeTime) {
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.status = SpaceStatus.ACTIVE;

    }
    public static Space create(String name, String description, int capacity, LocalTime openTime, LocalTime closeTime) {
        if (name == null || openTime == null || closeTime == null) {
            throw new IllegalArgumentException("필수 정보를 입력해 주세요.");
        }

        String normalizedName = Objects.requireNonNull(name).trim();

        if (normalizedName.isEmpty() || normalizedName.length() > 100) {
            throw new IllegalArgumentException("스터디룸 이름은 1자 이상 100자 이하이어야 합니다.");
        }

        if (capacity < 1) {
            throw new IllegalArgumentException("수용 인원은 1명 이상이어야 합니다.");
        }

        if (!openTime.isBefore(closeTime)) {
            throw new IllegalArgumentException("운영 시작 시간은 종료 시간보다 빨라야 합니다.");
        }

        return new Space(name.trim(), description, capacity, openTime, closeTime);
    }

    public void scheduleDeactivation(LocalDateTime deactivateAt) {
        this.deactivateAt = Objects.requireNonNull(deactivateAt);
    }

    public void deactivate() {
        this.status = SpaceStatus.INACTIVE;
        this.deactivateAt = null;
    }
}
