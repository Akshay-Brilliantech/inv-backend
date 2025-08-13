package com.xeine.services;

import com.xeine.dto.request.CompanyUpdateRequest;
import com.xeine.dto.response.CompanyResponseDTO;
import com.xeine.exception.CompanyNotFoundException;
import com.xeine.exception.DuplicateResourceException;
import com.xeine.models.Company;
import com.xeine.repository.CompanyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private SmsService smsService;

    @Autowired
    private ImageUploadService imageUploadService;

    public CompanyResponseDTO getCompanyById(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));

        return convertToResponseDTO(company);
    }

    private CompanyResponseDTO convertToResponseDTO(Company company) {
        CompanyResponseDTO dto = new CompanyResponseDTO();
        dto.setCompanyId(company.getCompanyId());
        dto.setMobile(company.getMobile());
        dto.setEmail(company.getEmail());
        dto.setCompanyName(company.getCompanyName());
        dto.setRole(company.getRole());
        dto.setGstNumber(company.getGstNumber());
        dto.setAddress(company.getAddress());
        dto.setCountry(company.getCountry());
        dto.setState(company.getState());
        dto.setCity(company.getCity());
        dto.setInvoiceCount(company.getInvoiceCount());
        dto.setCompanyImage(company.getCompany_Image());
        dto.setIsCompanyActive(company.getIsCompanyActive());
        dto.setIsOtpVerified(company.getIsOtpVerified());
        dto.setCreatedAt(company.getCreatedAt());
        dto.setUpdatedAt(company.getUpdatedAt());
        return dto;
    }

    @Transactional
    public CompanyResponseDTO updateCompany(Long companyId, CompanyUpdateRequest updateRequest) {
        log.info("Updating company with ID: {}", companyId);

        // Find existing active company
        Company existingCompany = companyRepository.findActiveById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Active company not found with ID: " + companyId));

        // Validate unique constraints only for fields that are being updated
        validateUniqueFieldsForUpdate(updateRequest, companyId);

        // Update only provided fields
        updateCompanyFields(existingCompany, updateRequest);

        // Handle company image update
        handleCompanyImageUpdate(existingCompany, updateRequest);

        // Set updated timestamp
        existingCompany.setUpdatedAt(LocalDateTime.now());

        // Save updated company
        Company updatedCompany = companyRepository.save(existingCompany);

        log.info("Successfully updated company with ID: {}", companyId);
        return convertToResponseDTO(updatedCompany);
    }
    private void handleCompanyImageUpdate(Company company, CompanyUpdateRequest request) {
        // Handle file upload
        if (request.getCompanyImageFile() != null && !request.getCompanyImageFile().isEmpty()) {
            // Delete old image if exists
            if (company.getCompany_Image() != null) {
                imageUploadService.deleteImage(company.getCompany_Image());
            }

            // Upload new image
            String imageUrl = imageUploadService.uploadImage(request.getCompanyImageFile(), "companies");
            company.setCompany_Image(imageUrl);
        }
        // Handle URL-based image
        else if (request.getCompanyImageUrl() != null && !request.getCompanyImageUrl().trim().isEmpty()) {
            company.setCompany_Image(request.getCompanyImageUrl().trim());
        }
    }

    private void validateUniqueFieldsForUpdate(CompanyUpdateRequest request, Long companyId) {
        // Only validate email if it's being updated (not null and not empty)
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty() &&
                companyRepository.existsByEmailAndNotCompanyIdAndActive(request.getEmail().trim(), companyId)) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        // Only validate mobile if it's being updated
        if (request.getMobile() != null && !request.getMobile().trim().isEmpty() &&
                companyRepository.existsByMobileAndNotCompanyIdAndActive(request.getMobile().trim(), companyId)) {
            throw new DuplicateResourceException("Mobile number already exists: " + request.getMobile());
        }

        // Only validate company name if it's being updated
        if (request.getCompanyName() != null && !request.getCompanyName().trim().isEmpty() &&
                companyRepository.existsByCompanyNameAndNotCompanyIdAndActive(request.getCompanyName().trim(), companyId)) {
            throw new DuplicateResourceException("Company name already exists: " + request.getCompanyName());
        }
    }

    private void updateCompanyFields(Company company, CompanyUpdateRequest request) {
        // Update mobile only if provided and not empty
        if (request.getMobile() != null && !request.getMobile().trim().isEmpty()) {
            company.setMobile(request.getMobile().trim());
        }

        // Update email only if provided and not empty
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            company.setEmail(request.getEmail().trim());
        }

        // Update company name only if provided and not empty
        if (request.getCompanyName() != null && !request.getCompanyName().trim().isEmpty()) {
            company.setCompanyName(request.getCompanyName().trim());
        }

        // Update GST number only if provided (can be empty to clear it)
        if (request.getGstNumber() != null) {
            company.setGstNumber(request.getGstNumber().trim().isEmpty() ? null : request.getGstNumber().trim());
        }




        // Update address only if provided and not empty
        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            company.setAddress(request.getAddress().trim());
        }

        // Update country only if provided and not empty
        if (request.getCountry() != null && !request.getCountry().trim().isEmpty()) {
            company.setCountry(request.getCountry().trim());
        }

        // Update state only if provided and not empty
        if (request.getState() != null && !request.getState().trim().isEmpty()) {
            company.setState(request.getState().trim());
        }

        // Update city only if provided and not empty
        if (request.getCity() != null && !request.getCity().trim().isEmpty()) {
            company.setCity(request.getCity().trim());
        }
    }


    //soft delete
    @Transactional
    public void softDeleteCompany(Long companyId) {
        log.info("Soft deleting company with ID: {}", companyId);

        Company company = companyRepository.findActiveById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Active company not found with ID: " + companyId));

        // Set company as inactive
        company.setIsCompanyActive(false);
        company.setUpdatedAt(LocalDateTime.now());

        // Save the change
        companyRepository.save(company);

        log.info("Successfully soft deleted company with ID: {}", companyId);
    }


    // Updated normalizeMobile method to handle quotes and other edge cases
    private String normalizeMobile(String mobile) {
        if (mobile == null) {
            return null;
        }

        // Convert to string and trim
        mobile = mobile.toString().trim();

        // Remove surrounding quotes (both single and double)
        if ((mobile.startsWith("\"") && mobile.endsWith("\"")) ||
                (mobile.startsWith("'") && mobile.endsWith("'"))) {
            mobile = mobile.substring(1, mobile.length() - 1);
        }

        // Remove decimal point if present (from integer conversion)
        if (mobile.endsWith(".0")) {
            mobile = mobile.substring(0, mobile.length() - 2);
        }

        // Trim again after quote removal
        mobile = mobile.trim();

        // Keep only digits and + sign for country code
        mobile = mobile.replaceAll("[^\\d+]", "");

        // Log for debugging (remove in production)
        log.debug("Mobile normalization result: '{}'", mobile);

        return mobile;
    }

    // Enhanced sendOtp method with better logging
    public String sendOtp(String mobile) {
        log.info("Raw mobile input: '{}'", mobile);

        // Normalize mobile number
        mobile = normalizeMobile(mobile);

        log.info("Normalized mobile: '{}' for OTP generation", mobile);



        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(1000000));

        // Find existing company or create new one
        Company company = companyRepository.findByMobile(mobile).orElse(null);

        if (company == null) {
            log.info("Creating new company for mobile: '{}'", mobile);
            company = new Company();
            company.setMobile(mobile);
            company.setCreatedAt(LocalDateTime.now());
            company.setIsCompanyActive(true);
        } else {
            log.info("Found existing company ID: {} for mobile: '{}'", company.getCompanyId(), mobile);
        }

        // Set OTP details
        company.setOtp(otp);
        company.setOtpGeneratedAt(LocalDateTime.now());
        company.setIsOtpVerified(false);
        company.setUpdatedAt(LocalDateTime.now());

        // Save company
        Company savedCompany = companyRepository.save(company);
        log.info("OTP generated and saved for company ID: {} with mobile: '{}'",
                savedCompany.getCompanyId(), savedCompany.getMobile());

        try {
            // Send OTP via SMS
            smsService.sendSms(mobile, "Your OTP is: " + otp);
            log.info("OTP sent successfully to mobile: '{}'", mobile);
            return "OTP sent successfully";
        } catch (Exception e) {
            log.error("Failed to send OTP to mobile: '{}'. Error: {}", mobile, e.getMessage());
            throw new RuntimeException("Failed to send OTP. Please try again.");
        }
    }

    // Enhanced verifyOtp method with better logging
    public String verifyOtp(String mobile, String otp) {
        log.info("Raw mobile input for verification: '{}'", mobile);
        log.info("Raw OTP input: '{}'", otp);

        // Normalize inputs
        mobile = normalizeMobile(mobile);
        otp = otp != null ? otp.trim() : null;

        log.info("Normalized mobile for verification: '{}'", mobile);
        log.info("Trimmed OTP: '{}'", otp);

        validateInputs(mobile, otp);

        // Debug: Check what's actually in the database
        List<Company> allCompanies = companyRepository.findAll();
        log.info("Debug - All companies in database:");
        for (Company c : allCompanies) {
            log.info("Company ID: {}, Mobile: '{}', Mobile Length: {}",
                    c.getCompanyId(), c.getMobile(), c.getMobile() != null ? c.getMobile().length() : "null");
        }

        String finalMobile = mobile;
        Company company = companyRepository.findByMobile(mobile)
                .orElseThrow(() -> {
                    log.error("Company not found for mobile: '{}'. Available mobiles: {}",
                            finalMobile, allCompanies.stream()
                                    .map(c -> "'" + c.getMobile() + "'")
                                    .collect(Collectors.toList()));
                    return new RuntimeException("Mobile number not found: " + finalMobile);
                });

        log.info("Found company ID: {} for mobile: '{}', OTP status: {}",
                company.getCompanyId(), company.getMobile(), company.getIsOtpVerified());

        // Validate OTP state
        validateOtpState(company);

        // Compare OTPs
        String storedOtp = company.getOtp().trim();
        log.info("OTP comparison - Stored: '{}' (length: {}), Provided: '{}' (length: {})",
                storedOtp, storedOtp.length(), otp, otp.length());

        if (storedOtp.equals(otp)) {
            log.info("OTP verification successful for company ID: {}", company.getCompanyId());

            // Success
            company.setIsOtpVerified(true);
            company.setOtp(null);
            company.setOtpGeneratedAt(null);

            company.setUpdatedAt(LocalDateTime.now());
            companyRepository.save(company);

            return "OTP verified successfully";
        }

        log.error("Invalid OTP for company ID: {}", company.getCompanyId());
        throw new RuntimeException("Invalid OTP. Please check and try again.");
    }

    private void validateInputs(String mobile, String otp) {
        if (mobile == null || mobile.isEmpty()) {
            throw new IllegalArgumentException("Valid mobile number is required");
        }
        if (otp == null || otp.isEmpty()) {
            throw new IllegalArgumentException("OTP is required");
        }
        if (otp.length() != 6) {
            throw new IllegalArgumentException("OTP must be 6 digits");
        }
    }

    private void validateOtpState(Company company) {
        if (company.getOtp() == null) {
            throw new RuntimeException("No active OTP found. Please request a new OTP.");
        }

        if (company.getOtpGeneratedAt() == null) {
            throw new RuntimeException("Invalid OTP state. Please request a new OTP.");
        }

        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(5);
        if (company.getOtpGeneratedAt().isBefore(expiryTime)) {
            log.info("Expired OTP for company ID: {}", company.getCompanyId());
            // Clean up expired OTP
            company.setOtp(null);
            company.setOtpGeneratedAt(null);

            company.setUpdatedAt(LocalDateTime.now());
            companyRepository.save(company);
            throw new RuntimeException("OTP has expired. Please request a new OTP.");
        }
    }




    /*public String sendOtp(String mobile) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        Company company = companyRepository.findByMobile(mobile)
                .orElse(new Company());
        company.setMobile(mobile);
        company.setOtp(otp);
        company.setOtpGeneratedAt(LocalDateTime.now());
        company.setIsOtpVerified(false);
        companyRepository.save(company);

        // Send OTP via Twilio SMS
        smsService.sendSms(mobile, "Your OTP is: " + otp);
        return "OTP sent successfully";
    }*/

    /*public String verifyOtp(String mobile, String otp) {
        Company company = companyRepository.findByMobile(mobile)
                .orElseThrow(() -> new RuntimeException("Mobile not found"));
        if (company.getOtp().equals(otp) &&
                company.getOtpGeneratedAt().isAfter(LocalDateTime.now().minusMinutes(5))) {
            company.setIsOtpVerified(true);
            company.setOtp(null);
            company.setOtpGeneratedAt(null);
            companyRepository.save(company);
            return "OTP verified";
        }
        throw new RuntimeException("Invalid or expired OTP");
    }*/



}
