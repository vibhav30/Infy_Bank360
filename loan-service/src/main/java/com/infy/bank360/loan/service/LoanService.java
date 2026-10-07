package com.infy.bank360.loan.service;

import com.infy.bank360.loan.dto.LoanApplicationDTO;
import com.infy.bank360.loan.dto.LoanEmiCalculationDTO;
import com.infy.bank360.loan.dto.LoanStatusUpdateDTO;

import java.util.List;

public interface LoanService {
    LoanEmiCalculationDTO calculateEmi(LoanEmiCalculationDTO request);
    LoanApplicationDTO applyLoan(LoanApplicationDTO request);
    LoanApplicationDTO getLoanById(Long loanId);
    List<LoanApplicationDTO> getLoansByCustomerId(Long customerId);
    LoanApplicationDTO updateLoanStatus(Long loanId, LoanStatusUpdateDTO updateDTO);
    LoanApplicationDTO disburseLoan(Long loanId, String accountNumber);
}
