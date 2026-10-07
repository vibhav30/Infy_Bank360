package com.infy.service;

import com.infy.dto.CustomerDTO;
import com.infy.exception.InfyBankException;

import java.util.List;

/**
 * Service Interface: CustomerService
 * Defines business operations for Customer and KYC management.
 * Reference: spec.md (AC-CUST-01 to AC-CUST-04) & plan.md (Section 3)
 */
public interface CustomerService {

    CustomerDTO registerCustomer(CustomerDTO customerDTO) throws InfyBankException;

    CustomerDTO getCustomerById(Long customerId) throws InfyBankException;

    CustomerDTO getCustomerByEmail(String email) throws InfyBankException;

    CustomerDTO updateKycStatus(Long customerId, String kycStatus) throws InfyBankException;

    boolean isKycVerified(Long customerId) throws InfyBankException;

    List<CustomerDTO> getAllCustomers() throws InfyBankException;
}
