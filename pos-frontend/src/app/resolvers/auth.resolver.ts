import { Injectable } from '@angular/core';
import { Resolve } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { take } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthResolver implements Resolve<any> {
  constructor(private authService: AuthService) {}

  resolve() {
    return this.authService.getCurrentUser().pipe(take(1));
  }
} 