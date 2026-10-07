import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Account } from '../models/account.model';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private readonly baseUrl = 'http://localhost:8080/infybank/accounts';

  constructor(private http: HttpClient) {}

  openAccount(account: Account): Observable<Account> {
    return this.http.post<Account>(this.baseUrl, account);
  }

  getAccountByNumber(accountNumber: string): Observable<Account> {
    return this.http.get<Account>(`${this.baseUrl}/${accountNumber}`);
  }

  getAccountsByCustomerId(customerId: number): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.baseUrl}/customer/${customerId}`);
  }

  debit(accountNumber: string, amount: number): Observable<Account> {
    return this.http.post<Account>(`${this.baseUrl}/${accountNumber}/debit?amount=${amount}`, {});
  }

  credit(accountNumber: string, amount: number): Observable<Account> {
    return this.http.post<Account>(`${this.baseUrl}/${accountNumber}/credit?amount=${amount}`, {});
  }

  updateAccountStatus(accountNumber: string, status: string): Observable<Account> {
    return this.http.put<Account>(`${this.baseUrl}/${accountNumber}/status?status=${status}`, {});
  }
}
