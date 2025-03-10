import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'POS System';
  isAuthenticated = false;
  userRole = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    // Subscribe to auth status
    this.authService.isAuthenticated().subscribe(
      isAuth => {
        this.isAuthenticated = isAuth;
        if (!isAuth && !this.isAuthRoute()) {
          this.router.navigate(['/login']);
        }
      }
    );

    // Subscribe to user role
    this.authService.getUserRole().subscribe(
      role => this.userRole = role
    );
  }

  private isAuthRoute(): boolean {
    const currentPath = this.router.url;
    return currentPath.includes('/login') || currentPath.includes('/signup');
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: (error) => {
        console.error('Logout failed:', error);
      }
    });
  }
}
