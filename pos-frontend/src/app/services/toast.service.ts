import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info' | 'warning';
  timeout?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toasts: BehaviorSubject<Toast[]> = new BehaviorSubject<Toast[]>([]);
  private nextId = 0;

  constructor() {}

  getToasts(): Observable<Toast[]> {
    return this.toasts.asObservable();
  }

  showSuccess(message: string, timeout: number = 5000): void {
    this.show(message, 'success', timeout);
  }

  showError(message: string, timeout: number = 5000): void {
    this.show(message, 'error', timeout);
  }

  showInfo(message: string, timeout: number = 5000): void {
    this.show(message, 'info', timeout);
  }

  showWarning(message: string, timeout: number = 5000): void {
    this.show(message, 'warning', timeout);
  }

  private show(message: string, type: 'success' | 'error' | 'info' | 'warning', timeout: number): void {
    const id = this.nextId++;
    const toast: Toast = { id, message, type, timeout };
    
    const currentToasts = this.toasts.getValue();
    this.toasts.next([...currentToasts, toast]);
    
    if (timeout > 0) {
      setTimeout(() => this.remove(id), timeout);
    }
  }

  remove(id: number): void {
    const currentToasts = this.toasts.getValue();
    this.toasts.next(currentToasts.filter(toast => toast.id !== id));
  }
} 