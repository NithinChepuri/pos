import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SalesReportItem } from '../models/sales-report';
import { Client } from '../models/client';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  // Use the correct API URL
  private baseUrl = '/employee/api';

  constructor(private http: HttpClient) {}

  getSalesReport(startDate: string, endDate: string, clientId?: number): Observable<SalesReportItem[]> {
    // Use HttpParams to properly encode the parameters
    let params = new HttpParams()
      .set('startDate', this.formatDateForApi(startDate))
      .set('endDate', this.formatDateForApi(endDate));
    
    // Add clientId parameter if provided
    if (clientId) {
      params = params.set('clientId', clientId.toString());
    }
    
    return this.http.get<SalesReportItem[]>(`${this.baseUrl}/report/sales`, { params });
  }

  // Add method to get all clients for the dropdown
  getAllClients(): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.baseUrl}/client`);
  }

  private formatDateForApi(dateTimeString: string): string {
    if (!dateTimeString) return '';
    
    // Create a date object from the input
    const date = new Date(dateTimeString);
    
    // Format with timezone information
    return date.toISOString();
  }
} 