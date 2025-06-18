package com.teamsync.dto;

import lombok.Data;

// Generic API response wrapper
@Data
public class ApiResponse<T> {

    private String status;
    private String message;
    private T data;

    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Convenience method for success responses
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("succeeded", message, data);
    }

    // Convenience method for error responses
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>("failed", message, data);
    }
}