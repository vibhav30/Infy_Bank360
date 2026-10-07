package com.infy.bank360.transaction.controller;

import com.infy.bank360.common.dto.ApiResponse;
import com.infy.bank360.transaction.dto.BillPaymentRequestDTO;
import com.infy.bank360.transaction.dto.TransactionDTO;
import com.infy.bank360.transaction.dto.TransferRequestDTO;
import com.infy.bank360.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/infybank/transactions")
@Validated
public class TransactionAPI {

    private final TransactionService transactionService;

    public TransactionAPI(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionDTO>> transferFunds(@Valid @RequestBody TransferRequestDTO request) {
        TransactionDTO result = transactionService.transferFunds(request);
        return new ResponseEntity<>(ApiResponse.success(result, "Transfer executed successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/bill-pay")
    public ResponseEntity<ApiResponse<TransactionDTO>> payUtilityBill(@Valid @RequestBody BillPaymentRequestDTO request) {
        TransactionDTO result = transactionService.payUtilityBill(request);
        return new ResponseEntity<>(ApiResponse.success(result, "Utility bill paid successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getStatement(@PathVariable String accountNumber) {
        List<TransactionDTO> statement = transactionService.getTransactionHistory(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(statement, "Account transaction history retrieved"));
    }
}
