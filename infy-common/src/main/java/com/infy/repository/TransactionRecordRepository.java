package com.infy.repository;

import com.infy.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository: TransactionRecordRepository
 * Spring Data JPA repository for TransactionRecord entity.
 * Reference: Tablescripts.sql (table: transaction_record) & spec.md (AC-TXN-01 to AC-TXN-04)
 */
@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {

    List<TransactionRecord> findBySourceAccountAccountNumberOrderByTransactionDateDesc(String sourceAccountNumber);

    List<TransactionRecord> findByDestinationAccountAccountNumberOrderByTransactionDateDesc(String destinationAccountNumber);

    @Query("SELECT t FROM TransactionRecord t " +
           "WHERE t.sourceAccount.accountNumber = :accountNumber " +
           "   OR t.destinationAccount.accountNumber = :accountNumber " +
           "ORDER BY t.transactionDate DESC")
    List<TransactionRecord> findAllByAccountNumberOrderByTransactionDateDesc(@Param("accountNumber") String accountNumber);

    List<TransactionRecord> findByStatus(String status);

    List<TransactionRecord> findByTransactionType(String transactionType);
}
