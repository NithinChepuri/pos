export interface UploadError {
  rowNumber: number;
  data: string;
  message: string;
}

// ErrorEntry and UploadError are the same, so we can use just one
export type ErrorEntry = UploadError;

export interface UploadResponse {
  successfulEntries: any[];
  errors: UploadError[];
  totalRows: number;
  successCount: number;
  errorCount: number;
  failedEntries?: FailedEntry[];
}

export interface FailedEntry {
  row: number;
  data: string;
  errorMessage: string;
} 