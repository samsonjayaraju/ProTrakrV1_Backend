package com.klup.protrackr.api;
// apiresponse
import java.time.Instant;
//api request and response record 
public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message, Instant.now());
    }

    public static ApiResponse<Object> error(String message) {
        return new ApiResponse<>(false, null, message, Instant.now());
    }
}

