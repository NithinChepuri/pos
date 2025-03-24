import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}
  
  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {
    // Always check localStorage first
    const storedUser = localStorage.getItem('currentUser');
    if (storedUser) {
      return true;
    }
    
    // Then check in-memory state
    if (this.authService.isAuthenticated()) {
      return true;
    }
    
    // If neither check passes, redirect to login
    this.router.navigate(['/login'], { queryParams: { returnUrl: state.url }});
    return false;
  }
} 