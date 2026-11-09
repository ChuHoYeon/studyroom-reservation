package com.studyroom.reservation.space.integration;

import com.studyroom.reservation.space.entity.Space;
import com.studyroom.reservation.space.entity.SpaceStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class SpaceCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUpdateAndDeactivate_persistsSpaceAndHidesInactiveSpaceFromPublicApi() throws Exception {
        String originalName = "통합테스트-" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(originalName, 4, "09:00", "22:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(originalName))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        Space createdSpace = findByName(originalName);
        long spaceId = createdSpace.getId();
        assertThat(createdSpace.getCreatedAt()).isNotNull();
        assertThat(createdSpace.getUpdatedAt()).isNotNull();
        assertThat(createdSpace.getStatus()).isEqualTo(SpaceStatus.ACTIVE);
        assertThat(createdSpace.getDeactivateAt()).isNull();

        String updatedName = originalName + "-수정";
        mockMvc.perform(put("/api/v1/admin/spaces/{spaceId}", spaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(updatedName, 6, "10:00", "21:00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedName))
                .andExpect(jsonPath("$.capacity").value(6));

        entityManager.flush();
        entityManager.clear();
        Space updatedSpace = entityManager.find(Space.class, spaceId);
        assertThat(updatedSpace.getName()).isEqualTo(updatedName);
        assertThat(updatedSpace.getCapacity()).isEqualTo(6);
        assertThat(updatedSpace.getOpenTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(updatedSpace.getCloseTime()).isEqualTo(LocalTime.of(21, 0));
        assertThat(updatedSpace.getUpdatedAt()).isNotNull();

        mockMvc.perform(delete("/api/v1/admin/spaces/{spaceId}", spaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.deactivateAt").isEmpty());

        entityManager.flush();
        entityManager.clear();
        Space inactiveSpace = entityManager.find(Space.class, spaceId);
        assertThat(inactiveSpace.getStatus()).isEqualTo(SpaceStatus.INACTIVE);

        mockMvc.perform(get("/api/v1/spaces/{spaceId}", spaceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SPACE_NOT_FOUND"));
    }

    private Space findByName(String name) {
        return entityManager.createQuery(
                        "select s from Space s where s.name = :name",
                        Space.class
                )
                .setParameter("name", name)
                .getSingleResult();
    }

    private String body(String name, int capacity, String openTime, String closeTime) {
        return """
                {
                  "name": "%s",
                  "description": "통합 테스트 공간",
                  "capacity": %d,
                  "openTime": "%s",
                  "closeTime": "%s"
                }
                """.formatted(name, capacity, openTime, closeTime);
    }
}
