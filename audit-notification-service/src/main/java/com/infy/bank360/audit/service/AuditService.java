package com.infy.bank360.audit.service;

import com.infy.bank360.common.dto.AuditLogDTO;
import com.infy.bank360.common.dto.HighValueTransactionDTO;

import java.util.List;

public interface AuditService {
    AuditLogDTO recordAuditLog(AuditLogDTO dto);
    List<AuditLogDTO> getAuditLogs();
    HighValueTransactionDTO flagHighValueTransaction(HighValueTransactionDTO dto);
    List<HighValueTransactionDTO> getFlaggedTransactions();
    HighValueTransactionDTO reviewTransaction(Long flagId, String remarks);
}
