import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

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
      error: (error) => {
        console.error('Signup error:', error);
        this.loading = false;
        this.error = error.message || 'Failed to create account';
      }
    });
  }
} 