package com.studyroom.reservation.space.service;

import com.studyroom.reservation.common.exception.SpaceInactiveException;
import com.studyroom.reservation.common.exception.SpaceNotFoundException;
import com.studyroom.reservation.space.dto.request.SpaceRequestDTO;
import com.studyroom.reservation.space.dto.response.AdminSpaceResponseDTO;
import com.studyroom.reservation.space.dto.response.SpaceDeactivationResponseDTO;
import com.studyroom.reservation.space.dto.response.SpaceResponseDTO;
import com.studyroom.reservation.space.entity.Space;
import com.studyroom.reservation.space.entity.SpaceStatus;
import com.studyroom.reservation.space.repository.SpaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpaceServiceTest {

    @Mock
    private SpaceRepository spaceRepository;

    @InjectMocks
    private SpaceService spaceService;

    @Test
    void findAllActive_returnsActiveSpacesIncludingScheduledDeactivation() {
        Space activeSpace = activeSpace("스터디룸 A");
        Space scheduledSpace = activeSpace("스터디룸 B");
        scheduledSpace.scheduleDeactivation(LocalDateTime.of(2026, 11, 15, 18, 0));
        when(spaceRepository.findAllByStatus(SpaceStatus.ACTIVE))
                .thenReturn(List.of(activeSpace, scheduledSpace));

        List<SpaceResponseDTO> result = spaceService.findAllActive();

        assertThat(result)
                .extracting(SpaceResponseDTO::name)
                .containsExactly("스터디룸 A", "스터디룸 B");
    }

    @Test
    void findActive_returnsActiveSpace() {
        Space space = activeSpace("스터디룸 A");
        when(spaceRepository.findByIdAndStatus(1L, SpaceStatus.ACTIVE))
                .thenReturn(Optional.of(space));

        SpaceResponseDTO response = spaceService.findActive(1L);

        assertThat(response.name()).isEqualTo("스터디룸 A");
        assertThat(response.capacity()).isEqualTo(4);
    }

    @Test
    void findActive_throwsSpaceNotFoundExceptionWhenSpaceIsUnavailable() {
        when(spaceRepository.findByIdAndStatus(1L, SpaceStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> spaceService.findActive(1L))
                .isInstanceOf(SpaceNotFoundException.class)
                .hasMessage("스터디룸을 찾을 수 없습니다.");
    }

    @Test
    void create_savesActiveSpaceAndReturnsAdminResponse() {
        SpaceRequestDTO request = request("  스터디룸 A  ");
        when(spaceRepository.save(any(Space.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AdminSpaceResponseDTO response = spaceService.create(request);

        ArgumentCaptor<Space> captor = ArgumentCaptor.forClass(Space.class);
        verify(spaceRepository).save(captor.capture());
        Space savedSpace = captor.getValue();

        assertThat(savedSpace.getName()).isEqualTo("스터디룸 A");
        assertThat(savedSpace.getStatus()).isEqualTo(SpaceStatus.ACTIVE);
        assertThat(savedSpace.getDeactivateAt()).isNull();
        assertThat(response.name()).isEqualTo("스터디룸 A");
        assertThat(response.status()).isEqualTo(SpaceStatus.ACTIVE);
    }

    @Test
    void update_changesEditableFieldsAndReturnsAdminResponse() {
        Space space = activeSpace("스터디룸 A");
        SpaceRequestDTO request = new SpaceRequestDTO(
                "스터디룸 B",
                "변경된 설명",
                6,
                LocalTime.of(10, 0),
                LocalTime.of(21, 0)
        );
        when(spaceRepository.findById(1L)).thenReturn(Optional.of(space));

        AdminSpaceResponseDTO response = spaceService.update(1L, request);

        assertThat(space.getName()).isEqualTo("스터디룸 B");
        assertThat(space.getCapacity()).isEqualTo(6);
        assertThat(response.name()).isEqualTo("스터디룸 B");
        assertThat(response.capacity()).isEqualTo(6);
        verify(spaceRepository, never()).save(any(Space.class));
    }

    @Test
    void update_throwsSpaceInactiveExceptionForInactiveSpace() {
        Space space = activeSpace("스터디룸 A");
        space.deactivate();
        when(spaceRepository.findById(1L)).thenReturn(Optional.of(space));

        assertThatThrownBy(() -> spaceService.update(1L, request("스터디룸 B")))
                .isInstanceOf(SpaceInactiveException.class)
                .hasMessage("비활성화된 스터디룸은 수정할 수 없습니다.");
    }

    @Test
    void update_throwsSpaceNotFoundExceptionWhenSpaceDoesNotExist() {
        when(spaceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> spaceService.update(1L, request("스터디룸 A")))
                .isInstanceOf(SpaceNotFoundException.class);
    }

    @Test
    void deactivate_changesActiveSpaceToInactive() {
        Space space = activeSpace("스터디룸 A");
        when(spaceRepository.findById(1L)).thenReturn(Optional.of(space));

        SpaceDeactivationResponseDTO response = spaceService.deactivate(1L);

        assertThat(space.getStatus()).isEqualTo(SpaceStatus.INACTIVE);
        assertThat(response.status()).isEqualTo(SpaceStatus.INACTIVE);
        assertThat(response.deactivateAt()).isNull();
    }

    @Test
    void deactivate_returnsCurrentStateWhenSpaceIsAlreadyInactive() {
        Space space = activeSpace("스터디룸 A");
        space.deactivate();
        when(spaceRepository.findById(1L)).thenReturn(Optional.of(space));

        SpaceDeactivationResponseDTO response = spaceService.deactivate(1L);

        assertThat(response.status()).isEqualTo(SpaceStatus.INACTIVE);
        assertThat(response.deactivateAt()).isNull();
    }

    @Test
    void deactivate_throwsSpaceNotFoundExceptionWhenSpaceDoesNotExist() {
        when(spaceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> spaceService.deactivate(1L))
                .isInstanceOf(SpaceNotFoundException.class);
    }

    private Space activeSpace(String name) {
        return Space.create(
                name,
                "화이트보드가 있는 공간",
                4,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0)
        );
    }

    private SpaceRequestDTO request(String name) {
        return new SpaceRequestDTO(
                name,
                "화이트보드가 있는 공간",
                4,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0)
        );
    }
}
