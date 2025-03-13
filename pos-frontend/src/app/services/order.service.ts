import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';
import { Order, OrderStatus } from '../models/order';
import { Product } from '../models/product';
import { saveAs } from 'file-saver';

export type OrderSearchType = 'date' | 'status' | 'all';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private baseUrl = '/employee/api';

  constructor(private http: HttpClient) { }

  private formatDate(dateObj: any): string {
    if (!dateObj) return '';
    
    // Handle Java ZonedDateTime format
    if (typeof dateObj === 'object') {
      if ('year' in dateObj) {
        // Convert Java date fields to ISO string
        const date = new Date(
          dateObj.year,
          dateObj.monthValue - 1,
          dateObj.dayOfMonth,
          dateObj.hour,
          dateObj.minute,
          dateObj.second,
          dateObj.nano / 1000000
        );

        // Adjust for timezone offset
        if (dateObj.offset?.totalSeconds) {
          const localOffset = date.getTimezoneOffset() * 60;
          const targetOffset = dateObj.offset.totalSeconds;
          const offsetDiff = targetOffset + localOffset;
          date.setSeconds(date.getSeconds() + offsetDiff);
        }

        return date.toISOString();
      }
    }
    return dateObj;
  }

  private formatOrder(order: any): Order {
    return {
      ...order,
      createdAt: this.formatDate(order.createdAt),
      status: order.status as OrderStatus
    };
  }

  getOrders(): Observable<Order[]> {
    return this.http.get<any[]>(`${this.baseUrl}/orders`).pipe(
      map(orders => orders.map(order => this.formatOrder(order)))
    );
  }

  getOrder(id: number): Observable<Order> {
    return this.http.get<any>(`${this.baseUrl}/orders/${id}`).pipe(
      map(order => this.formatOrder(order))
    );
  }

  createOrder(orderData: any): Observable<Order> {
    console.log('OrderService sending data:', orderData);
    return this.http.post<Order>(`${this.baseUrl}/orders`, orderData)
      .pipe(
        tap(response => console.log('Server response:', response)),
        catchError(error => {
          console.error('Server error:', error);
          throw error;
        })
      );
  }

  getOrdersByDateRange(startDate: Date, endDate: Date): Observable<Order[]> {
    const params = new HttpParams()
      .set('startDate', startDate.toISOString())
      .set('endDate', endDate.toISOString());

    return this.http.get<any[]>(`${this.baseUrl}/orders/filter`, { params }).pipe(
      map(orders => orders.map(order => this.formatOrder(order)))
    );
  }

  generateInvoice(orderId: number): Observable<Blob> {
    return this.http.post(`${this.baseUrl}/orders/${orderId}/invoice`, null, {
      responseType: 'blob'
    }).pipe(
      tap((blob: Blob) => {
        saveAs(blob, `invoice_${orderId}.pdf`);
      }),
      catchError(error => {
        console.error('Error downloading invoice:', error);
        throw error;
      })
    );
  }
} 