import { Injectable } from '@angular/core';
import { 
  CanActivate, 
  Router, 
  UrlTree
} from '@angular/router';
import { Observable, map, take } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): Observable<boolean | UrlTree> {
    return this.authService.currentUser$.pipe(
      take(1),
      map(user => {
        console.log('AuthGuard checking user:', user);
        
        // If user is authenticated, allow access
        if (user) {
          return true;
        }

        // Not authenticated, redirect to login
        console.log('AuthGuard: User not authenticated, redirecting to login');
        return this.router.createUrlTree(['/login']);
      })
    );
  }
} 