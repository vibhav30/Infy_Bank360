import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoanApplication, LoanEmiCalculation } from '../models/loan.model';

@Injectable({
  providedIn: 'root'
})
export class LoanService {
  private readonly baseUrl = 'http://localhost:8080/infybank/loans';

  constructor(private http: HttpClient) {}

  calculateEmi(calculation: LoanEmiCalculation): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/calculate-emi`, calculation);
  }

  applyLoan(application: LoanApplication): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/apply`, application);
  }

  getLoanById(loanId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${loanId}`);
  }

  getLoansByCustomerId(customerId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/customer/${customerId}`);
  }

  updateLoanStatus(loanId: number, status: string, remarks?: string): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${loanId}/status`, { status, remarks });
  }

  disburseLoan(loanId: number, accountNumber?: string): Observable<any> {
    const url = accountNumber 
      ? `${this.baseUrl}/${loanId}/disburse?accountNumber=${accountNumber}`
      : `${this.baseUrl}/${loanId}/disburse`;
    return this.http.post<any>(url, {});
  }
}
