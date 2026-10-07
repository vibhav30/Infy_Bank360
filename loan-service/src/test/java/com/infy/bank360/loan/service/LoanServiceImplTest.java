package com.infy.bank360.loan.service;

import com.infy.bank360.common.feign.AccountFeignClient;
import com.infy.bank360.loan.dto.LoanApplicationDTO;
import com.infy.bank360.loan.dto.LoanEmiCalculationDTO;
import com.infy.bank360.loan.entity.LoanApplication;
import com.infy.bank360.loan.repository.LoanApplicationRepository;
import com.infy.bank360.loan.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Loan Service Unit Tests (US-11, US-12, US-13)")
class LoanServiceImplTest {

    private LoanApplicationRepository loanRepository;
    private AccountFeignClient accountFeignClient;
    private LoanServiceImpl loanService;

    @BeforeEach
    void setUp() {
        loanRepository = Mockito.mock(LoanApplicationRepository.class);
        accountFeignClient = Mockito.mock(AccountFeignClient.class);
        loanService = new LoanServiceImpl(loanRepository, accountFeignClient);
    }

    @Test
    @DisplayName("US-11: Correctly calculates EMI for Personal Loan using standard formula")
    void testCalculateEmiFormula() {
        LoanEmiCalculationDTO request = LoanEmiCalculationDTO.builder()
                .principalAmount(new BigDecimal("10000.00"))
                .tenureMonths(12)
                .loanType("PERSONAL")
                .build();

        LoanEmiCalculationDTO result = loanService.calculateEmi(request);

        assertNotNull(result);
        assertNotNull(result.getMonthlyEmi());
        assertTrue(result.getMonthlyEmi().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(result.getTotalRepayment().compareTo(result.getPrincipalAmount()) > 0);
        assertEquals(0, result.getTotalInterest().compareTo(result.getTotalRepayment().subtract(result.getPrincipalAmount())));
    }

    @Test
    @DisplayName("US-12 & US-13: Flags DTI warning when EMI exceeds 50% of monthly income")
    void testDtiWarningWhenEmiExceedsHalfIncome() {
        // Declared monthly income is $500, but EMI for $50,000 over 12 months is high
        LoanApplicationDTO request = LoanApplicationDTO.builder()
                .customerId(1L)
                .loanType("PERSONAL")
                .principalAmount(new BigDecimal("50000.00"))
                .tenureMonths(12)
                .monthlyIncome(new BigDecimal("500.00"))
                .build();

        when(loanRepository.save(any(LoanApplication.class))).thenAnswer(invocation -> {
            LoanApplication app = invocation.getArgument(0);
            app.setLoanId(10L);
            return app;
        });

        LoanApplicationDTO result = loanService.applyLoan(request);

        assertNotNull(result);
        assertNotNull(result.getDtiWarning());
        assertTrue(result.getDtiWarning().contains("HIGH_DTI_RATIO"));
    }
}
