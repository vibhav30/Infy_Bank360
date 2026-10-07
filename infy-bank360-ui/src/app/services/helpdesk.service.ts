import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ServiceRequest } from '../models/service-request.model';

@Injectable({
  providedIn: 'root'
})
export class HelpdeskService {
  private readonly baseUrl = 'http://localhost:8080/infybank/requests';

  constructor(private http: HttpClient) {}

  createTicket(ticket: ServiceRequest): Observable<any> {
    return this.http.post<any>(this.baseUrl, ticket);
  }

  getTicketByTrackingId(trackingId: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${trackingId}`);
  }

  getTicketsByCustomerId(customerId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/customer/${customerId}`);
  }

  getTicketsByStatus(status: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/status/${status}`);
  }

  updateTicketStatus(serviceRequestId: number, status: string, resolutionNotes?: string, assignedTo?: string): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${serviceRequestId}/status`, { status, resolutionNotes, assignedTo });
  }

  closeTicket(serviceRequestId: number): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${serviceRequestId}/close`, {});
  }
}
