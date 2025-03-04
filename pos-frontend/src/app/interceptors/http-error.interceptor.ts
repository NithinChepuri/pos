import { 
  HttpRequest, 
  HttpHandlerFn,
  HttpErrorResponse 
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export function httpErrorInterceptor(
  req: HttpRequest<unknown>, 
  next: HttpHandlerFn
) {
  const router = inject(Router);
  const authService = inject(AuthService);

  // Log outgoing request
  console.log('Outgoing Request:', {
    url: req.url,
    method: req.method,
    headers: req.headers,
    body: req.body
  });

  return next(req).pipe(
    tap(response => {
      if (response.type !== 0) { // Skip progress events
        console.log(`API Response for ${req.url}:`, response);
      }
    }),
    catchError((error: HttpErrorResponse) => {
      console.error('API Error Details:', {
        url: req.url,
        method: req.method,
        requestBody: req.body,
        errorStatus: error.status,
        errorStatusText: error.statusText,
        errorMessage: error.message,
        errorBody: error.error
      });

      if (error.status === 403) {
        // If forbidden, check if it's an authentication issue
        authService.checkCurrentUser();
      }

      return throwError(() => error);
    })
  );
} 