package com.infy.bank360.audit.service.impl;

import com.infy.bank360.audit.entity.Notification;
import com.infy.bank360.audit.repository.NotificationRepository;
import com.infy.bank360.audit.service.NotificationService;
import com.infy.bank360.common.dto.NotificationDTO;
import com.infy.bank360.common.exception.InfyBankException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public NotificationDTO sendNotification(NotificationDTO dto) {
        Notification notification = Notification.builder()
                .customerId(dto.getCustomerId())
                .title(dto.getTitle())
                .message(dto.getMessage())
                .type(dto.getType() != null ? dto.getType() : "ALERT")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        dto.setNotificationId(saved.getNotificationId());
        dto.setCreatedAt(saved.getCreatedAt());
        dto.setRead(saved.isRead());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getCustomerNotifications(Long customerId) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(n -> NotificationDTO.builder()
                        .notificationId(n.getNotificationId())
                        .customerId(n.getCustomerId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .type(n.getType())
                        .isRead(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public NotificationDTO markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new InfyBankException("ERR_NOTIFICATION_NOT_FOUND", "Notification not found"));
        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return NotificationDTO.builder()
                .notificationId(saved.getNotificationId())
                .customerId(saved.getCustomerId())
                .title(saved.getTitle())
                .message(saved.getMessage())
                .type(saved.getType())
                .isRead(saved.isRead())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
