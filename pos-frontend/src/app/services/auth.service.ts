import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
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

    return this.http.post<User>(`${this.baseUrl}/signup`, normalizedRequest)
      .pipe(
        tap(user => console.log('Signup response:', user)),
        catchError((error: HttpErrorResponse) => {
          console.log('HTTP Error:', error);
          
          // Log the full error object to see its structure
          console.log('Error object details:', {
            status: error.status,
            message: error.message,
            error: error.error,
            name: error.name
          });
          
          // Extract the exact error message from the backend
          let errorMessage = 'Failed to create account';
          
          if (error.error) {
            if (typeof error.error === 'object' && error.error.error) {
              // If the error has a structured format with an 'error' property
              errorMessage = error.error.error;
            } else if (typeof error.error === 'string') {
              // If the error is a plain string
              errorMessage = error.error;
            }
          }
          
          return throwError(() => new Error(errorMessage));
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