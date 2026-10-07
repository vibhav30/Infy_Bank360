package com.infy.bank360.customer.repository;

import com.infy.bank360.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByNationalId(String nationalId);
    boolean existsByEmail(String email);
    boolean existsByNationalId(String nationalId);
    boolean existsByMobileNumber(String mobileNumber);
    List<Customer> findByKycStatus(String kycStatus);
}
