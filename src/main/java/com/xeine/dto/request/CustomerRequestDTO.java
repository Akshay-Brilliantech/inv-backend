package com.xeine.dto.request;


import com.xeine.enums.CustomerType;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CustomerRequestDTO {
    private String customerName;
    private String email;
    private String mobile;
    private String address;
    private String gstNumber;
    private String customerImageUrl;
    private CustomerType customerType;
    private MultipartFile customerImage;
    private Long companyId;


}
