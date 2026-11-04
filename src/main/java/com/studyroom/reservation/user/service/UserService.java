package com.studyroom.reservation.user.service;

import com.studyroom.reservation.auth.service.EmailVerificationService;
import com.studyroom.reservation.common.exception.DuplicateEmailException;
import com.studyroom.reservation.user.dto.request.SignUpRequestDTO;
import com.studyroom.reservation.user.dto.response.SignUpResponseDTO;
import com.studyroom.reservation.user.entity.User;
import com.studyroom.reservation.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public SignUpResponseDTO signUp(SignUpRequestDTO request) {
        // 이메일 검증
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }

        // 비밀번호 암호화
        String encodePassword = passwordEncoder.encode(request.password());

        User user = User.create(request.email(), encodePassword, request.name());

        User savedUser = userRepository.save(user);

        emailVerificationService.issueAndSend(savedUser);

        return SignUpResponseDTO.from(savedUser);
    }
}
