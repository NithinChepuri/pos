import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SalesReportItem } from '../models/sales-report';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  // Use the correct API URL
  private baseUrl = '/employee/api';

  constructor(private http: HttpClient) {}

  getSalesReport(startDate: string, endDate: string): Observable<SalesReportItem[]> {
    // Use HttpParams to properly encode the parameters
    const params = new HttpParams()
      .set('startDate', this.formatDateForApi(startDate))
      .set('endDate', this.formatDateForApi(endDate));
    
    return this.http.get<SalesReportItem[]>(`${this.baseUrl}/reports/sales`, { params });
  }

  private formatDateForApi(dateTimeString: string): string {
    if (!dateTimeString) return '';
    
    // Create a date object from the input
    const date = new Date(dateTimeString);
    
    // Format with timezone information
    return date.toISOString();
  }
} 