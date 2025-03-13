import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ClientService } from '../../services/client.service';

@Component({
  selector: 'app-add-client',
  templateUrl: './add-client.component.html',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styles: [` 
    /* Add your styles here if needed */
  `]
})
export class AddClientComponent {
  client = {
    name: '',
    email: '',
    phoneNumber: ''
  };
  error = '';

  constructor(
    private clientService: ClientService,
    private router: Router
  ) { }

  onSubmit(): void {
    const clientData = {
      name: this.client.name.toLowerCase().trim(),
      email: this.client.email.toLowerCase().trim(),
      phoneNumber: this.client.phoneNumber.trim()
    };

    this.clientService.createClient(clientData)
      .subscribe({
        next: () => {
          this.router.navigate(['/clients']);
          this.error = '';
        },
        error: (error) => {
          console.error('Error creating client:', error);
          this.error = error.error?.error || 'An error occurred while creating the client.';
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/clients']);
  }
} 