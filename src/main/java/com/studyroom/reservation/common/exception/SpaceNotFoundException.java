package com.studyroom.reservation.common.exception;

public class SpaceNotFoundException extends RuntimeException {
    public SpaceNotFoundException() {
        super("스터디룸을 찾을 수 없습니다.");
    }
}
