package com.infy.api;

import com.infy.dto.AccountDTO;
import com.infy.exception.InfyBankException;
import com.infy.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller: AccountAPI
 * Exposes endpoints for opening deposit accounts, status transitions, balance inquiries, and debit/credit mutations.
 * Base Path: /infybank/accounts/**
 * Reference: spec.md (AC-ACCT-01 to AC-ACCT-05) & plan.md (Section 3)
 */
@RestController
@Validated
@RequestMapping(value = "/infybank")
public class AccountAPI {

    private final AccountService accountService;
    private final Environment environment;

    /**
     * Strict Constructor Injection for Service collaborator and Spring Environment.
     */
    public AccountAPI(AccountService accountService, Environment environment) {
        this.accountService = accountService;
        this.environment = environment;
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountDTO> openAccount(@Valid @RequestBody AccountDTO accountDTO) throws InfyBankException {
        AccountDTO result = accountService.openAccount(accountDTO);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<AccountDTO> getAccountByNumber(@PathVariable String accountNumber) throws InfyBankException {
        AccountDTO result = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/accounts/customer/{customerId}")
    public ResponseEntity<List<AccountDTO>> getAccountsByCustomerId(@PathVariable Long customerId) throws InfyBankException {
        List<AccountDTO> result = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/accounts/{accountNumber}/status")
    public ResponseEntity<AccountDTO> updateAccountStatus(
            @PathVariable String accountNumber,
            @RequestParam String status) throws InfyBankException {
        AccountDTO result = accountService.updateAccountStatus(accountNumber, status);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/accounts/{accountNumber}/debit")
    public ResponseEntity<AccountDTO> debit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) throws InfyBankException {
        AccountDTO result = accountService.debit(accountNumber, amount);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/accounts/{accountNumber}/credit")
    public ResponseEntity<AccountDTO> credit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) throws InfyBankException {
        AccountDTO result = accountService.credit(accountNumber, amount);
        return ResponseEntity.ok(result);
    }
}
