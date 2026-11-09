package com.studyroom.reservation.common.exception;

public class SpaceInactiveException extends RuntimeException {
    public SpaceInactiveException() {
        super("비활성화된 스터디룸은 수정할 수 없습니다.");
    }
}
