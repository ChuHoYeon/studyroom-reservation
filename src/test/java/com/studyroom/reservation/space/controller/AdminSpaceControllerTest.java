package com.studyroom.reservation.space.controller;

import com.studyroom.reservation.auth.security.SecurityConfig;
import com.studyroom.reservation.common.exception.GlobalExceptionHandler;
import com.studyroom.reservation.space.dto.request.SpaceRequestDTO;
import com.studyroom.reservation.space.dto.response.AdminSpaceResponseDTO;
import com.studyroom.reservation.space.dto.response.SpaceDeactivationResponseDTO;
import com.studyroom.reservation.space.entity.SpaceStatus;
import com.studyroom.reservation.space.service.SpaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminSpaceController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AdminSpaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpaceService spaceService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returnsCreatedResponseForAdmin() throws Exception {
        when(spaceService.create(any(SpaceRequestDTO.class)))
                .thenReturn(adminResponse("스터디룸 A"));

        mockMvc.perform(post("/api/v1/admin/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody("  스터디룸 A  ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("스터디룸 A"))
                .andExpect(jsonPath("$.openTime").value("09:00"))
                .andExpect(jsonPath("$.closeTime").value("22:00"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.deactivateAt").isEmpty());

        verify(spaceService).create(new SpaceRequestDTO(
                "스터디룸 A",
                "화이트보드가 있는 공간",
                4,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0)
        ));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returnsBadRequestForInvalidOperatingHours() throws Exception {
        mockMvc.perform(post("/api/v1/admin/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "스터디룸 A",
                                  "capacity": 4,
                                  "openTime": "22:00",
                                  "closeTime": "09:00"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(spaceService, never()).create(any(SpaceRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_returnsForbiddenForNonAdminUser() throws Exception {
        mockMvc.perform(post("/api/v1/admin/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody("스터디룸 A")))
                .andExpect(status().isForbidden());

        verify(spaceService, never()).create(any(SpaceRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_returnsUpdatedResponseForAdmin() throws Exception {
        when(spaceService.update(org.mockito.ArgumentMatchers.eq(1L), any(SpaceRequestDTO.class)))
                .thenReturn(adminResponse("스터디룸 B"));

        mockMvc.perform(put("/api/v1/admin/spaces/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody("스터디룸 B")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("스터디룸 B"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivate_returnsInactiveStateForAdmin() throws Exception {
        when(spaceService.deactivate(1L))
                .thenReturn(new SpaceDeactivationResponseDTO(
                        1L,
                        SpaceStatus.INACTIVE,
                        null
                ));

        mockMvc.perform(delete("/api/v1/admin/spaces/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.deactivateAt").isEmpty());
    }

    private AdminSpaceResponseDTO adminResponse(String name) {
        return new AdminSpaceResponseDTO(
                1L,
                name,
                "화이트보드가 있는 공간",
                4,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                SpaceStatus.ACTIVE,
                null
        );
    }

    private String validBody(String name) {
        return """
                {
                  "name": "%s",
                  "description": "화이트보드가 있는 공간",
                  "capacity": 4,
                  "openTime": "09:00",
                  "closeTime": "22:00"
                }
                """.formatted(name);
    }
}
