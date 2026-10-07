package com.infy.bank360.account.service;

import com.infy.bank360.account.entity.Account;
import com.infy.bank360.account.repository.AccountRepository;
import com.infy.bank360.account.service.impl.AccountServiceImpl;
import com.infy.bank360.common.dto.BalanceOperationDTO;
import com.infy.bank360.common.exception.InfyBankException;
import com.infy.bank360.common.feign.CustomerFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("Account Service Unit Tests (BR-05, BR-06, US-04, US-05, US-06)")
class AccountServiceImplTest {

    private AccountRepository accountRepository;
    private CustomerFeignClient customerFeignClient;
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        accountRepository = Mockito.mock(AccountRepository.class);
        customerFeignClient = Mockito.mock(CustomerFeignClient.class);
        accountService = new AccountServiceImpl(accountRepository, customerFeignClient);
    }

    @Test
    @DisplayName("BR-06: Frozen account debit must be blocked with ERR_ACCOUNT_FROZEN")
    void testFrozenAccountDebitBlocked() {
        Account frozenAccount = Account.builder()
                .accountNumber("100000000006")
                .customerId(3L)
                .accountType("SAVINGS")
                .balance(new BigDecimal("500.00"))
                .status("FROZEN")
                .build();

        when(accountRepository.findByAccountNumber("100000000006")).thenReturn(Optional.of(frozenAccount));

        BalanceOperationDTO debitOp = BalanceOperationDTO.builder()
                .amount(new BigDecimal("50.00"))
                .build();

        InfyBankException ex = assertThrows(InfyBankException.class, () ->
                accountService.debit("100000000006", debitOp));

        assertEquals("ERR_ACCOUNT_FROZEN", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("FROZEN"));
    }

    @Test
    @DisplayName("BR-05: Insufficient balance debit must throw ERR_INSUFFICIENT_FUNDS")
    void testInsufficientBalanceDebitThrowsException() {
        Account activeAccount = Account.builder()
                .accountNumber("100000000001")
                .customerId(1L)
                .accountType("SAVINGS")
                .balance(new BigDecimal("100.00"))
                .status("ACTIVE")
                .build();

        when(accountRepository.findByAccountNumber("100000000001")).thenReturn(Optional.of(activeAccount));

        BalanceOperationDTO debitOp = BalanceOperationDTO.builder()
                .amount(new BigDecimal("200.00"))
                .build();

        InfyBankException ex = assertThrows(InfyBankException.class, () ->
                accountService.debit("100000000001", debitOp));

        assertEquals("ERR_INSUFFICIENT_FUNDS", ex.getErrorCode());
    }
}
