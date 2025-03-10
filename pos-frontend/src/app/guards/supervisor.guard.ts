import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map, take } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class SupervisorGuard {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate() {
    return this.authService.getUserRole().pipe(
      take(1),
      map(role => {
        if (role !== 'SUPERVISOR') {
          this.router.navigate(['/dashboard']);
          return false;
        }
        return true;
      })
    );
  }
} 