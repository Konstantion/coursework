package com.konstantion.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public record ResponseDto<T>(LocalDateTime timeStamp, int statusCode, HttpStatus status, String reason, String message,
                             String developerMessage, Map<String, T> data) {
    public static ResponseBuilder builder() {
        return new ResponseBuilder();
    }

    public static final class ResponseBuilder {
        private LocalDateTime timeStamp;
        private int statusCode;
        private HttpStatus status;
        private String reason;
        private String message;
        private String developerMessage;
        private Map<?, ?> data;

        private ResponseBuilder() {
        }

        public ResponseBuilder timeStamp(LocalDateTime timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }

        public ResponseBuilder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public ResponseBuilder status(HttpStatus status) {
            this.status = status;
            return this;
        }

        public ResponseBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public ResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ResponseBuilder developerMessage(String developerMessage) {
            this.developerMessage = developerMessage;
            return this;
        }

        public ResponseBuilder data(Map<?, ?> data) {
            this.data = data;
            return this;
        }

        public ResponseDto build() {
            return new ResponseDto(timeStamp, statusCode, status, reason, message, developerMessage, data);
        }
    }
}
