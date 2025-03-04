import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';
import { User, SignupRequest, LoginRequest } from '../models/user';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = '/employee/api/auth';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {
    this.checkCurrentUser();
  }

  public checkCurrentUser(): void {
    console.log('Checking current user session...');
    this.http.get<User>(`${this.baseUrl}/user`).subscribe({
      next: (user) => {
        console.log('Current user session:', user);
        this.currentUserSubject.next(user);
      },
      error: (error) => {
        console.log('No active session:', error);
        this.currentUserSubject.next(null);
        this.router.navigate(['/login']);
      }
    });
  }

  public refreshUser(): void {
    this.checkCurrentUser();
  }

  signup(request: SignupRequest): Observable<User> {
    const normalizedRequest = {
      email: request.email.toLowerCase().trim(),
      password: request.password
    };

    console.log('Sending signup request:', normalizedRequest);

    return this.http.post<User>(`${this.baseUrl}/signup`, normalizedRequest).pipe(
      tap(user => console.log('Signup response:', user)),
      catchError(error => {
        if (error instanceof HttpErrorResponse) {
          console.error('HTTP Error:', {
            status: error.status,
            statusText: error.statusText,
            error: error.error
          });

          if (error.status === 400) {
            throw new Error('Invalid email or password format');
          }
          if (error.status === 409) {
            throw new Error('Email already exists');
          }
          
          throw new Error(error.error?.message || `Server error occurred (${error.status})`);
        }
        throw error;
      })
    );
  }

  login(request: LoginRequest): Observable<User> {
    const normalizedRequest = {
      email: request.email.toLowerCase().trim(),
      password: request.password
    };

    return this.http.post<User>(`${this.baseUrl}/login`, normalizedRequest).pipe(
      tap(user => {
        this.currentUserSubject.next(user);
      })
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/logout`, {}).pipe(
      tap(() => {
        console.log('Logging out user...');
        this.currentUserSubject.next(null);
        this.router.navigate(['/login']);
      }),
      catchError(error => {
        console.error('Logout error:', error);
        // Even if the server request fails, clear the local state
        this.currentUserSubject.next(null);
        this.router.navigate(['/login']);
        throw error;
      })
    );
  }

  isAuthenticated(): boolean {
    const currentUser = this.currentUserSubject.value;
    const isAuth = !!currentUser;
    console.log('Authentication check:', { isAuth, currentUser });
    return isAuth;
  }

  isSupervisor(): boolean {
    return this.currentUserSubject.value?.role === 'SUPERVISOR';
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  getUsername(): string {
    const user = this.getCurrentUser();
    return user ? user.name : '';
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }
} 