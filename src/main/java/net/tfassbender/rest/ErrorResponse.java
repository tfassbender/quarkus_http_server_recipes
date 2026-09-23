package net.tfassbender.rest;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String message, Long retryAfterSeconds) {

    public ErrorResponse(String message) {
        this(message, null);
    }
}
