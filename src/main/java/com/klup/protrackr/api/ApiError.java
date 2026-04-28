package com.klup.protrackr.api;

import java.time.Instant;
import java.util.Map;
// api records
public record ApiError(
        boolean success,
        String message,
        Map<String, Object> details,
        Instant timestamp
) //api
{
    public static ApiError of(String message, Map<String, Object> details) {
        return new ApiError(false, message, details, Instant.now());
    }
}
