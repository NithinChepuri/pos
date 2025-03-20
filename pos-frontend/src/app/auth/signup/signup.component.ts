import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {
  email = '';
  password = '';
  loading = false;
  error = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    if (!this.email || !this.password) {
      this.error = 'Please fill in all fields';
      return;
    }

    this.loading = true;
    this.error = '';

    const signupData = {
      email: this.email.toLowerCase().trim(),
      password: this.password
    };

    console.log('Submitting signup data:', signupData);

    this.authService.signup(signupData).subscribe({
      next: (user) => {
        console.log('Signup successful:', user);
        this.router.navigate(['/login']);
      },
      error: (error: HttpErrorResponse) => {
        console.error('Signup error:', error);
        this.loading = false;
        
        // Extract the error message from the response
        if (error.error && typeof error.error === 'object' && error.error.error) {
          // If the error has a structured format with an 'error' property
          this.error = error.error.error;
        } else if (error.error && typeof error.error === 'string') {
          // If the error is a plain string
          this.error = error.error;
        } else if (error.status === 400) {
          // If it's a 400 Bad Request but we couldn't extract a specific message
          this.error = 'User already exists or invalid input';
        } else {
          // Fallback error message
          this.error = error.message || 'Failed to create account';
        }
      }
    });
  }
} 