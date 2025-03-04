import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { UploadResponse } from '../../models/upload-response';

@Component({
  selector: 'app-upload-product',
  templateUrl: './upload-product.component.html',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule]
})
export class UploadProductComponent {
  selectedFile: File | null = null;
  loading = false;
  error = '';
  success = '';
  progress = 0;
  uploadResponse?: UploadResponse;

  constructor(private productService: ProductService) {}

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
        console.log('Upload response:', response); // Debug log
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
        } else if (response.successCount === 0) {
          this.error = `Failed to upload any products. All ${response.errorCount} entries had errors.`;
        } else {
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
} 