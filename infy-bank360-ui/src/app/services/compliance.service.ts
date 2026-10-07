import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuditLog, HighValueTransaction, NotificationItem } from '../models/audit.model';

@Injectable({
  providedIn: 'root'
})
export class ComplianceService {
  private readonly auditUrl = 'http://localhost:8080/infybank/audit';
  private readonly notifUrl = 'http://localhost:8080/infybank/notifications';

  constructor(private http: HttpClient) {}

  getAuditLogs(): Observable<any> {
    return this.http.get<any>(`${this.auditUrl}/logs`);
  }

  getFlaggedTransactions(): Observable<any> {
    return this.http.get<any>(`${this.auditUrl}/surveillance`);
  }

  reviewTransaction(flagId: number, remarks: string): Observable<any> {
    return this.http.put<any>(`${this.auditUrl}/surveillance/${flagId}/review?remarks=${encodeURIComponent(remarks)}`, {});
  }

  getCustomerNotifications(customerId: number): Observable<any> {
    return this.http.get<any>(`${this.notifUrl}/customer/${customerId}`);
  }

  markNotificationRead(notificationId: number): Observable<any> {
    return this.http.put<any>(`${this.notifUrl}/${notificationId}/read`, {});
  }
}
