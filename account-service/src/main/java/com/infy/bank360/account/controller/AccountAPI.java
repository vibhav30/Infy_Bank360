package com.infy.bank360.account.controller;

import com.infy.bank360.account.dto.AccountCreateDTO;
import com.infy.bank360.account.dto.AccountDTO;
import com.infy.bank360.account.dto.AccountStatusUpdateDTO;
import com.infy.bank360.account.service.AccountService;
import com.infy.bank360.common.dto.ApiResponse;
import com.infy.bank360.common.dto.BalanceOperationDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/infybank/accounts")
@Validated
public class AccountAPI {

    private final AccountService accountService;

    public AccountAPI(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountDTO>> openAccount(@Valid @RequestBody AccountCreateDTO createDTO) {
        AccountDTO account = accountService.createAccount(createDTO);
        return new ResponseEntity<>(ApiResponse.success(account, "Account opened successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountDTO>> getAccount(@PathVariable String accountNumber) {
        AccountDTO account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(account, "Account retrieved successfully"));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getAccountsByCustomer(@PathVariable Long customerId) {
        List<AccountDTO> accounts = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(accounts, "Customer accounts retrieved"));
    }

    @PutMapping("/{accountNumber}/status")
    public ResponseEntity<ApiResponse<AccountDTO>> updateStatus(
            @PathVariable String accountNumber,
            @Valid @RequestBody AccountStatusUpdateDTO statusDTO) {
        AccountDTO updated = accountService.updateAccountStatus(accountNumber, statusDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Account status updated to " + updated.getStatus()));
    }

    @PostMapping("/{accountNumber}/debit")
    public ResponseEntity<ApiResponse<AccountDTO>> debit(
            @PathVariable String accountNumber,
            @Valid @RequestBody BalanceOperationDTO debitDTO) {
        AccountDTO account = accountService.debit(accountNumber, debitDTO);
        return ResponseEntity.ok(ApiResponse.success(account, "Account debited successfully"));
    }

    @PostMapping("/{accountNumber}/credit")
    public ResponseEntity<ApiResponse<AccountDTO>> credit(
            @PathVariable String accountNumber,
            @Valid @RequestBody BalanceOperationDTO creditDTO) {
        AccountDTO account = accountService.credit(accountNumber, creditDTO);
        return ResponseEntity.ok(ApiResponse.success(account, "Account credited successfully"));
    }
}
