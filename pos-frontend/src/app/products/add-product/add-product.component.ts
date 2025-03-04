import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { UploadResponse } from '../../models/upload-response';

@Component({
  selector: 'app-add-product',
  templateUrl: './add-product.component.html',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule]
})
export class AddProductComponent {
  selectedFile: File | null = null;
  loading = false;
  error = '';
  uploadResponse?: UploadResponse;

  constructor(private productService: ProductService) {}

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.name.endsWith('.tsv')) {
        this.selectedFile = file;
        this.error = '';
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
    this.uploadResponse = undefined;

    this.productService.uploadProducts(this.selectedFile).subscribe({
      next: (response: UploadResponse) => {
        this.uploadResponse = response;
        this.loading = false;
      },
      error: (error) => {
        console.error('Upload error:', error);
        this.error = 'Failed to process file. Please try again.';
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