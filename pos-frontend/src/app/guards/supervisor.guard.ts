import { Injectable } from '@angular/core';
import { Router, CanActivate, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class SupervisorGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): Observable<boolean | UrlTree> {
    return this.authService.currentUser$.pipe(
      take(1),
      map(user => {
        if (user?.role === 'SUPERVISOR') {
          return true;
        }
        
        // If not supervisor, redirect to the parent route
        console.log('Access denied: User is not a supervisor');
        const currentUrl = this.router.url;
        const parentUrl = currentUrl.substring(0, currentUrl.lastIndexOf('/'));
        return this.router.createUrlTree([parentUrl]);
      })
    );
  }
} 