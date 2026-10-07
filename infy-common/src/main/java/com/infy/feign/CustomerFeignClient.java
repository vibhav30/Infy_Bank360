package com.infy.feign;

import com.infy.bank360.common.dto.ApiResponse;
import com.infy.bank360.common.exception.InfyBankException;
import com.infy.dto.CustomerDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Declarative OpenFeign client for communicating with customer-service (Port 8100).
 * Decorated with Resilience4j @CircuitBreaker and default fallback methods.
 * Reference: spec.md (AC-ACCT-01) & plan.md (Section 5)
 */
@FeignClient(name = "customer-service")
public interface CustomerFeignClient {

    @GetMapping("/infybank/customers/{customerId}")
    @CircuitBreaker(name = "customerService", fallbackMethod = "getCustomerByIdFallback")
    ApiResponse<CustomerDTO> getCustomerById(@PathVariable("customerId") Long customerId);

    @GetMapping("/infybank/customers/{customerId}/kyc-status")
    @CircuitBreaker(name = "customerService", fallbackMethod = "getKycStatusFallback")
    ApiResponse<String> getKycStatus(@PathVariable("customerId") Long customerId);

    // -------------------------------------------------------------------------
    // Resilience4j Fallback Handlers
    // -------------------------------------------------------------------------

    default ApiResponse<CustomerDTO> getCustomerByIdFallback(Long customerId, Throwable throwable) {
        throw new InfyBankException(
            "ERR_CUSTOMER_SERVICE_UNAVAILABLE",
            "Customer service is temporarily unreachable for ID: " + customerId + ". Root cause: " + throwable.getMessage(),
            throwable
        );
    }

    default ApiResponse<String> getKycStatusFallback(Long customerId, Throwable throwable) {
        throw new InfyBankException(
            "ERR_CUSTOMER_SERVICE_UNAVAILABLE",
            "Unable to verify customer KYC status due to service outage for ID: " + customerId + ". Root cause: " + throwable.getMessage(),
            throwable
        );
    }
}
