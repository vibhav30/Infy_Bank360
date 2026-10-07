import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Customer } from '../models/customer.model';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  private readonly baseUrl = 'http://localhost:8080/infybank/customers';

  constructor(private http: HttpClient) {}

  registerCustomer(customer: Customer): Observable<Customer> {
    return this.http.post<Customer>(this.baseUrl, customer);
  }

  getCustomerById(customerId: number): Observable<Customer> {
    return this.http.get<Customer>(`${this.baseUrl}/${customerId}`);
  }

  getCustomerByEmail(email: string): Observable<Customer> {
    return this.http.get<Customer>(`${this.baseUrl}/email/${email}`);
  }

  updateKycStatus(customerId: number, kycStatus: string): Observable<Customer> {
    return this.http.put<Customer>(`${this.baseUrl}/${customerId}/kyc?kycStatus=${kycStatus}`, {});
  }

  getAllCustomers(): Observable<Customer[]> {
    return this.http.get<Customer[]>(this.baseUrl);
  }
}
