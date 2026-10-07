package com.infy.service;

import com.infy.bank360.common.dto.ApiResponse;
import com.infy.dto.AccountDTO;
import com.infy.entity.Account;
import com.infy.entity.Customer;
import com.infy.exception.InfyBankException;
import com.infy.feign.CustomerFeignClient;
import com.infy.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Implementation: AccountServiceImpl
 * Handles deposit account provisioning, status lifecycle, and atomic balance debit/credit operations.
 * Reference: spec.md (AC-ACCT-01 to AC-ACCT-05) & plan.md (Section 3)
 */
@Service(value = "accountService")
@Transactional
public class AccountServiceImpl implements AccountService {

    private static final BigDecimal MINIMUM_OPENING_DEPOSIT = new BigDecimal("100.00");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AccountRepository accountRepository;
    private final CustomerFeignClient customerFeignClient;

    /**
     * Strict Constructor Injection — No field-level @Autowired.
     */
    public AccountServiceImpl(
            AccountRepository accountRepository,
            CustomerFeignClient customerFeignClient) {
        this.accountRepository = accountRepository;
        this.customerFeignClient = customerFeignClient;
    }

    @Override
    public AccountDTO openAccount(AccountDTO accountDTO) throws InfyBankException {
        if (accountDTO == null) {
            throw new InfyBankException("ERR_INVALID_PAYLOAD", "Account payload cannot be null.");
        }

        // AC-ACCT-01: Inter-service KYC check via OpenFeign
        ApiResponse<String> kycResponse = customerFeignClient.getKycStatus(accountDTO.getCustomerId());
        String kycStatus = (kycResponse != null) ? kycResponse.getData() : null;

        if (!"KYC_VERIFIED".equalsIgnoreCase(kycStatus)) {
            throw new InfyBankException(
                "ERR_KYC_NOT_VERIFIED",
                "Customer KYC status is '" + kycStatus + "'. Account provisioning requires 'KYC_VERIFIED'."
            );
        }

        // AC-ACCT-03: Minimum opening deposit validation (>= 100.00)
        if (accountDTO.getBalance() == null || accountDTO.getBalance().compareTo(MINIMUM_OPENING_DEPOSIT) < 0) {
            throw new InfyBankException(
                "ERR_MINIMUM_DEPOSIT",
                "Opening deposit must be at least 100.00. Declared balance: " + accountDTO.getBalance()
            );
        }

        // AC-ACCT-02: Cryptographically unique 12-digit account number generation
        String accountNumber = accountDTO.getAccountNumber();
        if (accountNumber == null || accountNumber.isBlank()) {
            accountNumber = generateUniqueAccountNumber();
        } else if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new InfyBankException("ERR_ACCOUNT_EXISTS", "Account number already exists: " + accountNumber);
        }

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .customer(Customer.builder().customerId(accountDTO.getCustomerId()).build())
                .accountType(accountDTO.getAccountType())
                .balance(accountDTO.getBalance())
                .status("ACTIVE")
                .build();

        Account saved = accountRepository.save(account);
        return AccountDTO.entityToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTO getAccountByNumber(String accountNumber) throws InfyBankException {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new InfyBankException("ERR_ACCOUNT_NOT_FOUND", "Account not found: " + accountNumber));
        return AccountDTO.entityToDTO(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDTO> getAccountsByCustomerId(Long customerId) throws InfyBankException {
        return accountRepository.findByCustomerCustomerId(customerId).stream()
                .map(AccountDTO::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AccountDTO updateAccountStatus(String accountNumber, String status) throws InfyBankException {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new InfyBankException("ERR_ACCOUNT_NOT_FOUND", "Account not found: " + accountNumber));

        if (status == null || !List.of("ACTIVE", "FROZEN", "CLOSED").contains(status.toUpperCase())) {
            throw new InfyBankException("ERR_INVALID_STATUS", "Invalid account status: " + status);
        }

        account.setStatus(status.toUpperCase());
        Account updated = accountRepository.save(account);
        return AccountDTO.entityToDTO(updated);
    }

    @Override
    public AccountDTO debit(String accountNumber, BigDecimal amount) throws InfyBankException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InfyBankException("ERR_INVALID_AMOUNT", "Debit amount must be strictly greater than zero.");
        }

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new InfyBankException("ERR_ACCOUNT_NOT_FOUND", "Account not found: " + accountNumber));

        // AC-ACCT-05: Status checks
        if ("FROZEN".equalsIgnoreCase(account.getStatus())) {
            throw new InfyBankException("ERR_ACCOUNT_FROZEN", "Account " + accountNumber + " is FROZEN. Debits are strictly prohibited.");
        }
        if ("CLOSED".equalsIgnoreCase(account.getStatus())) {
            throw new InfyBankException("ERR_ACCOUNT_CLOSED", "Account " + accountNumber + " is CLOSED. Debits are prohibited.");
        }

        // AC-ACCT-05: Balance adequacy check
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InfyBankException(
                "ERR_INSUFFICIENT_BALANCE",
                "Insufficient funds in account " + accountNumber + ". Available: " + account.getBalance() + ", Requested: " + amount
            );
        }

        account.setBalance(account.getBalance().subtract(amount));
        Account updated = accountRepository.save(account);
        return AccountDTO.entityToDTO(updated);
    }

    @Override
    public AccountDTO credit(String accountNumber, BigDecimal amount) throws InfyBankException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InfyBankException("ERR_INVALID_AMOUNT", "Credit amount must be strictly greater than zero.");
        }

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new InfyBankException("ERR_ACCOUNT_NOT_FOUND", "Account not found: " + accountNumber));

        if ("CLOSED".equalsIgnoreCase(account.getStatus())) {
            throw new InfyBankException("ERR_ACCOUNT_CLOSED", "Cannot credit funds into closed account: " + accountNumber);
        }

        account.setBalance(account.getBalance().add(amount));
        Account updated = accountRepository.save(account);
        return AccountDTO.entityToDTO(updated);
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            long number = 100_000_000_000L + (long) (SECURE_RANDOM.nextDouble() * 900_000_000_000L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
