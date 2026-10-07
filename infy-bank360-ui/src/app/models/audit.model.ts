export interface AuditLog {
  auditId?: number;
  userId: string;
  userRole: string;
  action: string;
  resourceId?: string;
  details?: string;
  clientIpAddress?: string;
  timestamp?: string;
}

export interface HighValueTransaction {
  flagId?: number;
  transactionId?: number;
  sourceAccount: string;
  destinationAccount?: string;
  amount: number;
  flagReason?: string;
  flaggedAt?: string;
  reviewed: boolean;
  complianceNotes?: string;
}

export interface NotificationItem {
  notificationId?: number;
  customerId: number;
  title: string;
  message: string;
  type: string;
  isRead: boolean;
  createdAt?: string;
}
