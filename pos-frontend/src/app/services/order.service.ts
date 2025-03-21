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

  getOrders(page: number = 0, size: number = 10): Observable<Order[]> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Order[]>(`${this.baseUrl}/orders`, { params }).pipe(
      map(orders => orders.map(order => this.formatOrder(order)))
    );
  }

  getOrder(id: number): Observable<Order> {
    return this.http.get<any>(`${this.baseUrl}/orders/${id}`).pipe(
      map(order => this.formatOrder(order))
    );
  }

  createOrder(orderData: any): Observable<Order> {
    return this.http.post<Order>(`${this.baseUrl}/orders`, orderData);
  }

  getOrdersByDateRange(startDate: Date, endDate: Date, page: number = 0, size: number = 10): Observable<Order[]> {
    // Format dates to ISO string for the API
    const startIso = startDate.toISOString();
    const endIso = endDate.toISOString();
    
    // Build URL with query parameters
    const url = `${this.baseUrl}/orders/filter/date?startDate=${startIso}&endDate=${endIso}&page=${page}&size=${size}`;
    
    return this.http.get<any[]>(url).pipe(
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

  downloadInvoice(orderId: number): Observable<Blob> {
    return this.http.get(`/api/orders/${orderId}/invoice`, { responseType: 'blob' });
  }
} 