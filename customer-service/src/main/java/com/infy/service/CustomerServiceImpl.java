package com.infy.service;

import com.infy.dto.CustomerDTO;
import com.infy.entity.Customer;
import com.infy.exception.InfyBankException;
import com.infy.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Implementation: CustomerServiceImpl
 * Enforces customer profile invariants and onboarding KYC lifecycles.
 * Reference: spec.md (AC-CUST-01 to AC-CUST-04) & plan.md (Section 3)
 */
@Service(value = "customerService")
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Strict Constructor Injection — No field-level @Autowired.
     */
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public CustomerDTO registerCustomer(CustomerDTO customerDTO) throws InfyBankException {
        if (customerDTO == null) {
            throw new InfyBankException("ERR_INVALID_PAYLOAD", "Customer registration payload cannot be null.");
        }

        if (customerRepository.existsByEmail(customerDTO.getEmail())) {
            throw new InfyBankException("ERR_USER_EXISTS", "A customer with email " + customerDTO.getEmail() + " already exists.");
        }
        if (customerRepository.existsByMobileNumber(customerDTO.getMobileNumber())) {
            throw new InfyBankException("ERR_USER_EXISTS", "A customer with mobile number " + customerDTO.getMobileNumber() + " already exists.");
        }
        if (customerRepository.existsByNationalId(customerDTO.getNationalId())) {
            throw new InfyBankException("ERR_USER_EXISTS", "A customer with national ID " + customerDTO.getNationalId() + " already exists.");
        }

        Customer customer = CustomerDTO.dtoToEntity(customerDTO);
        customer.setKycStatus("PENDING_KYC");

        Customer savedCustomer = customerRepository.save(customer);
        return CustomerDTO.entityToDTO(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long customerId) throws InfyBankException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new InfyBankException("ERR_CUSTOMER_NOT_FOUND", "Customer not found with ID: " + customerId));
        return CustomerDTO.entityToDTO(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerByEmail(String email) throws InfyBankException {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new InfyBankException("ERR_CUSTOMER_NOT_FOUND", "Customer not found with email: " + email));
        return CustomerDTO.entityToDTO(customer);
    }

    @Override
    public CustomerDTO updateKycStatus(Long customerId, String kycStatus) throws InfyBankException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new InfyBankException("ERR_CUSTOMER_NOT_FOUND", "Customer not found with ID: " + customerId));

        if (kycStatus == null || !List.of("PENDING_KYC", "SUBMITTED_FOR_VERIFICATION", "KYC_VERIFIED", "KYC_REJECTED").contains(kycStatus)) {
            throw new InfyBankException("ERR_INVALID_KYC_STATUS", "Invalid KYC status transition: " + kycStatus);
        }

        customer.setKycStatus(kycStatus);
        Customer updatedCustomer = customerRepository.save(customer);
        return CustomerDTO.entityToDTO(updatedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isKycVerified(Long customerId) throws InfyBankException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new InfyBankException("ERR_CUSTOMER_NOT_FOUND", "Customer not found with ID: " + customerId));
        return "KYC_VERIFIED".equalsIgnoreCase(customer.getKycStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() throws InfyBankException {
        return customerRepository.findAll().stream()
                .map(CustomerDTO::entityToDTO)
                .collect(Collectors.toList());
    }
}
