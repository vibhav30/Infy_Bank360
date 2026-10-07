package com.infy.bank360.servicerequest.repository;

import com.infy.bank360.servicerequest.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    Optional<ServiceRequest> findByTrackingId(String trackingId);
    List<ServiceRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<ServiceRequest> findByStatusOrderByCreatedAtDesc(String status);
    boolean existsByTrackingId(String trackingId);
}
