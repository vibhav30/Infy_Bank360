package com.infy.bank360.audit.controller;

import com.infy.bank360.audit.service.NotificationService;
import com.infy.bank360.common.dto.ApiResponse;
import com.infy.bank360.common.dto.NotificationDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/infybank/notifications")
public class NotificationAPI {

    private final NotificationService notificationService;

    public NotificationAPI(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationDTO>> sendNotification(@RequestBody NotificationDTO dto) {
        NotificationDTO result = notificationService.sendNotification(dto);
        return new ResponseEntity<>(ApiResponse.success(result, "Notification generated"), HttpStatus.CREATED);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getCustomerNotifications(@PathVariable Long customerId) {
        List<NotificationDTO> list = notificationService.getCustomerNotifications(customerId);
        return ResponseEntity.ok(ApiResponse.success(list, "Notifications retrieved"));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationDTO>> markAsRead(@PathVariable Long notificationId) {
        NotificationDTO result = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success(result, "Notification marked as read"));
    }
}
