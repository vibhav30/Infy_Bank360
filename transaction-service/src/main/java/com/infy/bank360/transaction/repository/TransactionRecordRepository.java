package com.infy.bank360.transaction.repository;

import com.infy.bank360.transaction.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {
    List<TransactionRecord> findBySourceAccountNumberOrDestinationAccountNumberOrderByTransactionDateDesc(
            String sourceAccountNumber, String destinationAccountNumber);
}
