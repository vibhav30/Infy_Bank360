package com.infy.service;

import com.infy.dto.AccountDTO;
import com.infy.exception.InfyBankException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service Interface: AccountService
 * Defines business operations for Account lifecycle, balance queries, and debit/credit mutations.
 * Reference: spec.md (AC-ACCT-01 to AC-ACCT-05) & plan.md (Section 3)
 */
public interface AccountService {

    AccountDTO openAccount(AccountDTO accountDTO) throws InfyBankException;

    AccountDTO getAccountByNumber(String accountNumber) throws InfyBankException;

    List<AccountDTO> getAccountsByCustomerId(Long customerId) throws InfyBankException;

    AccountDTO updateAccountStatus(String accountNumber, String status) throws InfyBankException;

    AccountDTO debit(String accountNumber, BigDecimal amount) throws InfyBankException;

    AccountDTO credit(String accountNumber, BigDecimal amount) throws InfyBankException;
}
