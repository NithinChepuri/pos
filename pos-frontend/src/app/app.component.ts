import { Component, OnInit, HostListener } from '@angular/core';
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
  isPageReload = true;
  isNavbarCollapsed = true;
  isMobileView = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    // Check window width on initialization
    this.checkScreenSize();
    
    // Check if there's a stored user in localStorage first
    const storedUser = localStorage.getItem('currentUser');
    if (storedUser) {
      try {
        this.user = JSON.parse(storedUser);
      } catch (e) {
        console.error('Error parsing stored user:', e);
      }
    }

    // Subscribe to auth changes
    this.authService.currentUser$.subscribe(
      user => {
        console.log('App user state changed:', user);
        this.user = user;
        
        // Only redirect if:
        // 1. User is not authenticated
        // 2. Not on an auth route
        // 3. Not during initial page load/reload
        if (!user && !this.isAuthRoute() && !this.isPageReload) {
          console.log('No user detected, redirecting to login');
          this.router.navigate(['/login']);
        }
        
        // After first auth check, mark page reload as complete
        this.isPageReload = false;
      }
    );
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.checkScreenSize();
  }

  private checkScreenSize() {
    this.isMobileView = window.innerWidth < 992; // Bootstrap's lg breakpoint
    
    // Auto-collapse navbar in mobile view
    if (!this.isMobileView) {
      this.isNavbarCollapsed = true;
    }
  }

  private isAuthRoute(): boolean {
    const currentPath = this.router.url;
    return currentPath.includes('/login') || currentPath.includes('/signup');
  }

  ngOnInit() {
    // Refresh user but don't redirect during page load
    this.authService.refreshUser();
    
    // After a short delay, mark initial load as complete
    setTimeout(() => {
      this.isPageReload = false;
    }, 1000);
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

  toggleNavbar(): void {
    console.log('Toggling navbar, current state:', this.isNavbarCollapsed);
    this.isNavbarCollapsed = !this.isNavbarCollapsed;
    console.log('New navbar state:', this.isNavbarCollapsed);
  }
  
  collapseNavbar(): void {
    if (this.isMobileView) {
      this.isNavbarCollapsed = true;
    }
  }
}
