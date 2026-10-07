package com.infy.bank360.audit.service;

import com.infy.bank360.common.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {
    NotificationDTO sendNotification(NotificationDTO dto);
    List<NotificationDTO> getCustomerNotifications(Long customerId);
    NotificationDTO markAsRead(Long notificationId);
}
