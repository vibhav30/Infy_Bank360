package com.infy.bank360.customer.controller;

import com.infy.bank360.common.dto.ApiResponse;
import com.infy.bank360.customer.dto.CustomerDTO;
import com.infy.bank360.customer.dto.DocumentUploadDTO;
import com.infy.bank360.customer.dto.KycVerificationDTO;
import com.infy.bank360.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/infybank/customers")
@Validated
public class CustomerAPI {

    private final CustomerService customerService;

    public CustomerAPI(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDTO>> registerCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        CustomerDTO created = customerService.createCustomer(customerDTO);
        return new ResponseEntity<>(ApiResponse.success(created, "Customer registered successfully with status PENDING_KYC"), HttpStatus.CREATED);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomer(@PathVariable Long customerId) {
        CustomerDTO customer = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(ApiResponse.success(customer, "Customer retrieved successfully"));
    }

    @PostMapping("/{customerId}/documents")
    public ResponseEntity<ApiResponse<CustomerDTO>> uploadKycDocuments(
            @PathVariable Long customerId,
            @Valid @RequestBody DocumentUploadDTO uploadDTO) {
        CustomerDTO updated = customerService.uploadDocuments(customerId, uploadDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "KYC documents uploaded successfully; submitted for verification"));
    }

    @PutMapping("/{customerId}/kyc")
    public ResponseEntity<ApiResponse<CustomerDTO>> updateKyc(
            @PathVariable Long customerId,
            @RequestBody CustomerDTO customerDTO) {
        CustomerDTO updated = customerService.updateKycStatus(customerId, customerDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Customer KYC details updated"));
    }

    @PutMapping("/{customerId}/kyc/verify")
    public ResponseEntity<ApiResponse<CustomerDTO>> verifyKyc(
            @PathVariable Long customerId,
            @Valid @RequestBody KycVerificationDTO verificationDTO) {
        CustomerDTO updated = customerService.verifyKyc(customerId, verificationDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "KYC status transitioned to " + updated.getKycStatus()));
    }

    @GetMapping("/pending-kyc")
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> getPendingKycQueue() {
        List<CustomerDTO> list = customerService.getPendingKycCustomers();
        return ResponseEntity.ok(ApiResponse.success(list, "Pending KYC verification queue retrieved"));
    }
}
