package com.example.demo.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
public class ApiErrorResponse {
    private final String type;
    private final String title;
    private final int status;
    private final String detail;
    private final String instance;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    private final Instant timestamp;

    // این متد برای اضافه کردن فیلدهای اضافی (مثل traceId یا validation errors) در آینده عالی است
    private final Map<String, Object> additionalProperties;
}
