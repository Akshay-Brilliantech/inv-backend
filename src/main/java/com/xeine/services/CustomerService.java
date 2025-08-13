package com.xeine.services;

import com.xeine.dto.request.CustomerRequestDTO;
import com.xeine.dto.response.CustomerResponseDTO;
import com.xeine.exception.CompanyNotFoundException;
import com.xeine.exception.CustomerNotFoundException;
import com.xeine.exception.DuplicateResourceException;
import com.xeine.models.Company;
import com.xeine.models.Customer;
import com.xeine.repository.CompanyRepository;
import com.xeine.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {



    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    /**
     * Get all customers
     */
    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get customers by company ID
     */
    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> getCustomersByCompany(Long companyId) {

        // Verify company exists
        if (!companyRepository.existsById(companyId)) {
            throw new CompanyNotFoundException("Company not found with ID: " + companyId);
        }

        return customerRepository.findByCompanyCompanyId(companyId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get customer by ID
     */
//    @Transactional(readOnly = true)
//    public CustomerResponseDTO getCustomerById(Long id) {
//        Customer customer = customerRepository.findById(id)
//                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));
//        return toDTO(customer);
//    }

    /**
     * Create new customer
     */
    public CustomerResponseDTO createCustomer(CustomerRequestDTO dto) {

        // Validate unique constraints
        validateUniqueConstraints(dto.getEmail(), dto.getMobile(), null);

        // Verify company exists
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + dto.getCompanyId()));

        Customer customer = new Customer();
        customer.setCustomerName(dto.getCustomerName());
        customer.setEmail(dto.getEmail());
        customer.setMobile(dto.getMobile());
        customer.setAddress(dto.getAddress());
        customer.setGstNumber(dto.getGstNumber());
        customer.setCustomerType(dto.getCustomerType());
        customer.setCompany(company);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());


//        if (dto.getCustomerImage() == null) {
//            System.out.println("🔴 imageFile is NULL - check Postman request");
//        } else {
//            System.out.println("🔵 imageFile.isEmpty(): " + imageFile.isEmpty());
//            System.out.println("🔵 imageFile.getOriginalFilename(): " + imageFile.getOriginalFilename());
//            System.out.println("🔵 imageFile.getSize(): " + imageFile.getSize());
//        }

        // Upload image if provided
        if (dto.getCustomerImage() != null) {
            try {
                System.out.println("🟢 Entering image upload block");

                String imageUrl = imageUploadService.uploadImage(dto.getCustomerImage(), "customers");


                customer.setCustomerImage(imageUrl);

            } catch (Exception e) {
                System.out.println("🔴 Exception in image upload: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to upload customer image: " + e.getMessage());
            }
        } else {
            System.out.println("🟡 Skipping image upload");
        }

        System.out.println("🔵 Before save - customer.getCustomerImage(): '" + customer.getCustomerImage() + "'");

        Customer savedCustomer = customerRepository.save(customer);

        return toDTO(savedCustomer);
    }
    /**
     * Update customer without image
     */
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO dto) {


        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        // Validate unique constraints (excluding current customer)
        validateUniqueConstraints(dto.getEmail(), dto.getMobile(), id);

        // Verify company exists
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + dto.getCompanyId()));

        // Update customer fields
        customer.setCustomerName(dto.getCustomerName());
        customer.setEmail(dto.getEmail());
        customer.setMobile(dto.getMobile());
        customer.setAddress(dto.getAddress());
        customer.setGstNumber(dto.getGstNumber());
        customer.setCustomerType(dto.getCustomerType());
        customer.setCompany(company);
        customer.setUpdatedAt(LocalDateTime.now());

        Customer updatedCustomer = customerRepository.save(customer);


        return toDTO(updatedCustomer);
    }

    /**
     * Update customer with image
     */
    public CustomerResponseDTO updateCustomerWithImage(Long id, CustomerRequestDTO dto, MultipartFile imageFile) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        // Validate unique constraints (excluding current customer)
        validateUniqueConstraints(dto.getEmail(), dto.getMobile(), id);

        // Verify company exists
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + dto.getCompanyId()));

        // Handle image update
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Delete old image if exists
                if (customer.getCustomerImage() != null && !customer.getCustomerImage().trim().isEmpty()) {
                    imageUploadService.deleteImage(customer.getCustomerImage());

                }

                // Upload new image
                String newImageUrl = imageUploadService.uploadImage(imageFile, "customers");
                customer.setCustomerImage(newImageUrl);


            } catch (Exception e) {
                throw new RuntimeException("Failed to update customer image: " + e.getMessage());
            }
        }

        // Update customer fields
        customer.setCustomerName(dto.getCustomerName());
        customer.setEmail(dto.getEmail());
        customer.setMobile(dto.getMobile());
        customer.setAddress(dto.getAddress());
        customer.setGstNumber(dto.getGstNumber());
        customer.setCustomerType(dto.getCustomerType());
        customer.setCompany(company);
        customer.setUpdatedAt(LocalDateTime.now());

        Customer updatedCustomer = customerRepository.save(customer);


        return toDTO(updatedCustomer);
    }

    /**
     * Update customer image only
     */
    public CustomerResponseDTO updateCustomerImage(Long id, MultipartFile imageFile) {


        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        try {
            // Delete old image if exists
            if (customer.getCustomerImage() != null && !customer.getCustomerImage().trim().isEmpty()) {
                imageUploadService.deleteImage(customer.getCustomerImage());

            }

            // Upload new image
            String newImageUrl = imageUploadService.uploadImage(imageFile, "customers");
            customer.setCustomerImage(newImageUrl);
            customer.setUpdatedAt(LocalDateTime.now());

            Customer updatedCustomer = customerRepository.save(customer);


            return toDTO(updatedCustomer);

        } catch (Exception e) {

            throw new RuntimeException("Failed to update customer image: " + e.getMessage());
        }
    }

    /**
     * Remove customer image
     */
    public CustomerResponseDTO removeCustomerImage(Long id) {


        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        // Delete image if exists
        if (customer.getCustomerImage() != null && !customer.getCustomerImage().trim().isEmpty()) {
            try {
                imageUploadService.deleteImage(customer.getCustomerImage());

            } catch (Exception e) {

                // Continue with database update even if file deletion fails
            }

            customer.setCustomerImage(null);
            customer.setUpdatedAt(LocalDateTime.now());

            Customer updatedCustomer = customerRepository.save(customer);


            return toDTO(updatedCustomer);
        } else {

            return toDTO(customer);
        }
    }

    /**
     * Delete customer
     */
    public void deleteCustomer(Long id) {


        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        // Delete customer image if exists
        if (customer.getCustomerImage() != null && !customer.getCustomerImage().trim().isEmpty()) {
            try {
                imageUploadService.deleteImage(customer.getCustomerImage());

            } catch (Exception e) {

                // Continue with customer deletion even if image deletion fails
            }
        }

        customerRepository.delete(customer);

    }

    /**
     * Search customers by name or email
     */
