import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = '/employee/api/auth';
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  private userRoleSubject = new BehaviorSubject<string>('');

  constructor(private http: HttpClient) {
    // Check if user is already logged in
    this.getCurrentUser().subscribe(
      () => this.isAuthenticatedSubject.next(true),
      () => this.isAuthenticatedSubject.next(false)
    );
  }

  login(email: string, password: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/login`, { email, password }, { withCredentials: true })
      .pipe(
        tap((user: any) => {
          this.isAuthenticatedSubject.next(true);
          this.userRoleSubject.next(user.role);
        })
      );
  }

  signup(userData: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/signup`, userData, { withCredentials: true });
  }

  logout(): Observable<any> {
    return this.http.post(`${this.baseUrl}/logout`, {}, { withCredentials: true })
      .pipe(
        tap(() => {
          this.isAuthenticatedSubject.next(false);
          this.userRoleSubject.next('');
        })
      );
  }

  getCurrentUser(): Observable<any> {
    return this.http.get(`${this.baseUrl}/user`, { withCredentials: true })
      .pipe(
        tap((user: any) => {
          this.isAuthenticatedSubject.next(true);
          this.userRoleSubject.next(user.role);
        })
      );
  }

  isAuthenticated(): Observable<boolean> {
    return this.isAuthenticatedSubject.asObservable();
  }

  getUserRole(): Observable<string> {
    return this.userRoleSubject.asObservable();
  }
} 