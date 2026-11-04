package com.studyroom.reservation.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailSenderTest {

    @Mock
    private JavaMailSender javaMailSender;

    private EmailSender emailSender;

    @BeforeEach
    void setUp() {
        emailSender = new EmailSender(javaMailSender);
        ReflectionTestUtils.setField(
                emailSender,
                "appMailFrom",
                "no-reply@studyroom.local"
        );
    }

    @Test
    void sendVerificationEmail_sendsMessageWithVerificationDetails() {
        String verificationUrl =
                "http://localhost:8080/api/v1/auth/email/verify?token=raw-token";

        emailSender.sendVerificationEmail("user@example.com", verificationUrl);

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getFrom()).isEqualTo("no-reply@studyroom.local");
        assertThat(sentMessage.getTo()).containsExactly("user@example.com");
        assertThat(sentMessage.getSubject())
                .isEqualTo("스터디룸 예약 서비스 이메일 인증");
        assertThat(sentMessage.getText())
                .contains("스터디룸 예약 서비스 이메일 인증")
                .contains("아래 링크를 눌러 이메일 인증을 완료해 주세요.")
                .contains(verificationUrl)
                .contains("이 링크는 30분 동안 유효합니다.");
    }

    @Test
    void sendVerificationEmail_propagatesMailException() {
        MailSendException mailException = new MailSendException("메일 발송 실패");
        doThrow(mailException)
                .when(javaMailSender)
                .send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailSender.sendVerificationEmail(
                "user@example.com",
                "http://localhost:8080/api/v1/auth/email/verify?token=raw-token"
        ))
                .isSameAs(mailException);
    }
}
