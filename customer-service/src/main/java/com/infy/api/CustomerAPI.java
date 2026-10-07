package com.infy.api;

import com.infy.bank360.common.dto.ApiResponse;
import com.infy.dto.CustomerDTO;
import com.infy.exception.InfyBankException;
import com.infy.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller: CustomerAPI
 * Exposes endpoints for Customer registration, KYC triage, and profile lookups.
 * Base Path: /infybank/customers/**
 * Reference: spec.md (AC-CUST-01 to AC-CUST-04) & plan.md (Section 3)
 */
@RestController
@Validated
@RequestMapping(value = "/infybank")
public class CustomerAPI {

    private final CustomerService customerService;
    private final Environment environment;

    /**
     * Strict Constructor Injection for Service collaborator and Spring Environment.
     */
    public CustomerAPI(CustomerService customerService, Environment environment) {
        this.customerService = customerService;
        this.environment = environment;
    }

    @PostMapping("/customers")
    public ResponseEntity<CustomerDTO> registerCustomer(@Valid @RequestBody CustomerDTO customerDTO) throws InfyBankException {
        CustomerDTO result = customerService.registerCustomer(customerDTO);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long customerId) throws InfyBankException {
        CustomerDTO result = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/customers/email/{email}")
    public ResponseEntity<CustomerDTO> getCustomerByEmail(@PathVariable String email) throws InfyBankException {
        CustomerDTO result = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/customers/{customerId}/kyc")
    public ResponseEntity<CustomerDTO> updateKycStatus(
            @PathVariable Long customerId,
            @RequestParam String kycStatus) throws InfyBankException {
        CustomerDTO result = customerService.updateKycStatus(customerId, kycStatus);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/customers/{customerId}/kyc-status")
    public ResponseEntity<ApiResponse<String>> getKycStatus(@PathVariable Long customerId) throws InfyBankException {
        CustomerDTO result = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(ApiResponse.success(result.getKycStatus(), "KYC status retrieved."));
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() throws InfyBankException {
        List<CustomerDTO> result = customerService.getAllCustomers();
        return ResponseEntity.ok(result);
    }
}
