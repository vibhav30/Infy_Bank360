package com.infy.bank360.loan.service.impl;

import com.infy.bank360.common.dto.BalanceOperationDTO;
import com.infy.bank360.common.exception.InfyBankException;
import com.infy.bank360.common.feign.AccountFeignClient;
import com.infy.bank360.loan.dto.LoanApplicationDTO;
import com.infy.bank360.loan.dto.LoanEmiCalculationDTO;
import com.infy.bank360.loan.dto.LoanStatusUpdateDTO;
import com.infy.bank360.loan.entity.LoanApplication;
import com.infy.bank360.loan.repository.LoanApplicationRepository;
import com.infy.bank360.loan.service.LoanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LoanServiceImpl implements LoanService {

    private final LoanApplicationRepository loanRepository;
    private final AccountFeignClient accountFeignClient;

    public LoanServiceImpl(LoanApplicationRepository loanRepository,
                           AccountFeignClient accountFeignClient) {
        this.loanRepository = loanRepository;
        this.accountFeignClient = accountFeignClient;
    }

    private double resolveInterestRate(String loanType) {
        if ("HOME".equalsIgnoreCase(loanType)) {
            return 8.50;
        } else if ("VEHICLE".equalsIgnoreCase(loanType)) {
            return 9.75;
        } else {
            return 11.50; // PERSONAL
        }
    }

    /**
     * Standard Reducing Balance EMI formula:
     * EMI = [P * r * (1 + r)^n] / [(1 + r)^n - 1]
     */
    private BigDecimal computeMonthlyEmi(BigDecimal principal, double annualRate, int tenureMonths) {
        double p = principal.doubleValue();
        double r = (annualRate / 12.0) / 100.0;
        int n = tenureMonths;

        double numerator = p * r * Math.pow(1.0 + r, n);
        double denominator = Math.pow(1.0 + r, n) - 1.0;
        double emi = numerator / denominator;

        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public LoanEmiCalculationDTO calculateEmi(LoanEmiCalculationDTO request) {
        double annualRate = request.getAnnualInterestRate() != null 
                ? request.getAnnualInterestRate() 
                : resolveInterestRate(request.getLoanType());

        BigDecimal monthlyEmi = computeMonthlyEmi(request.getPrincipalAmount(), annualRate, request.getTenureMonths());
        BigDecimal totalRepayment = monthlyEmi.multiply(BigDecimal.valueOf(request.getTenureMonths())).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalInterest = totalRepayment.subtract(request.getPrincipalAmount()).setScale(2, RoundingMode.HALF_UP);

        request.setAnnualInterestRate(annualRate);
        request.setMonthlyEmi(monthlyEmi);
        request.setTotalRepayment(totalRepayment);
        request.setTotalInterest(totalInterest);
        return request;
    }

    @Override
    public LoanApplicationDTO applyLoan(LoanApplicationDTO request) {
        double rate = resolveInterestRate(request.getLoanType());
        BigDecimal emi = computeMonthlyEmi(request.getPrincipalAmount(), rate, request.getTenureMonths());

        // Underwriting rule: Check Debt-to-Income (DTI) ratio
        BigDecimal maxAllowedEmi = request.getMonthlyIncome().multiply(new BigDecimal("0.50"));
        String dtiWarning = null;
        if (emi.compareTo(maxAllowedEmi) > 0) {
            dtiWarning = "HIGH_DTI_RATIO: Estimated EMI ($" + emi + ") exceeds 50% of declared monthly income ($" + request.getMonthlyIncome() + ").";
        }

        LoanApplication loan = LoanApplication.builder()
                .customerId(request.getCustomerId())
                .accountNumber(request.getAccountNumber())
                .loanType(request.getLoanType().toUpperCase())
                .principalAmount(request.getPrincipalAmount())
                .tenureMonths(request.getTenureMonths())
                .annualInterestRate(rate)
                .monthlyEmi(emi)
                .monthlyIncome(request.getMonthlyIncome())
                .status("APPLIED")
                .build();

        LoanApplication saved = loanRepository.save(loan);
        LoanApplicationDTO dto = mapToDTO(saved);
        dto.setDtiWarning(dtiWarning);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationDTO getLoanById(Long loanId) {
        LoanApplication loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new InfyBankException("ERR_LOAN_NOT_FOUND", "Loan application not found with ID: " + loanId));
        return mapToDTO(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationDTO> getLoansByCustomerId(Long customerId) {
        return loanRepository.findByCustomerId(customerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LoanApplicationDTO updateLoanStatus(Long loanId, LoanStatusUpdateDTO updateDTO) {
        LoanApplication loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new InfyBankException("ERR_LOAN_NOT_FOUND", "Loan application not found with ID: " + loanId));

        if ("REJECTED".equalsIgnoreCase(updateDTO.getStatus()) && (updateDTO.getRemarks() == null || updateDTO.getRemarks().isBlank())) {
            throw new InfyBankException("ERR_REJECTION_REASON_REQUIRED", "Rejection remarks are mandatory when rejecting a loan.");
        }

        loan.setStatus(updateDTO.getStatus().toUpperCase());
        loan.setRejectionRemarks(updateDTO.getRemarks());
        LoanApplication saved = loanRepository.save(loan);
        return mapToDTO(saved);
    }

    @Override
    public LoanApplicationDTO disburseLoan(Long loanId, String accountNumber) {
        LoanApplication loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new InfyBankException("ERR_LOAN_NOT_FOUND", "Loan application not found with ID: " + loanId));

        if (!"APPROVED".equalsIgnoreCase(loan.getStatus())) {
            throw new InfyBankException("ERR_LOAN_NOT_APPROVED", "Cannot disburse loan. Current status is " + loan.getStatus() + ", must be APPROVED.");
        }

        String targetAccount = accountNumber != null ? accountNumber : loan.getAccountNumber();
        if (targetAccount == null || targetAccount.isBlank()) {
            throw new InfyBankException("ERR_TARGET_ACCOUNT_MISSING", "Disbursement target account number is required.");
        }

        // Credit the borrower's account via Feign
        BalanceOperationDTO creditOp = BalanceOperationDTO.builder()
                .amount(loan.getPrincipalAmount())
                .remarks("Loan Disbursal for " + loan.getLoanType() + " Loan #" + loan.getLoanId())
                .transactionReference("DISBURSE-" + loan.getLoanId())
                .build();

        try {
            accountFeignClient.executeCredit(targetAccount, creditOp);
        } catch (Exception e) {
            throw new InfyBankException("ERR_DISBURSEMENT_FAILED", "Failed to credit loan amount to account " + targetAccount + ": " + e.getMessage());
        }

        loan.setStatus("DISBURSED");
        loan.setAccountNumber(targetAccount);
        LoanApplication saved = loanRepository.save(loan);
        return mapToDTO(saved);
    }

    private LoanApplicationDTO mapToDTO(LoanApplication l) {
        return LoanApplicationDTO.builder()
                .loanId(l.getLoanId())
                .customerId(l.getCustomerId())
                .accountNumber(l.getAccountNumber())
                .loanType(l.getLoanType())
                .principalAmount(l.getPrincipalAmount())
                .tenureMonths(l.getTenureMonths())
                .annualInterestRate(l.getAnnualInterestRate())
                .monthlyEmi(l.getMonthlyEmi())
                .monthlyIncome(l.getMonthlyIncome())
                .status(l.getStatus())
                .rejectionRemarks(l.getRejectionRemarks())
                .build();
    }
}
