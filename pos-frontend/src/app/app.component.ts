import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';
import { User } from './models/user';
import { ToastComponent } from './shared/toast/toast.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, ToastComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'POS System';
  user: User | null = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.authService.currentUser$.subscribe(
      user => {
        console.log('App user state changed:', user);
        this.user = user;
        if (!user && !this.isAuthRoute()) {
          console.log('No user detected, redirecting to login');
          this.router.navigate(['/login']);
        }
      }
    );
  }

  private isAuthRoute(): boolean {
    const currentPath = this.router.url;
    return currentPath.includes('/login') || currentPath.includes('/signup');
  }

  ngOnInit() {
    this.authService.refreshUser();
  }

  logout(): void {
    console.log('Initiating logout...');
    this.authService.logout().subscribe({
      next: () => {
        console.log('Logout successful');
      },
      error: (error) => {
        console.error('Logout failed:', error);
      }
    });
  }
}
