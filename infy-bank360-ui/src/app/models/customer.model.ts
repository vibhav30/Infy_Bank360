/**
 * TypeScript Model: Customer
 * Mirrors com.infy.dto.CustomerDTO
 */
export type KycStatus = 'PENDING_KYC' | 'SUBMITTED_FOR_VERIFICATION' | 'KYC_VERIFIED' | 'KYC_REJECTED';

export interface Customer {
  customerId?: number;
  fullName: string;
  email: string;
  mobileNumber: string;
  nationalId: string;
  kycStatus?: KycStatus;
  createdAt?: string;
  updatedAt?: string;
}
