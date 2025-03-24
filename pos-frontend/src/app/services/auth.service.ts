import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError, of } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';
import { User, SignupRequest, LoginRequest } from '../models/user';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = '/employee/api/auth';
  private userSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.userSubject.asObservable();
  private isInitializing = true;

  constructor(private http: HttpClient, private router: Router) {
    // Initialize from localStorage first
    this.loadUserFromStorage();
  }

  private loadUserFromStorage(): void {
    try {
      const storedUser = localStorage.getItem('currentUser');
      if (storedUser) {
        const user = JSON.parse(storedUser);
        this.userSubject.next(user);
      }
    } catch (e) {
      console.error('Error loading user from storage:', e);
      localStorage.removeItem('currentUser');
    } finally {
      this.isInitializing = false;
    }
  }

  public checkCurrentUser(): void {
    console.log('Checking current user session...');
    this.http.get<User>(`${this.baseUrl}/user`).subscribe({
      next: (user) => {
        console.log('Current user session:', user);
        this.userSubject.next(user);
        localStorage.setItem('currentUser', JSON.stringify(user));
      },
      error: (error) => {
        console.log('No active session:', error);
        this.userSubject.next(null);
        localStorage.removeItem('currentUser');
        
        // Only redirect if not initializing
        if (!this.isInitializing) {
          this.router.navigate(['/login']);
        }
      }
    });
  }

  public refreshUser(): void {
    // First check localStorage to ensure we have a user during page reloads
    this.loadUserFromStorage();
    
    // Then verify with the server, but don't redirect on failure during page load
    this.isInitializing = true;
    
    this.http.get<User>(`${this.baseUrl}/info`)
      .pipe(
        catchError(error => {
          console.error('Auth refresh failed:', error);
          // Don't clear user during initialization to prevent flicker
          if (!this.isInitializing) {
            this.userSubject.next(null);
            localStorage.removeItem('currentUser');
          }
          return of(null);
        }),
        tap(() => {
          this.isInitializing = false;
        })
      )
      .subscribe(user => {
        if (user) {
          this.userSubject.next(user);
          localStorage.setItem('currentUser', JSON.stringify(user));
        }
      });
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
        this.userSubject.next(user);
        localStorage.setItem('currentUser', JSON.stringify(user));
      })
    );
  }

  logout(): Observable<any> {
    return this.http.post(`${this.baseUrl}/logout`, {})
      .pipe(
        tap(() => {
          this.userSubject.next(null);
          localStorage.removeItem('currentUser');
          this.router.navigate(['/login']);
        }),
        catchError(error => {
          // Even if the server logout fails, clear local state
          this.userSubject.next(null);
          localStorage.removeItem('currentUser');
          this.router.navigate(['/login']);
          return of(null);
        })
      );
  }

  isAuthenticated(): boolean {
    return !!this.userSubject.value;
  }

  isSupervisor(): boolean {
    return this.userSubject.value?.role === 'SUPERVISOR';
  }

  getCurrentUser(): User | null {
    return this.userSubject.value;
  }

  getUsername(): string {
    const user = this.getCurrentUser();
    return user ? user.name : '';
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  hasRole(role: string): boolean {
    const user = this.userSubject.value;
    return !!user && user.role === role;
  }
} 