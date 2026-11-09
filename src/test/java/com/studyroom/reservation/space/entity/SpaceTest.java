package com.studyroom.reservation.space.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpaceTest {

    @Test
    void create_normalizesNameAndSetsDefaultState() {
        Space space = Space.create(
                "  스터디룸 A  ",
                "화이트보드가 있는 공간",
                4,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0)
        );

        assertThat(space.getName()).isEqualTo("스터디룸 A");
        assertThat(space.getDescription()).isEqualTo("화이트보드가 있는 공간");
        assertThat(space.getCapacity()).isEqualTo(4);
        assertThat(space.getStatus()).isEqualTo(SpaceStatus.ACTIVE);
        assertThat(space.getDeactivateAt()).isNull();
    }

    @Test
    void create_rejectsBlankName() {
        assertThatThrownBy(() -> Space.create(
                "   ",
                null,
                4,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("스터디룸 이름은 1자 이상 100자 이하이어야 합니다.");
    }

    @Test
    void create_rejectsCapacityLessThanOne() {
        assertThatThrownBy(() -> Space.create(
                "스터디룸 A",
                null,
                0,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수용 인원은 1명 이상이어야 합니다.");
    }

    @Test
    void create_rejectsInvalidOperatingHours() {
        assertThatThrownBy(() -> Space.create(
                "스터디룸 A",
                null,
                4,
                LocalTime.of(22, 0),
                LocalTime.of(9, 0)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("운영 시작 시간은 종료 시간보다 빨라야 합니다.");
    }

    @Test
    void update_changesEditableFieldsAndNormalizesName() {
        Space space = activeSpace();

        space.update(
                "  스터디룸 B  ",
                "대형 모니터가 있는 공간",
                6,
                LocalTime.of(10, 0),
                LocalTime.of(21, 0)
        );

        assertThat(space.getName()).isEqualTo("스터디룸 B");
        assertThat(space.getDescription()).isEqualTo("대형 모니터가 있는 공간");
        assertThat(space.getCapacity()).isEqualTo(6);
        assertThat(space.getOpenTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(space.getCloseTime()).isEqualTo(LocalTime.of(21, 0));
    }

    @Test
    void update_rejectsInvalidValuesWithoutChangingExistingState() {
        Space space = activeSpace();

        assertThatThrownBy(() -> space.update(
                "스터디룸 B",
                "변경된 설명",
                0,
                LocalTime.of(10, 0),
                LocalTime.of(21, 0)
        )).isInstanceOf(IllegalArgumentException.class);

        assertThat(space.getName()).isEqualTo("스터디룸 A");
        assertThat(space.getDescription()).isEqualTo("기존 설명");
        assertThat(space.getCapacity()).isEqualTo(4);
    }

    @Test
    void scheduleDeactivation_setsDeactivateAtWithoutChangingActiveStatus() {
        Space space = activeSpace();
        LocalDateTime deactivateAt = LocalDateTime.of(2026, 11, 15, 18, 0);

        space.scheduleDeactivation(deactivateAt);

        assertThat(space.getStatus()).isEqualTo(SpaceStatus.ACTIVE);
        assertThat(space.getDeactivateAt()).isEqualTo(deactivateAt);
    }

    @Test
    void deactivate_setsInactiveAndClearsDeactivateAt() {
        Space space = activeSpace();
        space.scheduleDeactivation(LocalDateTime.of(2026, 11, 15, 18, 0));

        space.deactivate();

        assertThat(space.getStatus()).isEqualTo(SpaceStatus.INACTIVE);
        assertThat(space.getDeactivateAt()).isNull();
    }

    private Space activeSpace() {
        return Space.create(
                "스터디룸 A",
                "기존 설명",
                4,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0)
        );
    }
}
