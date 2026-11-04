package com.studyroom.reservation.user.service;

import com.studyroom.reservation.auth.service.EmailVerificationService;
import com.studyroom.reservation.common.exception.DuplicateEmailException;
import com.studyroom.reservation.user.dto.request.SignUpRequestDTO;
import com.studyroom.reservation.user.dto.response.SignUpResponseDTO;
import com.studyroom.reservation.user.entity.User;
import com.studyroom.reservation.user.entity.UserRole;
import com.studyroom.reservation.user.entity.UserStatus;
import com.studyroom.reservation.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private UserService userService;

    @Test
    void signUp_savesUserWithEncodedPasswordAndDefaultValues() {
        SignUpRequestDTO request = new SignUpRequestDTO(
                "  USER@EXAMPLE.COM  ",
                "password123",
                "  홍길동  "
        );
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SignUpResponseDTO response = userService.signUp(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("user@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getName()).isEqualTo("홍길동");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedUser.isEmailVerified()).isFalse();

        assertThat(response.getEmail()).isEqualTo("user@example.com");
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.isEmailVerified()).isFalse();

        verify(userRepository).existsByEmail("user@example.com");
        verify(passwordEncoder).encode("password123");
        verify(emailVerificationService).issueAndSend(savedUser);
    }

    @Test
    void signUp_throwsDuplicateEmailExceptionWithoutSavingUser() {
        SignUpRequestDTO request = new SignUpRequestDTO(
                "USER@EXAMPLE.COM",
                "password123",
                "홍길동"
        );
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, emailVerificationService);
    }

    @Test
    void signUp_propagatesMailException() {
        SignUpRequestDTO request = new SignUpRequestDTO(
                "user@example.com",
                "password123",
                "홍길동"
        );
        MailSendException mailException = new MailSendException("메일 발송 실패");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(mailException)
                .when(emailVerificationService)
                .issueAndSend(any(User.class));

        assertThatThrownBy(() -> userService.signUp(request))
                .isSameAs(mailException);

        verify(userRepository).save(any(User.class));
        verify(emailVerificationService).issueAndSend(any(User.class));
    }
}
