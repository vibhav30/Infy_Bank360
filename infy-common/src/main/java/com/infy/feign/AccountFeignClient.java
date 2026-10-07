package com.infy.feign;

import com.infy.bank360.common.dto.ApiResponse;
import com.infy.bank360.common.exception.InfyBankException;
import com.infy.dto.AccountDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * Declarative OpenFeign client for communicating with account-service (Port 8200).
 * Decorated with Resilience4j @CircuitBreaker and default fallback methods.
 * Reference: spec.md (AC-TXN-03, AC-ACCT-05) & plan.md (Section 5)
 */
@FeignClient(name = "account-service")
public interface AccountFeignClient {

    @GetMapping("/infybank/accounts/{accountNumber}")
    @CircuitBreaker(name = "accountService", fallbackMethod = "getAccountByNumberFallback")
    ApiResponse<AccountDTO> getAccountByNumber(@PathVariable("accountNumber") String accountNumber);

    @PostMapping("/infybank/accounts/{accountNumber}/debit")
    @CircuitBreaker(name = "accountService", fallbackMethod = "debitFallback")
    ApiResponse<AccountDTO> debit(
        @PathVariable("accountNumber") String accountNumber,
        @RequestParam("amount") BigDecimal amount
    );

    @PostMapping("/infybank/accounts/{accountNumber}/credit")
    @CircuitBreaker(name = "accountService", fallbackMethod = "creditFallback")
    ApiResponse<AccountDTO> credit(
        @PathVariable("accountNumber") String accountNumber,
        @RequestParam("amount") BigDecimal amount
    );

    // -------------------------------------------------------------------------
    // Resilience4j Fallback Handlers
    // -------------------------------------------------------------------------

    default ApiResponse<AccountDTO> getAccountByNumberFallback(String accountNumber, Throwable throwable) {
        throw new InfyBankException(
            "ERR_ACCOUNT_SERVICE_UNAVAILABLE",
            "Account service is unreachable for account: " + accountNumber + ". Root cause: " + throwable.getMessage(),
            throwable
        );
    }

    default ApiResponse<AccountDTO> debitFallback(String accountNumber, BigDecimal amount, Throwable throwable) {
        throw new InfyBankException(
            "ERR_ACCOUNT_SERVICE_UNAVAILABLE",
            "Debit operation failed due to account service outage for account: " + accountNumber + ". Root cause: " + throwable.getMessage(),
            throwable
        );
    }

    default ApiResponse<AccountDTO> creditFallback(String accountNumber, BigDecimal amount, Throwable throwable) {
        throw new InfyBankException(
            "ERR_ACCOUNT_SERVICE_UNAVAILABLE",
            "Credit operation failed due to account service outage for account: " + accountNumber + ". Root cause: " + throwable.getMessage(),
            throwable
        );
    }
}
