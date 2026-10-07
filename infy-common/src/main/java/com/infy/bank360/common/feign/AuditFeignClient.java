package com.infy.bank360.common.feign;

import com.infy.bank360.common.dto.ApiResponse;
import com.infy.bank360.common.dto.AuditLogDTO;
import com.infy.bank360.common.dto.HighValueTransactionDTO;
import com.infy.bank360.common.dto.NotificationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "audit-notification-service", path = "/infybank")
public interface AuditFeignClient {

    @PostMapping("/audit/log")
    ApiResponse<AuditLogDTO> logAudit(@RequestBody AuditLogDTO auditLogDTO);

    @PostMapping("/audit/surveillance")
    ApiResponse<HighValueTransactionDTO> flagHighValueTransaction(@RequestBody HighValueTransactionDTO flagDTO);

    @PostMapping("/notifications")
    ApiResponse<NotificationDTO> sendNotification(@RequestBody NotificationDTO notificationDTO);
}
