import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AccountService } from '../../services/account.service';
import { TransactionService } from '../../services/transaction.service';
import { CustomerService } from '../../services/customer.service';
import { Account } from '../../models/account.model';
import { TransactionRecord } from '../../models/transaction.model';
import { Customer } from '../../models/customer.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, CurrencyPipe, DatePipe],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  accounts: Account[] = [];
  transactions: TransactionRecord[] = [];
  selectedCustomer: Customer | null = null;
  isLoading = false;
  errorMessage: string | null = null;

  // Selected default test customer ID for demo view (Rajesh Sharma - ID 1)
  customerId = 1;

  constructor(
    private accountService: AccountService,
    private transactionService: TransactionService,
    private customerService: CustomerService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.isLoading = true;
    this.errorMessage = null;

    // 1. Fetch Customer Profile
    this.customerService.getCustomerById(this.customerId).subscribe({
      next: (cust) => {
        this.selectedCustomer = cust;
      },
      error: () => {
        // Fallback default mock data if backend not actively running
        this.selectedCustomer = {
          customerId: 1,
          fullName: 'Rajesh Sharma',
          email: 'rajesh.sharma@infybank.com',
          mobileNumber: '9876543210',
          nationalId: 'IND10982345',
          kycStatus: 'KYC_VERIFIED'
        };
      }
    });

    // 2. Fetch Accounts
    this.accountService.getAccountsByCustomerId(this.customerId).subscribe({
      next: (accs) => {
        this.accounts = accs;
        if (this.accounts.length > 0) {
          this.loadTransactions(this.accounts[0].accountNumber);
        } else {
          this.isLoading = false;
        }
      },
      error: () => {
        // Seed fallback data for UI preview
        this.accounts = [
          { accountId: 1, accountNumber: '100000000001', customerId: 1, accountType: 'SAVINGS', balance: 1000.00, status: 'ACTIVE' },
          { accountId: 2, accountNumber: '100000000002', customerId: 1, accountType: 'CHECKING', balance: 2500.00, status: 'ACTIVE' }
        ];
        this.loadTransactions('100000000001');
      }
    });
  }

  loadTransactions(accountNumber: string): void {
    this.transactionService.getTransactionHistory(accountNumber).subscribe({
      next: (txns) => {
        this.transactions = txns;
        this.isLoading = false;
      },
      error: () => {
        // Seed fallback transactions for UI preview
        this.transactions = [
          { transactionId: 1, sourceAccountNumber: '100000000001', destinationAccountNumber: '100000000003', grossAmount: 1200.00, discount: 60.00, netAmount: 1140.00, transactionType: 'TRANSFER', status: 'SUCCESS', transactionDate: '2026-10-01T10:30:00' },
          { transactionId: 6, sourceAccountNumber: '100000000001', destinationAccountNumber: '100000000004', grossAmount: 1500.00, discount: 75.00, netAmount: 1425.00, transactionType: 'TRANSFER', status: 'FAILED', transactionDate: '2026-10-06T18:00:00' }
        ];
        this.isLoading = false;
      }
    });
  }

  getTotalBalance(): number {
    return this.accounts.reduce((sum, acc) => sum + (Number(acc.balance) || 0), 0);
  }

  getActiveAccountsCount(): number {
    return this.accounts.filter(a => a.status === 'ACTIVE').length;
  }
}
