package com.infy.bank360.servicerequest.service.impl;

import com.infy.bank360.common.exception.InfyBankException;
import com.infy.bank360.servicerequest.dto.ServiceRequestDTO;
import com.infy.bank360.servicerequest.dto.ServiceRequestStatusDTO;
import com.infy.bank360.servicerequest.entity.ServiceRequest;
import com.infy.bank360.servicerequest.repository.ServiceRequestRepository;
import com.infy.bank360.servicerequest.service.ServiceRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServiceRequestServiceImpl implements ServiceRequestService {

    private final ServiceRequestRepository requestRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ServiceRequestServiceImpl(ServiceRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    public ServiceRequestDTO createTicket(ServiceRequestDTO dto) {
        // Generate unique tracking identifier TCK-XXXXX
        String trackingId;
        do {
            int randomSeq = 10000 + secureRandom.nextInt(90000);
            trackingId = "TCK-" + randomSeq;
        } while (requestRepository.existsByTrackingId(trackingId));

        ServiceRequest request = ServiceRequest.builder()
                .trackingId(trackingId)
                .customerId(dto.getCustomerId())
                .category(dto.getCategory().toUpperCase())
                .subject(dto.getSubject())
                .description(dto.getDescription())
                .status("OPEN")
                .build();

        ServiceRequest saved = requestRepository.save(request);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestDTO getTicketByTrackingId(String trackingId) {
        ServiceRequest request = requestRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new InfyBankException("ERR_TICKET_NOT_FOUND", "Ticket not found with Tracking ID: " + trackingId));
        return mapToDTO(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestDTO> getTicketsByCustomerId(Long customerId) {
        return requestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestDTO> getTicketsByStatus(String status) {
        return requestRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ServiceRequestDTO updateTicketStatus(Long serviceRequestId, ServiceRequestStatusDTO statusDTO) {
        ServiceRequest request = requestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new InfyBankException("ERR_TICKET_NOT_FOUND", "Ticket not found with ID: " + serviceRequestId));

        request.setStatus(statusDTO.getStatus().toUpperCase());
        if (statusDTO.getResolutionNotes() != null) {
            request.setResolutionNotes(statusDTO.getResolutionNotes());
        }
        if (statusDTO.getAssignedTo() != null) {
            request.setAssignedTo(statusDTO.getAssignedTo());
        }

        ServiceRequest saved = requestRepository.save(request);
        return mapToDTO(saved);
    }

    @Override
    public ServiceRequestDTO closeTicket(Long serviceRequestId) {
        ServiceRequest request = requestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new InfyBankException("ERR_TICKET_NOT_FOUND", "Ticket not found with ID: " + serviceRequestId));

        request.setStatus("CLOSED");
        ServiceRequest saved = requestRepository.save(request);
        return mapToDTO(saved);
    }

    private ServiceRequestDTO mapToDTO(ServiceRequest r) {
        return ServiceRequestDTO.builder()
                .serviceRequestId(r.getServiceRequestId())
                .trackingId(r.getTrackingId())
                .customerId(r.getCustomerId())
                .category(r.getCategory())
                .subject(r.getSubject())
                .description(r.getDescription())
                .status(r.getStatus())
                .resolutionNotes(r.getResolutionNotes())
                .assignedTo(r.getAssignedTo())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
