package com.studyroom.reservation.user.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Import(UserJpaAuditingTest.TestClockConfig.class)
@ActiveProfiles("test")
class UserJpaAuditingTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Instant INITIAL_INSTANT = Instant.parse("2026-10-29T01:00:00Z");

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MutableClock clock;

    @BeforeEach
    void setUp() {
        clock.setInstant(INITIAL_INSTANT);
    }

    @Test
    void save_setsCreatedAtAndUpdatedAt() {
        User user = User.create("test@example.com", "encoded-password", "홍길동");

        entityManager.persist(user);
        entityManager.flush();

        LocalDateTime expected = LocalDateTime.ofInstant(INITIAL_INSTANT, SEOUL);
        assertThat(user.getCreatedAt()).isEqualTo(expected);
        assertThat(user.getUpdatedAt()).isEqualTo(expected);
    }

    @Test
    void update_changesOnlyUpdatedAt() {
        User user = User.create("test@example.com", "encoded-password", "홍길동");
        entityManager.persist(user);
        entityManager.flush();

        LocalDateTime createdAt = user.getCreatedAt();
        LocalDateTime firstUpdatedAt = user.getUpdatedAt();

        clock.advance(Duration.ofMinutes(1));
        user.verifyEmail();
        entityManager.flush();

        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.getUpdatedAt()).isAfter(firstUpdatedAt);
        assertThat(user.getUpdatedAt())
                .isEqualTo(LocalDateTime.ofInstant(clock.instant(), SEOUL));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestClockConfig {

        @Bean
        @Primary
        MutableClock testClock() {
            return new MutableClock(INITIAL_INSTANT, SEOUL);
        }
    }

    static final class MutableClock extends Clock {

        private final AtomicReference<Instant> instant;
        private final ZoneId zone;

        private MutableClock(Instant initialInstant, ZoneId zone) {
            this(new AtomicReference<>(initialInstant), zone);
        }

        private MutableClock(AtomicReference<Instant> instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        void setInstant(Instant instant) {
            this.instant.set(instant);
        }

        void advance(Duration duration) {
            instant.updateAndGet(current -> current.plus(duration));
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant.get();
        }
    }
}
