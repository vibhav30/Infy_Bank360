import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LoanService } from '../../services/loan.service';
import { LoanApplication, LoanEmiCalculation } from '../../models/loan.model';

@Component({
  selector: 'app-loans',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './loans.component.html',
  styleUrls: ['./loans.component.css']
})
export class LoansComponent implements OnInit {
  // Calculator Model
  calculator: LoanEmiCalculation = {
    principalAmount: 250000,
    tenureMonths: 36,
    loanType: 'PERSONAL',
    annualInterestRate: 11.5,
    monthlyEmi: 0,
    totalRepayment: 0,
    totalInterest: 0
  };

  // Application Model
  application: LoanApplication = {
    customerId: 1,
    accountNumber: '100000000001',
    loanType: 'PERSONAL',
    principalAmount: 200000,
    tenureMonths: 24,
    monthlyIncome: 65000
  };

  submittedLoan: any = null;
  dtiWarning: string | null = null;
  alertMessage: string | null = null;
  alertType: string = 'info';

  // Officer / Review View
  pendingLoans: any[] = [];
  reviewRemarks: string = '';

  constructor(private loanService: LoanService) {}

  ngOnInit(): void {
    this.calculateEmi();
    this.loadCustomerLoans();
  }

  calculateEmi(): void {
    this.loanService.calculateEmi(this.calculator).subscribe({
      next: (res) => {
        const data = res.data || res;
        this.calculator.monthlyEmi = data.monthlyEmi;
        this.calculator.totalRepayment = data.totalRepayment;
        this.calculator.totalInterest = data.totalInterest;
        this.calculator.annualInterestRate = data.annualInterestRate;
      },
      error: (err) => console.error('EMI calc error', err)
    });
  }

  onTypeChange(): void {
    if (this.calculator.loanType === 'HOME') {
      this.calculator.annualInterestRate = 8.5;
    } else if (this.calculator.loanType === 'VEHICLE') {
      this.calculator.annualInterestRate = 9.75;
    } else {
      this.calculator.annualInterestRate = 11.5;
    }
    this.calculateEmi();
  }

  applyLoan(): void {
    this.alertMessage = null;
    this.dtiWarning = null;

    this.loanService.applyLoan(this.application).subscribe({
      next: (res) => {
        const data = res.data || res;
        this.submittedLoan = data;
        this.alertMessage = `Loan application submitted successfully! Application ID: #${data.loanId}`;
        this.alertType = 'success';
        if (data.dtiWarning) {
          this.dtiWarning = data.dtiWarning;
        }
        this.loadCustomerLoans();
      },
      error: (err) => {
        this.alertMessage = err?.error?.errorMessage || 'Failed to submit loan application.';
        this.alertType = 'danger';
      }
    });
  }

  loadCustomerLoans(): void {
    this.loanService.getLoansByCustomerId(1).subscribe({
      next: (res) => {
        this.pendingLoans = res.data || res || [];
      },
      error: (err) => console.error(err)
    });
  }

  underwrite(loanId: number, status: 'APPROVED' | 'REJECTED'): void {
    const remarks = status === 'REJECTED' && !this.reviewRemarks ? 'Credit score below threshold' : this.reviewRemarks;
    this.loanService.updateLoanStatus(loanId, status, remarks).subscribe({
      next: () => {
        this.alertMessage = `Loan #${loanId} updated to ${status}!`;
        this.alertType = status === 'APPROVED' ? 'success' : 'warning';
        this.loadCustomerLoans();
      },
      error: (err) => {
        this.alertMessage = err?.error?.errorMessage || 'Failed to update loan status.';
        this.alertType = 'danger';
      }
    });
  }

  disburse(loanId: number, accNum: string): void {
    this.loanService.disburseLoan(loanId, accNum).subscribe({
      next: () => {
        this.alertMessage = `Loan #${loanId} principal disbursed to account ${accNum}!`;
        this.alertType = 'success';
        this.loadCustomerLoans();
      },
      error: (err) => {
        this.alertMessage = err?.error?.errorMessage || 'Disbursement failed.';
        this.alertType = 'danger';
      }
    });
  }
}
