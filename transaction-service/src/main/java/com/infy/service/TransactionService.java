package com.infy.service;

import com.infy.dto.TransactionRecordDTO;
import com.infy.exception.InfyBankException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service Interface: TransactionService
 * Defines business operations for fund transfers, utility payments, discount engine, and ledger history.
 * Reference: spec.md (AC-TXN-01 to AC-TXN-04) & plan.md (Section 3)
 */
public interface TransactionService {

    BigDecimal calculateDiscount(BigDecimal amount) throws InfyBankException;

    TransactionRecordDTO transferFunds(TransactionRecordDTO transactionDTO) throws InfyBankException;

    TransactionRecordDTO payUtilityBill(TransactionRecordDTO transactionDTO) throws InfyBankException;

    List<TransactionRecordDTO> getTransactionHistory(String accountNumber) throws InfyBankException;
}
