import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HelpdeskService } from '../../services/helpdesk.service';
import { ServiceRequest } from '../../models/service-request.model';

@Component({
  selector: 'app-helpdesk',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './helpdesk.component.html',
  styleUrls: ['./helpdesk.component.css']
})
export class HelpdeskComponent implements OnInit {
  ticket: ServiceRequest = {
    customerId: 1,
    category: 'TRANSACTION_DISPUTE',
    subject: '',
    description: ''
  };

  searchTrackingId: string = '';
  searchedTicket: any = null;

  tickets: any[] = [];
  alertMessage: string | null = null;
  alertType: string = 'info';

  resolutionNotesInput: string = '';

  constructor(private helpdeskService: HelpdeskService) {}

  ngOnInit(): void {
    this.loadTickets();
  }

  loadTickets(): void {
    this.helpdeskService.getTicketsByCustomerId(1).subscribe({
      next: (res) => {
        this.tickets = res.data || res || [];
      },
      error: (err) => console.error(err)
    });
  }

  createTicket(): void {
    this.alertMessage = null;
    this.helpdeskService.createTicket(this.ticket).subscribe({
      next: (res) => {
        const data = res.data || res;
        this.alertMessage = `Support ticket created! Tracking ID: ${data.trackingId}`;
        this.alertType = 'success';
        this.ticket.subject = '';
        this.ticket.description = '';
        this.loadTickets();
      },
      error: (err) => {
        this.alertMessage = err?.error?.errorMessage || 'Failed to create support ticket.';
        this.alertType = 'danger';
      }
    });
  }

  searchTicket(): void {
    if (!this.searchTrackingId.trim()) return;
    this.helpdeskService.getTicketByTrackingId(this.searchTrackingId.trim()).subscribe({
      next: (res) => {
        this.searchedTicket = res.data || res;
      },
      error: (err) => {
        this.searchedTicket = null;
        this.alertMessage = err?.error?.errorMessage || 'Ticket not found.';
        this.alertType = 'warning';
      }
    });
  }

  resolveTicket(serviceRequestId: number): void {
    const notes = this.resolutionNotesInput.trim() || 'Dispute reviewed and resolved by support executive.';
    this.helpdeskService.updateTicketStatus(serviceRequestId, 'RESOLVED', notes, 'Support Officer').subscribe({
      next: () => {
        this.alertMessage = `Ticket #${serviceRequestId} marked as RESOLVED!`;
        this.alertType = 'success';
        this.loadTickets();
        if (this.searchedTicket && this.searchedTicket.serviceRequestId === serviceRequestId) {
          this.searchTicket();
        }
      },
      error: (err) => {
        this.alertMessage = err?.error?.errorMessage || 'Failed to update ticket.';
        this.alertType = 'danger';
      }
    });
  }

  closeTicket(serviceRequestId: number): void {
    this.helpdeskService.closeTicket(serviceRequestId).subscribe({
      next: () => {
        this.alertMessage = `Ticket #${serviceRequestId} accepted & closed by customer. Issue settled!`;
        this.alertType = 'success';
        this.loadTickets();
        if (this.searchedTicket && this.searchedTicket.serviceRequestId === serviceRequestId) {
          this.searchTicket();
        }
      },
      error: (err) => {
        this.alertMessage = err?.error?.errorMessage || 'Failed to close ticket.';
        this.alertType = 'danger';
      }
    });
  }
}
