import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container mt-5">
      <div class="row justify-content-center">
        <div class="col-md-6">
          <div class="card">
            <div class="card-body">
              <h3 class="card-title text-center mb-4">Login</h3>
              
              <div *ngIf="error" class="alert alert-danger">
                {{ error }}
              </div>

              <form (ngSubmit)="onSubmit()">
                <div class="mb-3">
                  <label class="form-label">Email</label>
                  <input 
                    type="email" 
                    class="form-control" 
                    [(ngModel)]="email" 
                    name="email" 
                    required>
                </div>

                <div class="mb-3">
                  <label class="form-label">Password</label>
                  <input 
                    type="password" 
                    class="form-control" 
                    [(ngModel)]="password" 
                    name="password" 
                    required>
                </div>

                <button 
                  type="submit" 
                  class="btn btn-primary w-100"
                  [disabled]="loading">
                  {{ loading ? 'Loading...' : 'Login' }}
                </button>
              </form>

              <div class="text-center mt-3">
                <a routerLink="/signup">Don't have an account? Sign up</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class LoginComponent {
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

    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (error) => {
        console.error('Login error:', error);
        this.error = 'Invalid email or password';
        this.loading = false;
      }
    });
  }
} 