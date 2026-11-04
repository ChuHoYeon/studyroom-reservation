package com.studyroom.reservation.user.dto.response;

import com.studyroom.reservation.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResponseDTO {
    private long id;
    private String email;
    private String name;
    private boolean emailVerified;

    public static SignUpResponseDTO from(User user) {
        return SignUpResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
