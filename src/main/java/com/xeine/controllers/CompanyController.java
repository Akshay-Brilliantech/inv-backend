package com.xeine.controllers;

import com.xeine.dto.request.CompanyUpdateRequest;
import com.xeine.dto.response.CompanyResponseDTO;
import com.xeine.services.CompanyService;
import com.xeine.utils.responsehandler.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/companies")
@Validated
public class CompanyController {


    @Autowired
    private CompanyService companyService;


    @PutMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyResponseDTO>> updateCompany(
            @PathVariable @Min(1) Long companyId,
            @Valid @RequestBody CompanyUpdateRequest updateRequest) {

        CompanyResponseDTO updatedCompany = companyService.updateCompany(companyId, updateRequest);
        ApiResponse<CompanyResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Company updated successfully",
                updatedCompany
        );
        return ResponseEntity.ok(response);
    }


    @PutMapping(value = "/{companyId}/with-image", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<CompanyResponseDTO>> updateCompanyWithImage(
            @PathVariable @Min(1) Long companyId,
            @Valid  @ModelAttribute CompanyUpdateRequest updateRequest) {

        System.out.println("company request to update" + updateRequest);
        CompanyResponseDTO updatedCompany = companyService.updateCompany(companyId, updateRequest);
        ApiResponse<CompanyResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Company with image updated successfully",
                updatedCompany
        );
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{companyId}")
    public ResponseEntity<ApiResponse<String>> softDeleteCompany(
            @PathVariable @Min(1) Long companyId) {

        companyService.softDeleteCompany(companyId);
        ApiResponse<String> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Company soft deleted successfully"
        );
        return ResponseEntity.ok(response);
    }
}
