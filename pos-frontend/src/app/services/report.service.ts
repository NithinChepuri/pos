import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SalesReportItem } from '../models/sales-report';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private baseUrl = '/employee/api/reports';

  constructor(private http: HttpClient) {}

  getSalesReport(startDate: string, endDate: string): Observable<SalesReportItem[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    return this.http.get<SalesReportItem[]>(`${this.baseUrl}/sales`, { params });
  }
} 