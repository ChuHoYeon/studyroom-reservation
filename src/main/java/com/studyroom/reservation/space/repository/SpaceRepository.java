package com.studyroom.reservation.space.repository;

import com.studyroom.reservation.space.entity.Space;
import com.studyroom.reservation.space.entity.SpaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpaceRepository extends JpaRepository<Space, Long> {
    List<Space> findAllByStatus(SpaceStatus status);
    Optional<Space> findByIdAndStatus(long id, SpaceStatus status);
}
