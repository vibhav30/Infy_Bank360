package com.infy.bank360.servicerequest.service;

import com.infy.bank360.servicerequest.dto.ServiceRequestDTO;
import com.infy.bank360.servicerequest.dto.ServiceRequestStatusDTO;

import java.util.List;

public interface ServiceRequestService {
    ServiceRequestDTO createTicket(ServiceRequestDTO dto);
    ServiceRequestDTO getTicketByTrackingId(String trackingId);
    List<ServiceRequestDTO> getTicketsByCustomerId(Long customerId);
    List<ServiceRequestDTO> getTicketsByStatus(String status);
    ServiceRequestDTO updateTicketStatus(Long serviceRequestId, ServiceRequestStatusDTO statusDTO);
    ServiceRequestDTO closeTicket(Long serviceRequestId);
}
