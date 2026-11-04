package com.studyroom.reservation.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EmailSender {

    private final JavaMailSender javaMailSender;

    @Value("${app.mail.from}")
    private String appMailFrom;

    public void sendVerificationEmail(String toEmail, String verificationUrl) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(appMailFrom);
        msg.setSubject("스터디룸 예약 서비스 이메일 인증");

        msg.setTo(toEmail);
        msg.setText(
                """
                스터디룸 예약 서비스 이메일 인증

                아래 링크를 눌러 이메일 인증을 완료해 주세요.

                %s

                이 링크는 30분 동안 유효합니다.
                """.formatted(verificationUrl));

        javaMailSender.send(msg);
    }
}
