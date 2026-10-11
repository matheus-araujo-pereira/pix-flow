package com.matheus.pixflow.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Standard API error response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @Schema(description = "Timestamp when error occurred", example = "2026-10-11T04:45:00Z")
        Instant timestamp,

        @Schema(description = "HTTP Status Code", example = "400")
        int status,

        @Schema(description = "HTTP Status Description", example = "Bad Request")
        String error,

        @Schema(description = "User-friendly error explanation", example = "Transaction amount must be strictly greater than zero.")
        String message,

        @Schema(description = "Request URI path", example = "/api/v1/charges")
        String path,

        @Schema(description = "Validation field errors map")
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path, null);
    }

    public static ErrorResponse ofValidation(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(Instant.now(), status, error, message, path, fieldErrors);
    }
}
