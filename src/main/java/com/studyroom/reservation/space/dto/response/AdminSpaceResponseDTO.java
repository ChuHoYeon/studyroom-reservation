package com.studyroom.reservation.space.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.studyroom.reservation.space.entity.Space;
import com.studyroom.reservation.space.entity.SpaceStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record AdminSpaceResponseDTO(
        long id,

        String name,

        String description,

        int capacity,

        @JsonFormat(pattern = "HH:mm")
        LocalTime openTime,

        @JsonFormat(pattern = "HH:mm")
        LocalTime closeTime,

        SpaceStatus status,

        LocalDateTime deactivateAt
) {
    public static AdminSpaceResponseDTO from(Space space) {
        return new AdminSpaceResponseDTO(
                space.getId(),
                space.getName(),
                space.getDescription(),
                space.getCapacity(),
                space.getOpenTime(),
                space.getCloseTime(),
                space.getStatus(),
                space.getDeactivateAt()
        );
    }
}
