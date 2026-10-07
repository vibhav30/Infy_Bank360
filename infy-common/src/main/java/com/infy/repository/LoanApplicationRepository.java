package com.infy.repository;

import com.infy.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository: LoanApplicationRepository
 * Spring Data JPA repository for LoanApplication entity.
 * Reference: Tablescripts.sql (table: loan_application) & spec.md (AC-LOAN-01 to AC-LOAN-04)
 */
@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByCustomerCustomerId(Long customerId);

    List<LoanApplication> findByCustomerCustomerIdAndStatus(Long customerId, String status);

    List<LoanApplication> findByStatus(String status);

    List<LoanApplication> findByLoanType(String loanType);
}
