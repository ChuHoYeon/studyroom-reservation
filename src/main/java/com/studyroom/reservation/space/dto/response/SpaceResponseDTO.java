package com.studyroom.reservation.space.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.studyroom.reservation.space.entity.Space;

import java.time.LocalTime;

public record SpaceResponseDTO(
        long id,

        String name,

        String description,

        int capacity,

        @JsonFormat(pattern = "HH:mm")
        LocalTime openTime,

        @JsonFormat(pattern = "HH:mm")
        LocalTime closeTime
) {
    public static SpaceResponseDTO from(Space space) {
        return new SpaceResponseDTO(
                space.getId(),
                space.getName(),
                space.getDescription(),
                space.getCapacity(),
                space.getOpenTime(),
                space.getCloseTime()
        );
    }
}
