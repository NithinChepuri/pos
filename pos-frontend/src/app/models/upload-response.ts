export interface UploadError {
  rowNumber: number;
  data: string;
  message: string;
}

export interface UploadResponse {
  successfulEntries: any[];
  errors: UploadError[];
  totalRows: number;
  successCount: number;
  errorCount: number;
} 