//    @Transactional(readOnly = true)
//    public List<CustomerResponseDTO> searchCustomers(String searchTerm) {
//
//
//        if (searchTerm == null || searchTerm.trim().isEmpty()) {
//            return getAllCustomers();
//        }
//
//        String searchPattern = "%" + searchTerm.toLowerCase().trim() + "%";
//        return customerRepository.findByCustomerNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
//                        searchTerm.trim(), searchTerm.trim())
//                .stream()
//                .map(this::toDTO)
//                .collect(Collectors.toList());
//    }

    /**
     * Check if customer exists by email
     */
//    @Transactional(readOnly = true)
//    public boolean existsByEmail(String email) {
//        return customerRepository.existsByEmail(email);
//    }

    /**
     * Check if customer exists by mobile
     */
//    @Transactional(readOnly = true)
//    public boolean existsByMobile(String mobile) {
//        return customerRepository.existsByMobile(mobile);
//    }

    /**
     * Get customer count by company
     */
//    @Transactional(readOnly = true)
//    public long getCustomerCountByCompany(Long companyId) {
//        return customerRepository.countByCompanyCompanyId(companyId);
//    }

    /**
     * Validate unique constraints
     */
    private void validateUniqueConstraints(String email, String mobile, Long excludeCustomerId) {
        // Check email uniqueness
        Optional<Customer> existingByEmail = customerRepository.findByEmail(email);
        if (existingByEmail.isPresent() &&
                (excludeCustomerId == null || !existingByEmail.get().getCustomerId().equals(excludeCustomerId))) {
            throw new DuplicateResourceException("Customer with email '" + email + "' already exists");
        }

        // Check mobile uniqueness
        Optional<Customer> existingByMobile = customerRepository.findByMobile(mobile);
        if (existingByMobile.isPresent() &&
                (excludeCustomerId == null || !existingByMobile.get().getCustomerId().equals(excludeCustomerId))) {
            throw new DuplicateResourceException("Customer with mobile '" + mobile + "' already exists");
        }
    }

    /**
     * Convert Customer entity to DTO
     */
    private CustomerResponseDTO toDTO(Customer customer) {

        System.out.println("🔵 Input customer.getCustomerImage(): '" + customer.getCustomerImage() + "'");

        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setCustomerId(customer.getCustomerId());
        dto.setCustomerName(customer.getCustomerName());
        dto.setEmail(customer.getEmail());
        dto.setMobile(customer.getMobile());
        dto.setAddress(customer.getAddress());
        dto.setGstNumber(customer.getGstNumber());
        dto.setCustomerType(customer.getCustomerType());
        dto.setCustomerImage(customer.getCustomerImage());


        System.out.println("🔵 Mapping customer image: " + customer.getCustomerImage() +
                " to DTO: " + dto.getCustomerImage());


        if (customer.getCompany() != null) {
            dto.setCompanyId(customer.getCompany().getCompanyId());
            dto.setCompanyName(customer.getCompany().getCompanyName());
        }

        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());
        return dto;
    }
}
