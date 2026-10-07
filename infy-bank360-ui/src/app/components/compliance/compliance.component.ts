import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComplianceService } from '../../services/compliance.service';
import { AuditLog, HighValueTransaction, NotificationItem } from '../../models/audit.model';

@Component({
  selector: 'app-compliance',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './compliance.component.html',
  styleUrls: ['./compliance.component.css']
})
export class ComplianceComponent implements OnInit {
  flaggedTransactions: HighValueTransaction[] = [];
  auditLogs: AuditLog[] = [];
  notifications: NotificationItem[] = [];

  alertMessage: string | null = null;
  alertType: string = 'info';

  constructor(private complianceService: ComplianceService) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loadFlaggedTransactions();
    this.loadAuditLogs();
    this.loadNotifications();
  }

  loadFlaggedTransactions(): void {
    this.complianceService.getFlaggedTransactions().subscribe({
      next: (res) => {
        this.flaggedTransactions = res.data || res || [];
      },
      error: (err) => console.error(err)
    });
  }

  loadAuditLogs(): void {
    this.complianceService.getAuditLogs().subscribe({
      next: (res) => {
        this.auditLogs = res.data || res || [];
      },
      error: (err) => console.error(err)
    });
  }

  loadNotifications(): void {
    this.complianceService.getCustomerNotifications(1).subscribe({
      next: (res) => {
        this.notifications = res.data || res || [];
      },
      error: (err) => console.error(err)
    });
  }

  reviewTransaction(flagId: number): void {
    const remarks = prompt('Enter compliance review remarks:') || 'Verified legitimate financial transaction.';
    this.complianceService.reviewTransaction(flagId, remarks).subscribe({
      next: () => {
        this.alertMessage = `High-value transaction #${flagId} reviewed and cleared!`;
        this.alertType = 'success';
        this.loadFlaggedTransactions();
      },
      error: (err) => {
        this.alertMessage = err?.error?.errorMessage || 'Review action failed.';
        this.alertType = 'danger';
      }
    });
  }

  markRead(notificationId: number): void {
    this.complianceService.markNotificationRead(notificationId).subscribe({
      next: () => {
        this.loadNotifications();
      },
      error: (err) => console.error(err)
    });
  }
}
