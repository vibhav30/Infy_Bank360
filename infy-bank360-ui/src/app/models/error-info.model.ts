/**
 * TypeScript Model: ErrorInfo & ApiResponse
 * Mirrors com.infy.exception.ErrorInfo and ApiResponse<T>
 */
export interface ErrorInfo {
  errorCode: string;
  errorMessage: string;
  timestamp: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}
