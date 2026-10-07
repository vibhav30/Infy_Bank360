/**
 * TypeScript Model: TransactionRecord
 * Mirrors com.infy.dto.TransactionRecordDTO
 */
export type TransactionType = 'TRANSFER' | 'UTILITY_BILL' | 'DEPOSIT';
export type TransactionStatus = 'SUCCESS' | 'FAILED' | 'PENDING' | 'COMPLIANCE_FLAGGED';

export interface TransactionRecord {
  transactionId?: number;
  sourceAccountNumber: string;
  destinationAccountNumber?: string | null;
  grossAmount: number;
  discount: number;
  netAmount: number;
  transactionType: TransactionType;
  transactionDate?: string;
  status: TransactionStatus;
}
