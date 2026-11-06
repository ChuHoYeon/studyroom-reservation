package com.studyroom.reservation.common.exception;

public class InvalidEmailVerificationTokenException extends RuntimeException {

    public InvalidEmailVerificationTokenException() {
        super("유효하지 않거나 만료된 이메일 인증 링크입니다.");
    }
}
