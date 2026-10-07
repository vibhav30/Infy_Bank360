/**
 * TypeScript Model: Account
 * Mirrors com.infy.dto.AccountDTO
 */
export type AccountType = 'SAVINGS' | 'CHECKING';
export type AccountStatus = 'ACTIVE' | 'FROZEN' | 'CLOSED';

export interface Account {
  accountId?: number;
  accountNumber: string;
  customerId: number;
  accountType: AccountType;
  balance: number;
  status: AccountStatus;
  createdAt?: string;
  updatedAt?: string;
}
