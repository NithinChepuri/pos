import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InventoryService } from '../../services/inventory.service';
import { UploadResponse } from '../../models/upload-response';

@Component({
  selector: 'app-upload-inventory-modal',
  templateUrl: './upload-inventory-modal.component.html',
  styleUrls: ['./upload-inventory-modal.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class UploadInventoryModalComponent implements OnInit {
  @Output() closeModal = new EventEmitter<boolean>();
  
  selectedFile: File | null = null;
  loading = false;
  error = '';
  success = '';
  progress = 0;
  uploadResponse?: UploadResponse;
  failedEntries: any[] = [];
  showDownloadOption: boolean = false;

  constructor(private inventoryService: InventoryService) {}

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

    this.inventoryService.uploadInventory(this.selectedFile).subscribe({
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
          this.success = `Successfully uploaded all ${response.totalRows} inventory items`;
          
          // Close modal after successful upload with a delay
          setTimeout(() => {
            this.close(true);
          }, 2000);
        } else if (response.successCount === 0) {
          this.showDownloadOption = true;
          // Convert errors to failedEntries format
          this.failedEntries = response.errors.map(error => ({
            row: error.rowNumber,
            data: error.data,
            errorMessage: error.message
          }));
          console.log('Failed entries prepared:', this.failedEntries);
          this.error = `Failed to upload any inventory items. All ${response.errorCount} entries had errors.`;
        } else {
          this.showDownloadOption = true;
          // Convert errors to failedEntries format
          this.failedEntries = response.errors.map(error => ({
            row: error.rowNumber,
            data: error.data,
            errorMessage: error.message
          }));
          console.log('Failed entries prepared:', this.failedEntries);
          this.success = `Successfully uploaded ${response.successCount} out of ${response.totalRows} inventory items. ` + 
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
    console.log('Download button clicked');
    console.log('Failed entries to download:', this.failedEntries);
    
    if (!this.failedEntries || this.failedEntries.length === 0) {
      console.log('No failed entries to download');
      return;
    }
    
    try {
      // Create CSV content
      const headers = ['Row', 'Barcode', 'Quantity', 'Error Message'];
      let csvContent = headers.join(',') + '\n';
      
      this.failedEntries.forEach(entry => {
        console.log('Processing entry:', entry);
        
        // Handle different data formats
        let barcode = '', quantity = '';
        
        try {
          if (entry.data && typeof entry.data === 'string') {
            // Try to extract data from the entry string
            const dataStr = entry.data;
            
            // Extract using regex patterns
            const barcodeMatch = dataStr.match(/Barcode: ([^,]+)/);
            const quantityMatch = dataStr.match(/Quantity: ([^,]+)/);
            
            barcode = barcodeMatch ? barcodeMatch[1] : '';
            quantity = quantityMatch ? quantityMatch[1] : '';
          }
        } catch (e) {
          console.error('Error parsing entry data:', e);
        }
        
        // Create CSV row with proper escaping for CSV format
        const rowData = [
          entry.row || entry.rowNumber || '',
          barcode,
          quantity,
          entry.errorMessage || entry.message || ''
        ].map(value => {
          // Escape quotes and wrap in quotes
          if (value === null || value === undefined) return '""';
          return '"' + String(value).replace(/"/g, '""') + '"';
        }).join(',');
        
        csvContent += rowData + '\n';
      });
      
      console.log('Generated CSV content:', csvContent);
      
      // Create and trigger download
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      
      // Create link element
      const link = document.createElement('a');
      link.setAttribute('href', url);
      link.setAttribute('download', 'failed_inventory_entries.csv');
      document.body.appendChild(link);
      
      console.log('Download link created, clicking...');
      link.click();
      
      // Clean up
      setTimeout(() => {
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        console.log('Download cleanup completed');
      }, 100);
    } catch (error) {
      console.error('Error in download process:', error);
    }
  }

  downloadSampleTsv() {
    console.log('Download sample inventory TSV method called');
    // Sample data with header and two example rows
    const sampleData = 'Barcode\tQuantity\nAPPL010\t100\nSMSG001\t50';
    
    // Create a blob with the sample data
    const blob = new Blob([sampleData], { type: 'text/tab-separated-values' });
    const url = URL.createObjectURL(blob);
    
    // Create a link element and trigger download
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', 'sample_inventory.tsv');
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
} 