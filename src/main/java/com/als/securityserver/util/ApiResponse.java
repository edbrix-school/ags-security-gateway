package com.als.securityserver.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public final class ApiResponse {

    private ApiResponse() {
    }

    public static ResponseEntity<?> success(String message, Object data) {
        return ResponseEntity.ok(Map.of(
                "statusCode", HttpStatus.OK.value(),
                "success", true,
                "message", message,
                "result", data != null ? Map.of("data", data) : ""
        ));
    }

    public static ResponseEntity<?> error(String message, int statusCode) {
        return ResponseEntity.status(statusCode).body(Map.of(
                "success", false,
                "statusCode", statusCode,
                "message", message
        ));
    }

    public static ResponseEntity<?> unauthorized(String message) {
        return error(message, HttpStatus.UNAUTHORIZED.value());
    }

    public static ResponseEntity<?> internalServerError(String message) {
        return error(message, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}

