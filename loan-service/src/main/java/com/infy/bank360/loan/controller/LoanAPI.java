package com.infy.bank360.loan.controller;

import com.infy.bank360.common.dto.ApiResponse;
import com.infy.bank360.loan.dto.LoanApplicationDTO;
import com.infy.bank360.loan.dto.LoanEmiCalculationDTO;
import com.infy.bank360.loan.dto.LoanStatusUpdateDTO;
import com.infy.bank360.loan.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/infybank/loans")
@Validated
public class LoanAPI {

    private final LoanService loanService;

    public LoanAPI(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/calculate-emi")
    public ResponseEntity<ApiResponse<LoanEmiCalculationDTO>> calculateEmi(@Valid @RequestBody LoanEmiCalculationDTO request) {
        LoanEmiCalculationDTO result = loanService.calculateEmi(request);
        return ResponseEntity.ok(ApiResponse.success(result, "EMI calculated successfully"));
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<LoanApplicationDTO>> applyLoan(@Valid @RequestBody LoanApplicationDTO request) {
        LoanApplicationDTO result = loanService.applyLoan(request);
        return new ResponseEntity<>(ApiResponse.success(result, "Loan application submitted successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<ApiResponse<LoanApplicationDTO>> getLoan(@PathVariable Long loanId) {
        LoanApplicationDTO result = loanService.getLoanById(loanId);
        return ResponseEntity.ok(ApiResponse.success(result, "Loan application retrieved"));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<LoanApplicationDTO>>> getLoansByCustomer(@PathVariable Long customerId) {
        List<LoanApplicationDTO> list = loanService.getLoansByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(list, "Customer loans retrieved"));
    }

    @PutMapping("/{loanId}/status")
    public ResponseEntity<ApiResponse<LoanApplicationDTO>> updateLoanStatus(
            @PathVariable Long loanId,
            @Valid @RequestBody LoanStatusUpdateDTO updateDTO) {
        LoanApplicationDTO result = loanService.updateLoanStatus(loanId, updateDTO);
        return ResponseEntity.ok(ApiResponse.success(result, "Loan application status updated to " + result.getStatus()));
    }

    @PostMapping("/{loanId}/disburse")
    public ResponseEntity<ApiResponse<LoanApplicationDTO>> disburseLoan(
            @PathVariable Long loanId,
            @RequestParam(required = false) String accountNumber) {
        LoanApplicationDTO result = loanService.disburseLoan(loanId, accountNumber);
        return ResponseEntity.ok(ApiResponse.success(result, "Loan disbursed successfully to account"));
    }
}
