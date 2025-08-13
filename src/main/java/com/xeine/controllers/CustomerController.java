package com.xeine.controllers;

import com.xeine.dto.request.CustomerRequestDTO;
import com.xeine.dto.response.CustomerResponseDTO;
import com.xeine.services.CustomerService;
import com.xeine.utils.responsehandler.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/customers")
public class CustomerController {

    @Autowired
    CustomerService customerService;

    /**
     * Get all customers
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponseDTO>>> getAllCustomers() {
        List<CustomerResponseDTO> customers = customerService.getAllCustomers();
        ApiResponse<List<CustomerResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Customers fetched successfully",
                customers
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get customers by company ID
     */

    @GetMapping("/by-company/{companyId}")
    public ResponseEntity<ApiResponse<List<CustomerResponseDTO>>> getCustomersByCompany(@PathVariable Long companyId) {
        List<CustomerResponseDTO> customers = customerService.getCustomersByCompany(companyId);
        ApiResponse<List<CustomerResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Customers for company " + companyId + " fetched successfully",
                customers
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Create new customer with an optional image
     */
    @PostMapping( consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> createCustomer(
            @Valid @ModelAttribute CustomerRequestDTO customerRequestDTO) {

        System.out.println(customerRequestDTO.getCustomerImage());

        CustomerResponseDTO createdCustomer = customerService.createCustomer(customerRequestDTO);
        ApiResponse<CustomerResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.CREATED.value(),
                "Customer created successfully",
                createdCustomer
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update customer without image
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> updateCustomer(
            @PathVariable Long id,
            @RequestBody @Valid CustomerRequestDTO customerRequestDTO) {
        CustomerResponseDTO updatedCustomer = customerService.updateCustomer(id, customerRequestDTO);
        ApiResponse<CustomerResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Customer updated successfully",
                updatedCustomer
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Update customer with an optional image
     */
    @PutMapping(value = "/with-image/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> updateCustomerWithImage(
            @PathVariable Long id,
            @RequestPart @Valid CustomerRequestDTO customerRequestDTO,
            @RequestPart(required = false) MultipartFile imageFile) {
        CustomerResponseDTO updatedCustomer = customerService.updateCustomerWithImage(id, customerRequestDTO, imageFile);
        ApiResponse<CustomerResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Customer and image updated successfully",
                updatedCustomer
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Update customer image only
     */
    @PatchMapping(value = "/image/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> updateCustomerImage(
            @PathVariable Long id,
            @RequestPart MultipartFile imageFile) {
        CustomerResponseDTO updatedCustomer = customerService.updateCustomerImage(id, imageFile);
        ApiResponse<CustomerResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Customer image updated successfully",
                updatedCustomer
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Remove customer image
     */
    @DeleteMapping("/image/{id}")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> removeCustomerImage(@PathVariable Long id) {
        CustomerResponseDTO updatedCustomer = customerService.removeCustomerImage(id);
        ApiResponse<CustomerResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Customer image removed successfully",
                updatedCustomer
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Delete customer
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        ApiResponse<Void> response = new ApiResponse<>(
                true,
                HttpStatus.NO_CONTENT.value(),
                "Customer deleted successfully",
                null
        );
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }
}
