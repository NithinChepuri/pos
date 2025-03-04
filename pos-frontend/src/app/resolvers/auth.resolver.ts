import { Injectable } from '@angular/core';
import { Resolve } from '@angular/router';
import { Observable } from 'rxjs';
import { take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { User } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class AuthResolver implements Resolve<User | null> {
  constructor(private authService: AuthService) {}

  resolve(): Observable<User | null> {
    return this.authService.currentUser$.pipe(take(1));
  }
} 