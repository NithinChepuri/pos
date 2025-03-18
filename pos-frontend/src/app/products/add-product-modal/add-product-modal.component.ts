import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../services/product.service';
import { Client } from '../../models/client';
import { ToastService } from '../../services/toast.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-add-product-modal',
  templateUrl: './add-product-modal.component.html',
  styleUrls: ['./add-product-modal.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class AddProductModalComponent implements OnInit {
  @Output() closeModal = new EventEmitter<boolean>();
  @Input() clients: Client[] = [];
  
  newProduct = {
    name: '',
    barcode: '',
    clientId: 0,
    mrp: 0
  };
  
  loading = false;
  error = '';
  fieldErrors: {[key: string]: string} = {};

  constructor(
    private productService: ProductService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    // Initialize with first client if available
    if (this.clients.length > 0) {
      this.newProduct.clientId = this.clients[0].id;
    }
  }

  onSubmit(): void {
    this.loading = true;
    this.error = '';
    this.fieldErrors = {};
    
    this.productService.createProduct(this.newProduct).subscribe({
      next: () => {
        this.loading = false;
        this.closeModal.emit(true); // true indicates success and data should be refreshed
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        this.handleError(err);
      }
    });
  }

  handleError(err: HttpErrorResponse): void {
    console.error('Error adding product:', err);
    
    if (err.status === 400) {
      // Handle validation errors
      if (err.error && typeof err.error === 'object') {
        if (err.error.fieldErrors) {
          // Spring validation errors format
          for (const field in err.error.fieldErrors) {
            this.fieldErrors[field] = err.error.fieldErrors[field];
          }
          this.error = 'Please correct the errors in the form.';
        } else if (err.error.message) {
          // General error message
          this.error = err.error.message;
        } else if (err.error.error) {
          // Another common format
          this.error = err.error.error;
        } else {
          // Try to extract field-specific errors
          for (const key in err.error) {
            if (typeof err.error[key] === 'string') {
              this.fieldErrors[key] = err.error[key];
            }
          }
          
          if (Object.keys(this.fieldErrors).length > 0) {
            this.error = 'Please correct the errors in the form.';
          } else {
            this.error = 'Invalid data provided. Please check your inputs.';
          }
        }
      } else if (typeof err.error === 'string') {
        this.error = err.error;
      } else {
        this.error = 'Bad request: Please check your inputs.';
      }
    } else if (err.status === 409) {
      this.error = 'A product with this barcode already exists.';
    } else {
      this.error = 'An error occurred while adding the product. Please try again.';
    }
  }

  getFieldError(fieldName: string): string {
    return this.fieldErrors[fieldName] || '';
  }

  hasFieldError(fieldName: string): boolean {
    return !!this.fieldErrors[fieldName];
  }

  cancel(): void {
    this.closeModal.emit(false);
  }
} 