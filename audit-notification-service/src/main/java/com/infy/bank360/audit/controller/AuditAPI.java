package com.infy.bank360.audit.controller;

import com.infy.bank360.audit.service.AuditService;
import com.infy.bank360.common.dto.ApiResponse;
import com.infy.bank360.common.dto.AuditLogDTO;
import com.infy.bank360.common.dto.HighValueTransactionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/infybank/audit")
public class AuditAPI {

    private final AuditService auditService;

    public AuditAPI(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping("/log")
    public ResponseEntity<ApiResponse<AuditLogDTO>> recordAuditLog(@RequestBody AuditLogDTO dto) {
        AuditLogDTO result = auditService.recordAuditLog(dto);
        return new ResponseEntity<>(ApiResponse.success(result, "Audit log recorded"), HttpStatus.CREATED);
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<AuditLogDTO>>> getAuditLogs() {
        List<AuditLogDTO> list = auditService.getAuditLogs();
        return ResponseEntity.ok(ApiResponse.success(list, "Audit logs retrieved successfully"));
    }

    @PostMapping("/surveillance")
    public ResponseEntity<ApiResponse<HighValueTransactionDTO>> flagHighValueTransaction(@RequestBody HighValueTransactionDTO dto) {
        HighValueTransactionDTO result = auditService.flagHighValueTransaction(dto);
        return new ResponseEntity<>(ApiResponse.success(result, "High value transaction flagged for compliance"), HttpStatus.CREATED);
    }

    @GetMapping("/surveillance")
    public ResponseEntity<ApiResponse<List<HighValueTransactionDTO>>> getFlaggedTransactions() {
        List<HighValueTransactionDTO> list = auditService.getFlaggedTransactions();
        return ResponseEntity.ok(ApiResponse.success(list, "High-value transactions retrieved"));
    }

    @PutMapping("/surveillance/{flagId}/review")
    public ResponseEntity<ApiResponse<HighValueTransactionDTO>> reviewTransaction(
            @PathVariable Long flagId,
            @RequestParam(defaultValue = "Reviewed by compliance officer") String remarks) {
        HighValueTransactionDTO result = auditService.reviewTransaction(flagId, remarks);
        return ResponseEntity.ok(ApiResponse.success(result, "Transaction review status updated"));
    }
}
