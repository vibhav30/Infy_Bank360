package com.infy.repository;

import com.infy.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository: CustomerRepository
 * Spring Data JPA repository for Customer entity.
 * Reference: Tablescripts.sql (table: customer) & spec.md (AC-CUST-01 to AC-CUST-04)
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByMobileNumber(String mobileNumber);

    Optional<Customer> findByNationalId(String nationalId);

    List<Customer> findByKycStatus(String kycStatus);

    boolean existsByEmail(String email);

    boolean existsByNationalId(String nationalId);

    boolean existsByMobileNumber(String mobileNumber);
}
