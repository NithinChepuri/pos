import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DailySalesData } from '../models/daily-sales';

@Injectable({
  providedIn: 'root'
})
export class DailySalesService {
  private baseUrl = '/employee/api/daily-sales';

  constructor(private http: HttpClient) {}

  /**
   * Get all daily sales data
   */
  getDailySales(): Observable<DailySalesData[]> {
    return this.http.get<DailySalesData[]>(this.baseUrl);
  }

  /**
   * Get daily sales data for a specific date
   */
  getDailySalesByDate(date: string): Observable<DailySalesData> {
    return this.http.get<DailySalesData>(`${this.baseUrl}/date/${date}`);
  }

  /**
   * Get daily sales data for a date range
   */
  getDailySalesByDateRange(startDate: string, endDate: string): Observable<DailySalesData[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    
    return this.http.get<DailySalesData[]>(`${this.baseUrl}/range`, { params });
  }

  /**
   * Get the latest daily sales data
   */
  getLatestDailySales(): Observable<DailySalesData> {
    return this.http.get<DailySalesData>(`${this.baseUrl}/latest`);
  }
} 