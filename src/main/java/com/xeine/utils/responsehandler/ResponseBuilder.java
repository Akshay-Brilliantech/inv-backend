package com.xeine.utils.responsehandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseBuilder {

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, HttpStatus status, T data) {
        ApiResponse<T> response = new ApiResponse<>(true, status.value(), message, data, null);
        return new ResponseEntity<>(response, status);
    }

    public static ResponseEntity<ApiResponse<Object>> error(String message, HttpStatus status) {
        ApiResponse<Object> response = new ApiResponse<>(false, status.value(), message, null, message);
        return new ResponseEntity<>(response, status);
    }
}