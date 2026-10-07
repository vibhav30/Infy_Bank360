package com.infy.bank360.customer.service;

import com.infy.bank360.customer.dto.CustomerDTO;
import com.infy.bank360.customer.dto.DocumentUploadDTO;
import com.infy.bank360.customer.dto.KycVerificationDTO;

import java.util.List;

public interface CustomerService {
    CustomerDTO createCustomer(CustomerDTO dto);
    CustomerDTO getCustomerById(Long customerId);
    CustomerDTO uploadDocuments(Long customerId, DocumentUploadDTO dto);
    CustomerDTO verifyKyc(Long customerId, KycVerificationDTO dto);
    CustomerDTO updateKycStatus(Long customerId, CustomerDTO dto);
    List<CustomerDTO> getPendingKycCustomers();
}
