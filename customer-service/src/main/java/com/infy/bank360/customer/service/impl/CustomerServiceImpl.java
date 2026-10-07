package com.infy.bank360.customer.service.impl;

import com.infy.bank360.common.exception.InfyBankException;
import com.infy.bank360.customer.dto.CustomerDTO;
import com.infy.bank360.customer.dto.DocumentUploadDTO;
import com.infy.bank360.customer.dto.KycVerificationDTO;
import com.infy.bank360.customer.entity.Customer;
import com.infy.bank360.customer.repository.CustomerRepository;
import com.infy.bank360.customer.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public CustomerDTO createCustomer(CustomerDTO dto) {
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new InfyBankException("ERR_USER_EXISTS", "Email already registered: " + dto.getEmail());
        }
        if (customerRepository.existsByNationalId(dto.getNationalId())) {
            throw new InfyBankException("ERR_USER_EXISTS", "National ID already registered: " + dto.getNationalId());
        }
        if (customerRepository.existsByMobileNumber(dto.getMobileNumber())) {
            throw new InfyBankException("ERR_USER_EXISTS", "Mobile number already registered: " + dto.getMobileNumber());
        }

        Customer customer = Customer.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .mobileNumber(dto.getMobileNumber())
                .nationalId(dto.getNationalId())
                .kycStatus("PENDING_KYC")
                .build();

        Customer saved = customerRepository.save(customer);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new InfyBankException("ERR_USER_NOT_FOUND", "Customer not found with ID: " + customerId));
        return mapToDTO(customer);
    }

    @Override
    public CustomerDTO uploadDocuments(Long customerId, DocumentUploadDTO dto) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new InfyBankException("ERR_USER_NOT_FOUND", "Customer not found with ID: " + customerId));

        customer.setIdDocumentType(dto.getIdDocumentType());
        customer.setIdDocumentUrl(dto.getIdDocumentUrl());
        customer.setKycStatus("SUBMITTED_FOR_VERIFICATION");

        Customer updated = customerRepository.save(customer);
        return mapToDTO(updated);
    }

    @Override
    public CustomerDTO verifyKyc(Long customerId, KycVerificationDTO dto) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new InfyBankException("ERR_USER_NOT_FOUND", "Customer not found with ID: " + customerId));

        String decision = dto.getStatus();
        if ("KYC_VERIFIED".equalsIgnoreCase(decision)) {
            customer.setKycStatus("KYC_VERIFIED");
            customer.setRejectionReason(null);
        } else if ("KYC_REJECTED".equalsIgnoreCase(decision)) {
            if (dto.getRemarks() == null || dto.getRemarks().trim().isEmpty()) {
                throw new InfyBankException("ERR_MANDATORY_REJECTION_REASON", "Rejection remarks are mandatory when rejecting KYC.");
            }
            customer.setKycStatus("KYC_REJECTED");
            customer.setRejectionReason(dto.getRemarks());
        } else {
            throw new InfyBankException("ERR_INVALID_KYC_STATUS", "Invalid KYC decision status: " + decision);
        }

        Customer updated = customerRepository.save(customer);
        return mapToDTO(updated);
    }

    @Override
    public CustomerDTO updateKycStatus(Long customerId, CustomerDTO dto) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new InfyBankException("ERR_USER_NOT_FOUND", "Customer not found with ID: " + customerId));

        if (dto.getKycStatus() != null) {
            customer.setKycStatus(dto.getKycStatus());
        }
        if (dto.getRejectionReason() != null) {
            customer.setRejectionReason(dto.getRejectionReason());
        }

        Customer updated = customerRepository.save(customer);
        return mapToDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> getPendingKycCustomers() {
        return customerRepository.findByKycStatus("SUBMITTED_FOR_VERIFICATION").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CustomerDTO mapToDTO(Customer c) {
        return CustomerDTO.builder()
                .customerId(c.getCustomerId())
                .fullName(c.getFullName())
                .email(c.getEmail())
                .mobileNumber(c.getMobileNumber())
                .nationalId(c.getNationalId())
                .kycStatus(c.getKycStatus())
                .rejectionReason(c.getRejectionReason())
                .idDocumentType(c.getIdDocumentType())
                .idDocumentUrl(c.getIdDocumentUrl())
                .build();
    }
}
