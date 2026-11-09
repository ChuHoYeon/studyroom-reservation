package com.studyroom.reservation.space.controller;

import com.studyroom.reservation.auth.security.SecurityConfig;
import com.studyroom.reservation.common.exception.GlobalExceptionHandler;
import com.studyroom.reservation.common.exception.SpaceNotFoundException;
import com.studyroom.reservation.space.dto.response.SpaceResponseDTO;
import com.studyroom.reservation.space.service.SpaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpaceController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class SpaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpaceService spaceService;

    @Test
    void findAll_allowsUnauthenticatedRequestAndReturnsActiveSpaces() throws Exception {
        when(spaceService.findAllActive()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/v1/spaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("스터디룸 A"))
                .andExpect(jsonPath("$[0].capacity").value(4))
                .andExpect(jsonPath("$[0].openTime").value("09:00"))
                .andExpect(jsonPath("$[0].closeTime").value("22:00"))
                .andExpect(jsonPath("$[0].status").doesNotExist())
                .andExpect(jsonPath("$[0].deactivateAt").doesNotExist());
    }

    @Test
    void findAll_returnsEmptyArrayWhenNoActiveSpaceExists() throws Exception {
        when(spaceService.findAllActive()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/spaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void findById_allowsUnauthenticatedRequestAndReturnsSpace() throws Exception {
        when(spaceService.findActive(1L)).thenReturn(response());

        mockMvc.perform(get("/api/v1/spaces/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("스터디룸 A"))
                .andExpect(jsonPath("$.openTime").value("09:00"))
                .andExpect(jsonPath("$.closeTime").value("22:00"));
    }

    @Test
    void findById_returnsNotFoundForUnavailableSpace() throws Exception {
        when(spaceService.findActive(1L)).thenThrow(new SpaceNotFoundException());

        mockMvc.perform(get("/api/v1/spaces/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SPACE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("스터디룸을 찾을 수 없습니다."));
    }

    private SpaceResponseDTO response() {
        return new SpaceResponseDTO(
                1L,
                "스터디룸 A",
                "화이트보드가 있는 공간",
                4,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0)
        );
    }
}
