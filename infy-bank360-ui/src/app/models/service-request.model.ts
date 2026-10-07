export interface ServiceRequest {
  serviceRequestId?: number;
  trackingId?: string;
  customerId: number;
  category: string;
  subject: string;
  description: string;
  status?: string;
  resolutionNotes?: string;
  assignedTo?: string;
  createdAt?: string;
  updatedAt?: string;
}
