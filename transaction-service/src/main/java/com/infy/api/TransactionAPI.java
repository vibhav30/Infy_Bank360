package com.infy.api;

import com.infy.dto.TransactionRecordDTO;
import com.infy.exception.InfyBankException;
import com.infy.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller: TransactionAPI
 * Exposes endpoints for intra-bank fund transfers, utility payments, and account statement lookups.
 * Base Path: /infybank/transactions/**
 * Reference: spec.md (AC-TXN-01 to AC-TXN-04) & plan.md (Section 3)
 */
@RestController
@Validated
@RequestMapping(value = "/infybank")
public class TransactionAPI {

    private final TransactionService transactionService;
    private final Environment environment;

    /**
     * Strict Constructor Injection for Service collaborator and Spring Environment.
     */
    public TransactionAPI(TransactionService transactionService, Environment environment) {
        this.transactionService = transactionService;
        this.environment = environment;
    }

    @PostMapping("/transactions/transfer")
    public ResponseEntity<TransactionRecordDTO> transferFunds(
            @Valid @RequestBody TransactionRecordDTO transactionDTO) throws InfyBankException {
        TransactionRecordDTO result = transactionService.transferFunds(transactionDTO);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PostMapping("/transactions/bill-pay")
    public ResponseEntity<TransactionRecordDTO> payUtilityBill(
            @Valid @RequestBody TransactionRecordDTO transactionDTO) throws InfyBankException {
        TransactionRecordDTO result = transactionService.payUtilityBill(transactionDTO);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/transactions/{accountNumber}")
    public ResponseEntity<List<TransactionRecordDTO>> getTransactionHistory(
            @PathVariable String accountNumber) throws InfyBankException {
        List<TransactionRecordDTO> result = transactionService.getTransactionHistory(accountNumber);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/transactions/discount")
    public ResponseEntity<BigDecimal> calculateDiscount(
            @RequestParam BigDecimal amount) throws InfyBankException {
        BigDecimal discount = transactionService.calculateDiscount(amount);
        return ResponseEntity.ok(discount);
    }
}
