package com.infy.bank360.servicerequest.controller;

import com.infy.bank360.common.dto.ApiResponse;
import com.infy.bank360.servicerequest.dto.ServiceRequestDTO;
import com.infy.bank360.servicerequest.dto.ServiceRequestStatusDTO;
import com.infy.bank360.servicerequest.service.ServiceRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/infybank/requests")
@Validated
public class ServiceRequestAPI {

    private final ServiceRequestService serviceRequestService;

    public ServiceRequestAPI(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> createTicket(@Valid @RequestBody ServiceRequestDTO requestDTO) {
        ServiceRequestDTO created = serviceRequestService.createTicket(requestDTO);
        return new ResponseEntity<>(ApiResponse.success(created, "Support ticket created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/{trackingId}")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> getTicket(@PathVariable String trackingId) {
        ServiceRequestDTO ticket = serviceRequestService.getTicketByTrackingId(trackingId);
        return ResponseEntity.ok(ApiResponse.success(ticket, "Support ticket retrieved"));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<ServiceRequestDTO>>> getTicketsByCustomer(@PathVariable Long customerId) {
        List<ServiceRequestDTO> list = serviceRequestService.getTicketsByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(list, "Customer support tickets retrieved"));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ServiceRequestDTO>>> getTicketsByStatus(@PathVariable String status) {
        List<ServiceRequestDTO> list = serviceRequestService.getTicketsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(list, "Tickets for status " + status + " retrieved"));
    }

    @PutMapping("/{serviceRequestId}/status")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> updateTicketStatus(
            @PathVariable Long serviceRequestId,
            @Valid @RequestBody ServiceRequestStatusDTO statusDTO) {
        ServiceRequestDTO updated = serviceRequestService.updateTicketStatus(serviceRequestId, statusDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Ticket status updated to " + updated.getStatus()));
    }

    @PutMapping("/{serviceRequestId}/close")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> closeTicket(@PathVariable Long serviceRequestId) {
        ServiceRequestDTO closed = serviceRequestService.closeTicket(serviceRequestId);
        return ResponseEntity.ok(ApiResponse.success(closed, "Support ticket closed"));
    }
}
