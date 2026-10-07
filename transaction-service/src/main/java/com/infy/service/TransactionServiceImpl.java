package com.infy.service;

import com.infy.dto.TransactionRecordDTO;
import com.infy.entity.Account;
import com.infy.entity.TransactionRecord;
import com.infy.exception.InfyBankException;
import com.infy.feign.AccountFeignClient;
import com.infy.repository.TransactionRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Implementation: TransactionServiceImpl
 * Enforces atomic intra-bank transfers, promotional discount calculations, and audit ledgers.
 * Reference: spec.md (AC-TXN-01 to AC-TXN-04) & plan.md (Section 3)
 */
@Service(value = "transactionService")
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("500.00");
    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("0.05"); // 5% Discount

    private final TransactionRecordRepository transactionRecordRepository;
    private final AccountFeignClient accountFeignClient;

    /**
     * Strict Constructor Injection — No field-level @Autowired.
     */
    public TransactionServiceImpl(
            TransactionRecordRepository transactionRecordRepository,
            AccountFeignClient accountFeignClient) {
        this.transactionRecordRepository = transactionRecordRepository;
        this.accountFeignClient = accountFeignClient;
    }

    /**
     * Calculates promotional discount: 5% when amount > 500.00.
     * Throws InfyBankException if amount <= 0.00 or null.
     */
    @Override
    public BigDecimal calculateDiscount(BigDecimal amount) throws InfyBankException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InfyBankException(
                "ERR_INVALID_TRANSACTION_AMOUNT",
                "Transaction amount must be strictly greater than zero."
            );
        }

        if (amount.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return amount.multiply(DISCOUNT_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Executes atomic fund transfer between two distinct accounts.
     */
    @Override
    public TransactionRecordDTO transferFunds(TransactionRecordDTO dto) throws InfyBankException {
        if (dto == null) {
            throw new InfyBankException("ERR_INVALID_PAYLOAD", "Transaction payload cannot be null.");
        }

        String sourceAcc = dto.getSourceAccountNumber();
        String destAcc = dto.getDestinationAccountNumber();

        if (sourceAcc == null || sourceAcc.isBlank() || destAcc == null || destAcc.isBlank()) {
            throw new InfyBankException("ERR_MISSING_ACCOUNT", "Source and destination account numbers are mandatory.");
        }

        if (sourceAcc.equalsIgnoreCase(destAcc)) {
            throw new InfyBankException("ERR_SELF_TRANSFER_PROHIBITED", "Source and destination accounts cannot be identical.");
        }

        // 1. Strict validation and 5% discount calculation (> 500 Rule)
        BigDecimal grossAmount = dto.getGrossAmount();
        BigDecimal discount = calculateDiscount(grossAmount);
        BigDecimal netAmount = grossAmount.subtract(discount).setScale(2, RoundingMode.HALF_UP);

        // 2. Inter-service debit on source account via OpenFeign
        accountFeignClient.debit(sourceAcc, netAmount);

        // 3. Inter-service credit on destination account via OpenFeign (with compensating rollback)
        try {
            accountFeignClient.credit(destAcc, grossAmount);
        } catch (Exception ex) {
            try {
                accountFeignClient.credit(sourceAcc, netAmount);
            } catch (Exception compEx) {
                throw new InfyBankException(
                    "ERR_COMPENSATION_FAILED",
                    "Critical transfer error: Debit succeeded but credit and compensation failed. " + compEx.getMessage(),
                    compEx
                );
            }
            throw new InfyBankException(
                "ERR_TRANSFER_CREDIT_FAILED",
                "Transfer credit failed. Source account has been refunded. Reason: " + ex.getMessage(),
                ex
            );
        }

        // 4. Double-entry ledger persistence
        TransactionRecord record = TransactionRecord.builder()
                .sourceAccount(Account.builder().accountNumber(sourceAcc).build())
                .destinationAccount(Account.builder().accountNumber(destAcc).build())
                .grossAmount(grossAmount)
                .discount(discount)
                .netAmount(netAmount)
                .transactionType("TRANSFER")
                .status("SUCCESS")
                .transactionDate(LocalDateTime.now())
                .build();

        TransactionRecord saved = transactionRecordRepository.save(record);
        return TransactionRecordDTO.entityToDTO(saved);
    }

    /**
     * Executes utility bill payment with promotional discount.
     */
    @Override
    public TransactionRecordDTO payUtilityBill(TransactionRecordDTO dto) throws InfyBankException {
        if (dto == null) {
            throw new InfyBankException("ERR_INVALID_PAYLOAD", "Bill payment payload cannot be null.");
        }

        String sourceAcc = dto.getSourceAccountNumber();
        if (sourceAcc == null || sourceAcc.isBlank()) {
            throw new InfyBankException("ERR_MISSING_ACCOUNT", "Source account number is mandatory for bill payment.");
        }

        BigDecimal grossAmount = dto.getGrossAmount();
        BigDecimal discount = calculateDiscount(grossAmount);
        BigDecimal netAmount = grossAmount.subtract(discount).setScale(2, RoundingMode.HALF_UP);

        // Inter-service debit on customer account
        accountFeignClient.debit(sourceAcc, netAmount);

        // Persist utility payment ledger entry
        TransactionRecord record = TransactionRecord.builder()
                .sourceAccount(Account.builder().accountNumber(sourceAcc).build())
                .destinationAccount(null)
                .grossAmount(grossAmount)
                .discount(discount)
                .netAmount(netAmount)
                .transactionType("UTILITY_BILL")
                .status("SUCCESS")
                .transactionDate(LocalDateTime.now())
                .build();

        TransactionRecord saved = transactionRecordRepository.save(record);
        return TransactionRecordDTO.entityToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionRecordDTO> getTransactionHistory(String accountNumber) throws InfyBankException {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new InfyBankException("ERR_INVALID_ACCOUNT", "Account number cannot be blank.");
        }

        return transactionRecordRepository.findAllByAccountNumberOrderByTransactionDateDesc(accountNumber).stream()
                .map(TransactionRecordDTO::entityToDTO)
                .collect(Collectors.toList());
    }
}
