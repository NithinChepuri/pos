import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {
  email = '';
  password = '';
  loading = false;
  error = '';
  passwordValidation = {
    minLength: false,
    hasUpperCase: false,
    hasLowerCase: false,
    hasNumber: false,
    hasSpecialChar: false
  };
  showPassword = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  validatePassword(password: string): boolean {
    // Reset validation state
    this.passwordValidation = {
      minLength: password.length >= 8,
      hasUpperCase: /[A-Z]/.test(password),
      hasLowerCase: /[a-z]/.test(password),
      hasNumber: /[0-9]/.test(password),
      hasSpecialChar: /[!@#$%^&*(),.?":{}|<>]/.test(password)
    };

    // Return true if all validations pass
    return Object.values(this.passwordValidation).every(value => value === true);
  }

  onPasswordChange(): void {
    this.validatePassword(this.password);
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    if (!this.email || !this.password) {
      this.error = 'Please fill in all fields';
      return;
    }

    if (!this.validatePassword(this.password)) {
      this.error = 'Please ensure your password meets all requirements';
      return;
    }

    this.loading = true;
    this.error = '';

    const signupData = {
      email: this.email.toLowerCase().trim(),
      password: this.password
    };

    this.authService.signup(signupData).subscribe({
      next: (user) => {
        console.log('Signup successful:', user);
        this.router.navigate(['/login']);
      },
      error: (error: HttpErrorResponse) => {
        console.error('Signup error:', error);
        this.loading = false;
        
        if (error.error && typeof error.error === 'object' && error.error.error) {
          this.error = error.error.error;
        } else if (error.error && typeof error.error === 'string') {
          this.error = error.error;
        } else if (error.status === 400) {
          this.error = 'User already exists or invalid input';
        } else {
          this.error = error.message || 'Failed to create account';
        }
      }
    });
  }
} 