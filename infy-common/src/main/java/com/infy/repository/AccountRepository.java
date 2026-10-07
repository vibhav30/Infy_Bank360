package com.infy.repository;

import com.infy.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository: AccountRepository
 * Spring Data JPA repository for Account entity.
 * Reference: Tablescripts.sql (table: account) & spec.md (AC-ACCT-01 to AC-ACCT-04)
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerCustomerId(Long customerId);

    List<Account> findByCustomerCustomerIdAndStatus(Long customerId, String status);

    List<Account> findByStatus(String status);

    boolean existsByAccountNumber(String accountNumber);
}
