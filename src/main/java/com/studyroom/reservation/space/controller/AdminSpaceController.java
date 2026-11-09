package com.studyroom.reservation.space.controller;

import com.studyroom.reservation.space.dto.request.SpaceRequestDTO;
import com.studyroom.reservation.space.dto.response.AdminSpaceResponseDTO;
import com.studyroom.reservation.space.dto.response.SpaceDeactivationResponseDTO;
import com.studyroom.reservation.space.service.SpaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/spaces")
@RestController
public class AdminSpaceController {

    private final SpaceService spaceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminSpaceResponseDTO create(@Valid @RequestBody SpaceRequestDTO request) {
        return spaceService.create(request);
    }

    @PutMapping("/{spaceId}")
    public AdminSpaceResponseDTO update(@PathVariable long spaceId, @Valid @RequestBody SpaceRequestDTO request) {
        return spaceService.update(spaceId, request);
    }

    @DeleteMapping("/{spaceId}")
    public SpaceDeactivationResponseDTO deactivate(@PathVariable long spaceId) {
        return spaceService.deactivate(spaceId);
    }
}
