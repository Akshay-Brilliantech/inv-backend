package com.xeine.utils.responsehandler;

public class ApiResponse<T> {
    private boolean success;
    private int status;
    private String message;
    private T data;
    private String error;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, int status, String message, T data, String error) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.data = data;
        this.error = error;
    }

    public ApiResponse(boolean success, int status, String message) {
        this.success = success;
        this.status = status;
        this.message = message;
    }

    public ApiResponse(boolean success, int status, String message, String error) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.error = error;
    }

    public ApiResponse(boolean success, int status, String message, T data) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
