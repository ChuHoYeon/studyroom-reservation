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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SpaceService {

    private final SpaceRepository spaceRepository;

    public List<SpaceResponseDTO> findAllActive() {
        return spaceRepository.findAllByStatus(SpaceStatus.ACTIVE)
                .stream()
                .map(SpaceResponseDTO::from)
                .toList();
    }

    public SpaceResponseDTO findActive(long spaceId) {
        Space space = spaceRepository.findByIdAndStatus(spaceId, SpaceStatus.ACTIVE).orElseThrow(SpaceNotFoundException::new);

        return SpaceResponseDTO.from(space);
    }

    @Transactional
    public AdminSpaceResponseDTO create(SpaceRequestDTO request) {
        Space space = spaceRepository.save(Space.create(
                request.name(),
                request.description(),
                request.capacity(),
                request.openTime(),
                request.closeTime()
        ));

        return AdminSpaceResponseDTO.from(space);
    }

    @Transactional
    public AdminSpaceResponseDTO update(long spaceId, SpaceRequestDTO request) {
        Space space = spaceRepository.findById(spaceId).orElseThrow(SpaceNotFoundException::new);

        if (space.getStatus() == SpaceStatus.INACTIVE) {
            throw new SpaceInactiveException();
        }

        space.update(
                request.name(),
                request.description(),
                request.capacity(),
                request.openTime(),
                request.closeTime()
        );

        return AdminSpaceResponseDTO.from(space);
    }

    @Transactional
    public SpaceDeactivationResponseDTO deactivate(long spaceId) {
        Space space = spaceRepository.findById(spaceId).orElseThrow(SpaceNotFoundException::new);

        if (space.getStatus() == SpaceStatus.ACTIVE) {
            space.deactivate();
        }

        return SpaceDeactivationResponseDTO.from(space);
    }

}
