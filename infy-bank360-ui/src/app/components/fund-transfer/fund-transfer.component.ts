import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { TransactionService } from '../../services/transaction.service';
import { AccountService } from '../../services/account.service';
import { Account } from '../../models/account.model';
import { TransactionRecord } from '../../models/transaction.model';
import { ErrorInfo } from '../../models/error-info.model';

@Component({
  selector: 'app-fund-transfer',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, CurrencyPipe],
  templateUrl: './fund-transfer.component.html',
  styleUrls: ['./fund-transfer.component.css']
})
export class FundTransferComponent implements OnInit {
  transferForm!: FormGroup;
  accounts: Account[] = [];
  selectedAccount: Account | null = null;

  // Real-time dynamic discount calculations
  grossAmount = 0;
  discount = 0;
  netAmount = 0;
  isDiscountEligible = false;
  hasInsufficientBalance = false;

  isSubmitting = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  completedTransaction: TransactionRecord | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private transactionService: TransactionService,
    private accountService: AccountService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadAccounts();

    // Listen to amount changes for dynamic real-time discount calculation
    this.transferForm.get('amount')?.valueChanges.subscribe(val => {
      this.recalculateDiscountAndBalance(val);
    });

    // Listen to source account selection changes for balance checking
    this.transferForm.get('sourceAccountNumber')?.valueChanges.subscribe(accNum => {
      this.selectedAccount = this.accounts.find(a => a.accountNumber === accNum) || null;
      this.recalculateDiscountAndBalance(this.transferForm.get('amount')?.value);
    });
  }

  private initForm(): void {
    const prefillSource = this.route.snapshot.queryParams['source'] || '';

    this.transferForm = this.fb.group({
      sourceAccountNumber: [prefillSource, [Validators.required, Validators.pattern('^[0-9]{12}$')]],
      destinationAccountNumber: ['', [Validators.required, Validators.pattern('^[0-9]{12}$')]],
      amount: [
        '',
        [
          Validators.required,
          Validators.min(0.01)
        ]
      ],
      remarks: ['']
    });
  }

  private loadAccounts(): void {
    this.accountService.getAccountsByCustomerId(1).subscribe({
      next: (accs) => {
        this.accounts = accs;
        if (this.accounts.length > 0 && !this.transferForm.get('sourceAccountNumber')?.value) {
          this.transferForm.patchValue({ sourceAccountNumber: this.accounts[0].accountNumber });
          this.selectedAccount = this.accounts[0];
        }
      },
      error: () => {
        // Fallback demo accounts
        this.accounts = [
          { accountId: 1, accountNumber: '100000000001', customerId: 1, accountType: 'SAVINGS', balance: 1000.00, status: 'ACTIVE' },
          { accountId: 2, accountNumber: '100000000002', customerId: 1, accountType: 'CHECKING', balance: 2500.00, status: 'ACTIVE' }
        ];
        this.selectedAccount = this.accounts[0];
        this.transferForm.patchValue({ sourceAccountNumber: this.accounts[0].accountNumber });
      }
    });
  }

  recalculateDiscountAndBalance(rawAmount: any): void {
    const amt = Number(rawAmount) || 0;
    this.grossAmount = amt;

    // BR-03 & BR-04: Dynamic Discount Calculation (> 500 Rule)
    if (amt > 500) {
      this.isDiscountEligible = true;
      this.discount = Math.round(amt * 0.05 * 100) / 100; // 5% discount rounded
      this.netAmount = Math.round((amt - this.discount) * 100) / 100;
    } else {
      this.isDiscountEligible = false;
      this.discount = 0;
      this.netAmount = amt;
    }

    // Dynamic Balance Verification
    if (this.selectedAccount && this.netAmount > Number(this.selectedAccount.balance)) {
      this.hasInsufficientBalance = true;
    } else {
      this.hasInsufficientBalance = false;
    }
  }

  get f() {
    return this.transferForm.controls;
  }

  onSubmit(): void {
    if (this.transferForm.invalid || this.hasInsufficientBalance) {
      this.transferForm.markAllAsTouched();
      return;
    }

    const source = this.transferForm.value.sourceAccountNumber;
    const dest = this.transferForm.value.destinationAccountNumber;

    // Client-side self transfer check
    if (source === dest) {
      this.errorMessage = '[ERR_SELF_TRANSFER_PROHIBITED] Source and destination account numbers cannot be identical.';
      return;
    }

    this.isSubmitting = true;
    this.successMessage = null;
    this.errorMessage = null;

    const transferPayload: TransactionRecord = {
      sourceAccountNumber: source,
      destinationAccountNumber: dest,
      grossAmount: this.grossAmount,
      discount: this.discount,
      netAmount: this.netAmount,
      transactionType: 'TRANSFER',
      status: 'SUCCESS'
    };

    this.transactionService.transferFunds(transferPayload).subscribe({
      next: (res: TransactionRecord) => {
        this.isSubmitting = false;
        this.completedTransaction = res;
        this.successMessage = `Fund transfer executed successfully! Transaction Ref: #${res.transactionId || 'SUCCESS'}`;
        
        // Update local balance
        if (this.selectedAccount) {
          this.selectedAccount.balance -= this.netAmount;
        }

        this.transferForm.get('destinationAccountNumber')?.reset();
        this.transferForm.get('amount')?.reset();
        this.grossAmount = 0;
        this.discount = 0;
        this.netAmount = 0;
      },
      error: (err) => {
        this.isSubmitting = false;
        // Parse InfyBankException ErrorInfo envelope from backend
        if (err.error && err.error.errorMessage) {
          const errorInfo: ErrorInfo = err.error;
          this.errorMessage = `[${errorInfo.errorCode || 'ERR_TRANSFER_REJECTED'}] ${errorInfo.errorMessage}`;
        } else {
          this.errorMessage = 'An unexpected network error occurred while routing through API Gateway (Port 8080).';
        }
      }
    });
  }

  dismissAlert(): void {
    this.successMessage = null;
    this.errorMessage = null;
  }
}
