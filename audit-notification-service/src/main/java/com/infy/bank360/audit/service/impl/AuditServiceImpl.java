package com.infy.bank360.audit.service.impl;

import com.infy.bank360.audit.entity.AuditLog;
import com.infy.bank360.audit.entity.HighValueTransaction;
import com.infy.bank360.audit.repository.AuditLogRepository;
import com.infy.bank360.audit.repository.HighValueTransactionRepository;
import com.infy.bank360.audit.service.AuditService;
import com.infy.bank360.common.dto.AuditLogDTO;
import com.infy.bank360.common.dto.HighValueTransactionDTO;
import com.infy.bank360.common.exception.InfyBankException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final HighValueTransactionRepository highValueTransactionRepository;

    public AuditServiceImpl(AuditLogRepository auditLogRepository,
                            HighValueTransactionRepository highValueTransactionRepository) {
        this.auditLogRepository = auditLogRepository;
        this.highValueTransactionRepository = highValueTransactionRepository;
    }

    @Override
    public AuditLogDTO recordAuditLog(AuditLogDTO dto) {
        AuditLog log = AuditLog.builder()
                .userId(dto.getUserId() != null ? dto.getUserId() : "SYSTEM")
                .userRole(dto.getUserRole() != null ? dto.getUserRole() : "ROLE_SYSTEM")
                .action(dto.getAction())
                .resourceId(dto.getResourceId())
                .details(dto.getDetails())
                .clientIpAddress(dto.getClientIpAddress() != null ? dto.getClientIpAddress() : "127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        AuditLog saved = auditLogRepository.save(log);
        dto.setAuditId(saved.getAuditId());
        dto.setTimestamp(saved.getTimestamp());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .map(log -> AuditLogDTO.builder()
                        .auditId(log.getAuditId())
                        .userId(log.getUserId())
                        .userRole(log.getUserRole())
                        .action(log.getAction())
                        .resourceId(log.getResourceId())
                        .details(log.getDetails())
                        .clientIpAddress(log.getClientIpAddress())
                        .timestamp(log.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public HighValueTransactionDTO flagHighValueTransaction(HighValueTransactionDTO dto) {
        HighValueTransaction flag = HighValueTransaction.builder()
                .transactionId(dto.getTransactionId())
                .sourceAccount(dto.getSourceAccount())
                .destinationAccount(dto.getDestinationAccount())
                .amount(dto.getAmount())
                .flagReason(dto.getFlagReason() != null ? dto.getFlagReason() : "HIGH_VALUE_THRESHOLD_EXCEEDED")
                .flaggedAt(LocalDateTime.now())
                .reviewed(false)
                .build();

        HighValueTransaction saved = highValueTransactionRepository.save(flag);
        dto.setFlagId(saved.getFlagId());
        dto.setFlaggedAt(saved.getFlaggedAt());
        dto.setReviewed(saved.isReviewed());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HighValueTransactionDTO> getFlaggedTransactions() {
        return highValueTransactionRepository.findAllByOrderByFlaggedAtDesc().stream()
                .map(t -> HighValueTransactionDTO.builder()
                        .flagId(t.getFlagId())
                        .transactionId(t.getTransactionId())
                        .sourceAccount(t.getSourceAccount())
                        .destinationAccount(t.getDestinationAccount())
                        .amount(t.getAmount())
                        .flagReason(t.getFlagReason())
                        .flaggedAt(t.getFlaggedAt())
                        .reviewed(t.isReviewed())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public HighValueTransactionDTO reviewTransaction(Long flagId, String remarks) {
        HighValueTransaction tx = highValueTransactionRepository.findById(flagId)
                .orElseThrow(() -> new InfyBankException("ERR_FLAG_NOT_FOUND", "Flagged transaction record not found"));
        tx.setReviewed(true);
        tx.setComplianceNotes(remarks);
        HighValueTransaction saved = highValueTransactionRepository.save(tx);
        return HighValueTransactionDTO.builder()
                .flagId(saved.getFlagId())
                .transactionId(saved.getTransactionId())
                .sourceAccount(saved.getSourceAccount())
                .destinationAccount(saved.getDestinationAccount())
                .amount(saved.getAmount())
                .flagReason(saved.getFlagReason())
                .flaggedAt(saved.getFlaggedAt())
                .reviewed(saved.isReviewed())
                .build();
    }
}
