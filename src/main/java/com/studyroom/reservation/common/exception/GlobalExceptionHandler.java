package com.studyroom.reservation.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException exception) {
        ErrorResponse response = new ErrorResponse("DUPLICATE_EMAIL", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(InvalidEmailVerificationTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEmailVerificationToken(InvalidEmailVerificationTokenException exception) {
        ErrorResponse response = new ErrorResponse("INVALID_EMAIL_VERIFICATION_TOKEN", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(SpaceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSpaceNotFoundException(SpaceNotFoundException exception) {
        ErrorResponse response = new ErrorResponse("SPACE_NOT_FOUND", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(SpaceInactiveException.class)
    public ResponseEntity<ErrorResponse> handleSpaceInactiveException(SpaceInactiveException exception) {
        ErrorResponse response = new ErrorResponse("SPACE_INACTIVE", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
}
