package com.teamsync.config;

import com.teamsync.dto.ApiResponse;
import com.teamsync.exception.ResourceNotFoundException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

// Global handler for response wrapping and exception handling
@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Apply to all controllers except those returning ApiResponse
        return !returnType.getParameterType().equals(ApiResponse.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // Skip wrapping for null responses (e.g., 204 No Content)
        if (body == null) {
            return null;
        }
        // Use generic message based on HTTP method
        String message = getGenericMessage(request.getMethod().toString(), body);
        return ApiResponse.success(message, body);
    }

    // Generic messages based on HTTP method
    private String getGenericMessage(String method, Object body) {
        switch (method) {
            case "POST":
                return "resource created successfully";
            case "GET":
                return body instanceof Iterable ? "resources retrieved successfully" : "resource retrieved successfully";
            case "PUT":
                return "resource updated successfully";
            case "DELETE":
                return "resource deleted successfully";
            default:
                return "request processed successfully";
        }
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiResponse<Void> handleResourceNotFoundException(ResourceNotFoundException ex, ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.NOT_FOUND);
        return ApiResponse.error(ex.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException ex, ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        return ApiResponse.error(ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleGenericException(Exception ex, ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return ApiResponse.error("An unexpected error occurred", null);
    }
}