package com.studyroom.reservation.space.controller;

import com.studyroom.reservation.space.dto.response.SpaceResponseDTO;
import com.studyroom.reservation.space.service.SpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/spaces")
@RestController
public class SpaceController {

    private final SpaceService spaceService;

    @GetMapping
    public List<SpaceResponseDTO> findAll() {
        return spaceService.findAllActive();
    }

    @GetMapping("/{spaceId}")
    public SpaceResponseDTO findById(@PathVariable long spaceId) {
        return spaceService.findActive(spaceId);
    }
}
