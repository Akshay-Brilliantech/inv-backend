package com.xeine.utils.responsehandler;

public class ApiResponseUtil {

    /**
     * Create success response with data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, 200, message, data);
    }

    /**
     * Create success response without data
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, 200, message);
    }

    /**
     * Create success response with custom status
     */
    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<>(true, status, message, data);
    }

    /**
     * Create error response
     */
    public static <T> ApiResponse<T> error(String message, String error) {
        return new ApiResponse<>(false, 400, message, error);
    }

    /**
     * Create error response with custom status
     */
    public static <T> ApiResponse<T> error(int status, String message, String error) {
        return new ApiResponse<>(false, status, message, error);
    }

    /**
     * Create error response with just message
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, 400, message, message);
    }

    /**
     * Create error response with custom status and just message
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(false, status, message, message);
    }
}
