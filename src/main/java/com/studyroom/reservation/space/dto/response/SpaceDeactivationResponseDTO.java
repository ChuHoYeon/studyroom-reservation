package com.studyroom.reservation.space.dto.response;

import com.studyroom.reservation.space.entity.Space;
import com.studyroom.reservation.space.entity.SpaceStatus;

import java.time.LocalDateTime;

public record SpaceDeactivationResponseDTO(
        long id,
        SpaceStatus status,
        LocalDateTime deactivateAt
) {
    public static SpaceDeactivationResponseDTO from(Space space) {
        return new SpaceDeactivationResponseDTO(
                space.getId(),
                space.getStatus(),
                space.getDeactivateAt()
        );
    }
}
