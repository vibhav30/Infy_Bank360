package com.infy.bank360.audit.repository;

import com.infy.bank360.audit.entity.HighValueTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HighValueTransactionRepository extends JpaRepository<HighValueTransaction, Long> {
    List<HighValueTransaction> findAllByOrderByFlaggedAtDesc();
    List<HighValueTransaction> findByReviewedFalseOrderByFlaggedAtDesc();
}
