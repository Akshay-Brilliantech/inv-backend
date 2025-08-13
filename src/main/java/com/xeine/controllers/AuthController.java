package com.xeine.controllers;

import com.xeine.dto.OtpRequestDto;
import com.xeine.dto.OtpVerifyRequestDto;
import com.xeine.models.Company;
import com.xeine.repository.CompanyRepository;
import com.xeine.services.CompanyService;
import com.xeine.utils.JwtUtil;
import com.xeine.utils.responsehandler.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private CompanyService otpService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CompanyRepository userRepository;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody OtpRequestDto request) {
        try {
            String mobile = request.getMobile();
            otpService.sendOtp(mobile);
            return ResponseBuilder.success("OTP sent successfully", HttpStatus.OK, null);
        } catch (Exception e) {
            return ResponseBuilder.error("Failed to send OTP: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyRequestDto request) {
        try {
            String mobile = request.getMobile();
            String otp = request.getOtp();

            System.out.println(request.getOtp());
            System.out.println(request.getMobile());

            String res = otpService.verifyOtp(mobile, otp);

            System.out.println(res);

            Company company = userRepository.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String token = jwtUtil.generateToken(company.getMobile());

            return ResponseBuilder.success("OTP verified", HttpStatus.OK,
                    java.util.Map.of("token", token,"companyId", company.getCompanyId()));
        } catch (RuntimeException e) {
            return ResponseBuilder.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseBuilder.error("Something went wrong: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
