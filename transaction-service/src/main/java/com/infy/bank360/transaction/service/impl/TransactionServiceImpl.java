package com.infy.bank360.transaction.service.impl;

import com.infy.bank360.common.dto.BalanceOperationDTO;
import com.infy.bank360.common.dto.HighValueTransactionDTO;
import com.infy.bank360.common.dto.NotificationDTO;
import com.infy.bank360.common.exception.InfyBankException;
import com.infy.bank360.common.feign.AccountFeignClient;
import com.infy.bank360.common.feign.AuditFeignClient;
import com.infy.bank360.transaction.dto.BillPaymentRequestDTO;
import com.infy.bank360.transaction.dto.DiscountCalculationResult;
import com.infy.bank360.transaction.dto.TransactionDTO;
import com.infy.bank360.transaction.dto.TransferRequestDTO;
import com.infy.bank360.transaction.entity.TransactionRecord;
import com.infy.bank360.transaction.repository.TransactionRecordRepository;
import com.infy.bank360.transaction.service.PromotionAndValidationService;
import com.infy.bank360.transaction.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private static final BigDecimal AML_THRESHOLD = new BigDecimal("10000.00");
    private final TransactionRecordRepository transactionRepository;
    private final PromotionAndValidationService promotionAndValidationService;
    private final AccountFeignClient accountFeignClient;
    private final AuditFeignClient auditFeignClient;
    private final SecureRandom secureRandom = new SecureRandom();

    public TransactionServiceImpl(
            TransactionRecordRepository transactionRepository,
            PromotionAndValidationService promotionAndValidationService,
            AccountFeignClient accountFeignClient,
            AuditFeignClient auditFeignClient) {
        this.transactionRepository = transactionRepository;
        this.promotionAndValidationService = promotionAndValidationService;
        this.accountFeignClient = accountFeignClient;
        this.auditFeignClient = auditFeignClient;
    }

    @Override
    public TransactionDTO transferFunds(TransferRequestDTO request) {
        // 1. Prohibit self transfer
        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new InfyBankException("ERR_SELF_TRANSFER_PROHIBITED", "Source and destination account numbers must be different.");
        }

        // 2. Validate amount & compute promotional discount (> 500 Rule)
        DiscountCalculationResult calc = promotionAndValidationService.evaluateAmountAndDiscount(request.getAmount());

        // 3. Debit source account with netAmount
        BalanceOperationDTO debitOp = BalanceOperationDTO.builder()
                .amount(calc.getNetAmount())
                .remarks("Transfer to " + request.getDestinationAccountNumber() + (calc.getDiscount().compareTo(BigDecimal.ZERO) > 0 ? " (5% Discount applied: -$" + calc.getDiscount() + ")" : ""))
                .transactionReference("TXN-" + System.currentTimeMillis())
                .build();

        try {
            accountFeignClient.executeDebit(request.getSourceAccountNumber(), debitOp);
        } catch (Exception e) {
            throw new InfyBankException("ERR_DEBIT_FAILED", "Failed to debit source account: " + e.getMessage());
        }

        // 4. Credit destination account with full gross amount
        BalanceOperationDTO creditOp = BalanceOperationDTO.builder()
                .amount(calc.getGrossAmount())
                .remarks("Received transfer from " + request.getSourceAccountNumber())
                .transactionReference(debitOp.getTransactionReference())
                .build();

        try {
            accountFeignClient.executeCredit(request.getDestinationAccountNumber(), creditOp);
        } catch (Exception e) {
            // Compensating transaction: refund source account
            try {
                BalanceOperationDTO refundOp = BalanceOperationDTO.builder()
                        .amount(calc.getNetAmount())
                        .remarks("Compensating refund for failed transfer")
                        .transactionReference("REFUND-" + System.currentTimeMillis())
                        .build();
                accountFeignClient.executeCredit(request.getSourceAccountNumber(), refundOp);
            } catch (Exception refundEx) {
                // Log refund error
            }
            throw new InfyBankException("ERR_CREDIT_FAILED", "Failed to credit destination account. Debited funds were compensated: " + e.getMessage());
        }

        // 5. Persist transaction record
        String status = "SUCCESS";
        if (request.getAmount().compareTo(AML_THRESHOLD) >= 0) {
            status = "COMPLIANCE_FLAGGED";
        }

        TransactionRecord record = TransactionRecord.builder()
                .sourceAccountNumber(request.getSourceAccountNumber())
                .destinationAccountNumber(request.getDestinationAccountNumber())
                .grossAmount(calc.getGrossAmount())
                .discount(calc.getDiscount())
                .netAmount(calc.getNetAmount())
                .transactionType("TRANSFER")
                .transactionDate(LocalDateTime.now())
                .status(status)
                .referenceNumber(debitOp.getTransactionReference())
                .build();

        TransactionRecord saved = transactionRepository.save(record);

        // 6. Asynchronous compliance flag if AML threshold met (BR-07)
        if (request.getAmount().compareTo(AML_THRESHOLD) >= 0) {
            try {
                HighValueTransactionDTO flagDTO = HighValueTransactionDTO.builder()
                        .transactionId(saved.getTransactionId())
                        .sourceAccount(request.getSourceAccountNumber())
                        .destinationAccount(request.getDestinationAccountNumber())
                        .amount(request.getAmount())
                        .flagReason("Transaction amount $" + request.getAmount() + " exceeded $10,000 threshold")
                        .build();
                auditFeignClient.flagHighValueTransaction(flagDTO);
            } catch (Exception ignored) {}
        }

        // 7. Event-driven notification (US-19)
        try {
            NotificationDTO notif = NotificationDTO.builder()
                    .customerId(1L)
                    .title("Fund Transfer Successful")
                    .message("Transferred $" + calc.getGrossAmount() + " to " + request.getDestinationAccountNumber() + ". Net debited: $" + calc.getNetAmount())
                    .type("DEBIT")
                    .build();
            auditFeignClient.sendNotification(notif);
        } catch (Exception ignored) {}

        return mapToDTO(saved);
    }

    @Override
    public TransactionDTO payUtilityBill(BillPaymentRequestDTO request) {
        // 1. Validate amount & compute promotional discount (> 500 Rule)
        DiscountCalculationResult calc = promotionAndValidationService.evaluateAmountAndDiscount(request.getAmount());

        String receiptRef = "TXN-BILL-" + (10000 + secureRandom.nextInt(90000));

        // 2. Debit source account with netAmount
        BalanceOperationDTO debitOp = BalanceOperationDTO.builder()
                .amount(calc.getNetAmount())
                .remarks("Bill Payment: " + request.getBillerCategory() + " Ref: " + request.getBillReferenceNumber())
                .transactionReference(receiptRef)
                .build();

        try {
            accountFeignClient.executeDebit(request.getAccountNumber(), debitOp);
        } catch (Exception e) {
            throw new InfyBankException("ERR_DEBIT_FAILED", "Failed to debit account for bill payment: " + e.getMessage());
        }

        // 3. Persist transaction record
        TransactionRecord record = TransactionRecord.builder()
                .sourceAccountNumber(request.getAccountNumber())
                .destinationAccountNumber(null)
                .grossAmount(calc.getGrossAmount())
                .discount(calc.getDiscount())
                .netAmount(calc.getNetAmount())
                .transactionType("UTILITY_BILL")
                .transactionDate(LocalDateTime.now())
                .status("SUCCESS")
                .referenceNumber(receiptRef)
                .billerCategory(request.getBillerCategory())
                .build();

        TransactionRecord saved = transactionRepository.save(record);

        // 4. Send notification if discount applied (US-19)
        try {
            String msg = "Utility bill paid! $" + calc.getNetAmount() + " debited for " + request.getBillerCategory();
            if (calc.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                msg += " with promotional discount of $" + calc.getDiscount() + " applied!";
            }
            NotificationDTO notif = NotificationDTO.builder()
                    .customerId(1L)
                    .title("Bill Payment Successful")
                    .message(msg)
                    .type("DISCOUNT")
                    .build();
            auditFeignClient.sendNotification(notif);
        } catch (Exception ignored) {}

        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionHistory(String accountNumber) {
        return transactionRepository.findBySourceAccountNumberOrDestinationAccountNumberOrderByTransactionDateDesc(accountNumber, accountNumber)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private TransactionDTO mapToDTO(TransactionRecord t) {
        return TransactionDTO.builder()
                .transactionId(t.getTransactionId())
                .sourceAccountNumber(t.getSourceAccountNumber())
                .destinationAccountNumber(t.getDestinationAccountNumber())
                .grossAmount(t.getGrossAmount())
                .discount(t.getDiscount())
                .netAmount(t.getNetAmount())
                .transactionType(t.getTransactionType())
                .transactionDate(t.getTransactionDate())
                .status(t.getStatus())
                .referenceNumber(t.getReferenceNumber())
                .billerCategory(t.getBillerCategory())
                .build();
    }
}
