package com.xeine.dto;

public class OtpVerifyRequestDto {
    private String mobile;
    private String otp;

    // Getters
    public String getMobile() {
        return mobile;
    }

    public String getOtp() {
        return otp;
    }

    // Setters
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}

