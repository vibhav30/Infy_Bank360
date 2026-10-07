export interface LoanApplication {
  loanId?: number;
  customerId: number;
  accountNumber?: string;
  loanType: string;
  principalAmount: number;
  tenureMonths: number;
  annualInterestRate?: number;
  monthlyEmi?: number;
  monthlyIncome: number;
  status?: string;
  rejectionRemarks?: string;
  dtiWarning?: string;
}

export interface LoanEmiCalculation {
  principalAmount: number;
  tenureMonths: number;
  loanType: string;
  annualInterestRate?: number;
  monthlyEmi?: number;
  totalRepayment?: number;
  totalInterest?: number;
}
