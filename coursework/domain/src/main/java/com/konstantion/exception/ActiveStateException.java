package com.konstantion.exception;

public class ActiveStateException extends BadRequestException {
    public ActiveStateException(String message) {
        super(message);
    }
}
