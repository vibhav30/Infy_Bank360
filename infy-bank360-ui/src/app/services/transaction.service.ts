import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TransactionRecord } from '../models/transaction.model';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private readonly baseUrl = 'http://localhost:8080/infybank/transactions';

  constructor(private http: HttpClient) {}

  transferFunds(transaction: TransactionRecord): Observable<TransactionRecord> {
    return this.http.post<TransactionRecord>(`${this.baseUrl}/transfer`, transaction);
  }

  payUtilityBill(transaction: TransactionRecord): Observable<TransactionRecord> {
    return this.http.post<TransactionRecord>(`${this.baseUrl}/bill-pay`, transaction);
  }

  getTransactionHistory(accountNumber: string): Observable<TransactionRecord[]> {
    return this.http.get<TransactionRecord[]>(`${this.baseUrl}/${accountNumber}`);
  }

  calculateDiscount(amount: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/discount?amount=${amount}`);
  }
}
