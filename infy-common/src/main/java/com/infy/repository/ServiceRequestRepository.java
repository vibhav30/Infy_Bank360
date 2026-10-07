package com.infy.repository;

import com.infy.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository: ServiceRequestRepository
 * Spring Data JPA repository for ServiceRequest entity.
 * Reference: Tablescripts.sql (table: service_request) & spec.md (AC-REQ-01 to AC-REQ-03)
 */
@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    Optional<ServiceRequest> findByTrackingId(String trackingId);

    List<ServiceRequest> findByCustomerCustomerId(Long customerId);

    List<ServiceRequest> findByCustomerCustomerIdAndStatus(Long customerId, String status);

    List<ServiceRequest> findByStatus(String status);

    List<ServiceRequest> findByCategory(String category);

    boolean existsByTrackingId(String trackingId);
}
