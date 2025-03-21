import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClientService } from '../../services/client.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-add-client-modal',
  templateUrl: './add-client-modal.component.html',
  styleUrls: ['./add-client-modal.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class AddClientModalComponent implements OnInit {
  @Output() closeModal = new EventEmitter<boolean>();
  
  client = {
    name: '',
    email: '',
    phoneNumber: ''
  };
  
  loading = false;
  error = '';
  fieldErrors: {[key: string]: string} = {};

  constructor(
    private clientService: ClientService
  ) { }

  ngOnInit(): void {
    // Any initialization if needed
  }

  onSubmit(): void {
    this.loading = true;
    this.error = '';
    this.fieldErrors = {};
    
    const clientData = {
      name: this.client.name.toLowerCase().trim(),
      email: this.client.email.toLowerCase().trim(),
      phoneNumber: this.client.phoneNumber.trim()
    };

    this.clientService.createClient(clientData)
      .subscribe({
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
    console.error('Error creating client:', err);
    
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
      this.error = 'A client with this email or phone number already exists.';
    } else {
      this.error = 'An error occurred while creating the client. Please try again.';
    }
    
    // No toast notification - errors only shown in the popup
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