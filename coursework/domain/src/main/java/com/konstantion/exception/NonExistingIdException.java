package com.konstantion.exception;

public class NonExistingIdException extends BadRequestException {
    public NonExistingIdException(String message) {
        super(message);
    }
}
