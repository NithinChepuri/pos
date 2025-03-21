import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../services/product.service';
import { UploadResponse } from '../../models/upload-response';

@Component({
  selector: 'app-upload-product-modal',
  templateUrl: './upload-product-modal.component.html',
  styleUrls: ['./upload-product-modal.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class UploadProductModalComponent implements OnInit {
  @Output() closeModal = new EventEmitter<boolean>();
  
  selectedFile: File | null = null;
  loading = false;
  error = '';
  success = '';
  progress = 0;
  uploadResponse?: UploadResponse;
  failedEntries: any[] = [];
  showDownloadOption: boolean = false;

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    // Initialize any additional setup if needed
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.name.endsWith('.tsv')) {
        this.selectedFile = file;
        this.error = '';
        this.success = '';
        this.uploadResponse = undefined;
        console.log('Selected file:', file);
      } else {
        this.error = 'Please select a valid TSV file (must have .tsv extension)';
        this.selectedFile = null;
        event.target.value = '';
      }
    }
  }

  onSubmit(): void {
    if (!this.selectedFile) {
      this.error = 'Please select a TSV file first';
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';
    this.uploadResponse = undefined;

    this.productService.uploadProducts(this.selectedFile).subscribe({
      next: (response: UploadResponse) => {
        console.log('Upload response:', response);
        this.uploadResponse = response;
        this.loading = false;
        
        if (!response || typeof response.totalRows === 'undefined') {
          this.error = 'Invalid response from server';
          return;
        }

        if (response.totalRows === 0) {
          this.error = 'The file appears to be empty';
        } else if (response.errorCount === 0) {
          this.success = `Successfully uploaded all ${response.totalRows} products`;
          // Close modal after successful upload with a delay
          setTimeout(() => {
            this.close(true);
          }, 2000);
        } else if (response.successCount === 0) {
          this.showDownloadOption = true;
          this.failedEntries = response.errors.map(error => ({
            row: error.rowNumber,
            data: error.data,
            errorMessage: error.message
          }));
          this.error = `Failed to upload any products. All ${response.errorCount} entries had errors.`;
        } else {
          this.showDownloadOption = true;
          this.failedEntries = response.errors.map(error => ({
            row: error.rowNumber,
            data: error.data,
            errorMessage: error.message
          }));
          this.success = `Successfully uploaded ${response.successCount} out of ${response.totalRows} products. ` + 
                        `Please check the error details below for ${response.errorCount} failed entries.`;
        }
      },
      error: (error) => {
        console.error('Upload error:', error);
        this.error = 'Failed to upload file. Please try again.';
        this.loading = false;
      }
    });
  }

  getAlertClass(): string {
    if (!this.uploadResponse) return '';
    
    if (this.uploadResponse.errorCount === 0) {
      return 'alert-success';
    } else if (this.uploadResponse.successCount === 0) {
      return 'alert-danger';
    } else {
      return 'alert-warning';
    }
  }

  downloadFailedEntries() {
    if (!this.failedEntries || this.failedEntries.length === 0) {
      return;
    }
    
    try {
      // Create CSV content
      const headers = ['Row', 'Barcode', 'Name', 'Client ID', 'Error Message'];
      let csvContent = headers.join(',') + '\n';
      
      this.failedEntries.forEach(entry => {
        let barcode = '', name = '', clientId = '';
        
        try {
          if (entry.data && typeof entry.data === 'string') {
            const dataStr = entry.data;
            
            const barcodeMatch = dataStr.match(/Barcode: ([^,]+)/);
            const nameMatch = dataStr.match(/Name: ([^,]+)/);
            const clientIdMatch = dataStr.match(/Client ID: ([^,]+)/);
            
            barcode = barcodeMatch ? barcodeMatch[1] : '';
            name = nameMatch ? nameMatch[1] : '';
            clientId = clientIdMatch ? clientIdMatch[1] : '';
          }
        } catch (e) {
          console.error('Error parsing entry data:', e);
        }
        
        const rowData = [
          entry.row || entry.rowNumber || '',
          barcode,
          name,
          clientId,
          entry.errorMessage || entry.message || ''
        ].map(value => {
          if (value === null || value === undefined) return '""';
          return '"' + String(value).replace(/"/g, '""') + '"';
        }).join(',');
        
        csvContent += rowData + '\n';
      });
      
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      
      const link = document.createElement('a');
      link.setAttribute('href', url);
      link.setAttribute('download', 'failed_product_entries.csv');
      document.body.appendChild(link);
      
      link.click();
      
      setTimeout(() => {
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      }, 100);
    } catch (error) {
      console.error('Error in download process:', error);
    }
  }

  downloadSampleTsv() {
    console.log('Download sample TSV method called');
    // Sample data with header and one example row
    const sampleData = 'ClientId\tProduct Name\tBarcode\tMRP\n2\tiPhone 14\tAPPL010\t99999.99';
    
    // Create a blob with the sample data
    const blob = new Blob([sampleData], { type: 'text/tab-separated-values' });
    const url = URL.createObjectURL(blob);
    
    // Create a link element and trigger download
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', 'sample_products.tsv');
    document.body.appendChild(link);
    
    link.click();
    
    // Clean up
    setTimeout(() => {
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    }, 100);
  }

  close(refreshData: boolean = false): void {
    this.closeModal.emit(refreshData);
  }

  /**
   * Returns the first 5 errors for display in the UI
   */
  getDisplayErrors(): any[] {
    if (!this.uploadResponse || !this.uploadResponse.errors) {
      return [];
    }
    
    // Return only the first 5 errors
    return this.uploadResponse.errors.slice(0, 5);
  }
} 