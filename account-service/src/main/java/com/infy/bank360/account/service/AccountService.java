package com.infy.bank360.account.service;

import com.infy.bank360.account.dto.AccountCreateDTO;
import com.infy.bank360.account.dto.AccountDTO;
import com.infy.bank360.account.dto.AccountStatusUpdateDTO;
import com.infy.bank360.common.dto.BalanceOperationDTO;

import java.util.List;

public interface AccountService {
    AccountDTO createAccount(AccountCreateDTO dto);
    AccountDTO getAccountByNumber(String accountNumber);
    List<AccountDTO> getAccountsByCustomerId(Long customerId);
    AccountDTO updateAccountStatus(String accountNumber, AccountStatusUpdateDTO dto);
    AccountDTO debit(String accountNumber, BalanceOperationDTO dto);
    AccountDTO credit(String accountNumber, BalanceOperationDTO dto);
}
