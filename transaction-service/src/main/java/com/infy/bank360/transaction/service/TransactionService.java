package com.infy.bank360.transaction.service;

import com.infy.bank360.transaction.dto.BillPaymentRequestDTO;
import com.infy.bank360.transaction.dto.TransactionDTO;
import com.infy.bank360.transaction.dto.TransferRequestDTO;

import java.util.List;

public interface TransactionService {
    TransactionDTO transferFunds(TransferRequestDTO request);
    TransactionDTO payUtilityBill(BillPaymentRequestDTO request);
    List<TransactionDTO> getTransactionHistory(String accountNumber);
}
