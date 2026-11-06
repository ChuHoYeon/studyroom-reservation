package com.studyroom.reservation.user.controller;

import com.studyroom.reservation.user.dto.request.SignUpRequestDTO;
import com.studyroom.reservation.user.dto.response.SignUpResponseDTO;
import com.studyroom.reservation.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@RestController
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SignUpResponseDTO signUp(@Valid @RequestBody SignUpRequestDTO request) {
        return userService.signUp(request);
    }
}
