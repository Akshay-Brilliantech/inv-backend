package com.xeine.dto.request;

import com.xeine.enums.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;



@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class CompanyUpdateRequest {

    @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Invalid mobile number format")
    private String mobile;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String companyName;

    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
            message = "Invalid GST number format")
    private String gstNumber;

    private BusinessType businessType;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    private String companyImageUrl;
    private MultipartFile companyImageFile;
}
