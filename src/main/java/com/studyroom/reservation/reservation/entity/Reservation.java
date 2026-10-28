package com.studyroom.reservation.reservation.entity;

import com.studyroom.reservation.space.entity.Space;
import com.studyroom.reservation.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reservations")
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "space_id", referencedColumnName = "id", nullable = false)
    private Space space;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "participant_count", nullable = false)
    private int participantCount;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'RESERVED'")
    @Column(name = "status", length = 20, nullable = false)
    private ReservationStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    private Reservation(User user, Space space, LocalDate date, LocalTime startTime, LocalTime endTime, int participantCount) {
        this.user = user;
        this.space = space;
        this.reservationDate = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.participantCount = participantCount;
        this.status = ReservationStatus.RESERVED;
    }


    public static Reservation create(User user, Space space, LocalDate reservationDate, LocalTime startTime, LocalTime endTime, int participantCount) {
        if (user == null || space == null || reservationDate == null || startTime == null || endTime == null){
            throw new IllegalArgumentException("필수 정보를 입력해 주세요.");
        }

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 빨라야 합니다.");
        }

        if (participantCount < 1) {
            throw new IllegalArgumentException("사용 인원은 1명 이상이어야 합니다.");
        }

        if (Duration.between(startTime, endTime).toMinutes() < 30) {
            throw new IllegalArgumentException("최소 예약 시간은 30분입니다.");
        }

        if (startTime.getMinute() != 0 && startTime.getMinute() != 30) {
            throw new IllegalArgumentException("시작 예약 시간은 30분 단위 입니다.");
        }

        if (endTime.getMinute() != 0 && endTime.getMinute() != 30) {
            throw new IllegalArgumentException("종료 예약 시간은 30분 단위 입니다.");
        }

        return new Reservation(user, space, reservationDate, startTime, endTime, participantCount);
    }

    public void cancel(LocalDateTime canceledAt) {
        if (status == ReservationStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 예약입니다.");
        }

        LocalDateTime cancelTime = Objects.requireNonNull(canceledAt);

        this.status = ReservationStatus.CANCELED;
        this.canceledAt = cancelTime;
    }
}
