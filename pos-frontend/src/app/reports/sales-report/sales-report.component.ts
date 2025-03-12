import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportService } from '../../services/report.service';
import { SalesReportItem } from '../../models/sales-report';

@Component({
  selector: 'app-sales-report',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <h2 class="mb-4">Sales Report</h2>
      
      <div class="row mb-4">
        <div class="col-md-5">
          <div class="form-group">
            <label for="startDate">Start Date</label>
            <input 
              type="datetime-local" 
              class="form-control" 
              id="startDate"
              [(ngModel)]="startDate"
              (change)="loadReport()">
          </div>
        </div>
        <div class="col-md-5">
          <div class="form-group">
            <label for="endDate">End Date</label>
            <input 
              type="datetime-local" 
              class="form-control" 
              id="endDate"
              [(ngModel)]="endDate"
              (change)="loadReport()">
          </div>
        </div>
      </div>

      <div class="table-responsive" *ngIf="salesData.length > 0">
        <table class="table table-striped">
          <thead>
            <tr>
              <th>Barcode</th>
              <th>Product Name</th>
              <th class="text-end">Quantity</th>
              <th class="text-end">Revenue</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let item of salesData">
              <td>{{item.barcode}}</td>
              <td>{{item.productName}}</td>
              <td class="text-end">{{item.quantity}}</td>
              <td class="text-end">{{item.revenue | currency}}</td>
            </tr>
            <tr class="table-info fw-bold">
              <td colspan="2">Total</td>
              <td class="text-end">{{getTotalQuantity()}}</td>
              <td class="text-end">{{getTotalRevenue() | currency}}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div *ngIf="salesData.length === 0" class="alert alert-info">
        No sales data available for the selected date range.
      </div>
    </div>
  `,
  styles: [`
    .form-group {
      margin-bottom: 1rem;
    }
  `]
})
export class SalesReportComponent {
  startDate = '';
  endDate = '';
  salesData: SalesReportItem[] = [];

  constructor(private reportService: ReportService) {
    // Set default dates to current month
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    
    this.startDate = this.formatDateTimeForInput(firstDay);
    this.endDate = this.formatDateTimeForInput(today);
    
    this.loadReport();
  }

  formatDateTimeForInput(date: Date): string {
    // Format: YYYY-MM-DDThh:mm
    return date.toISOString().slice(0, 16);
  }

  loadReport() {
    if (this.startDate && this.endDate) {
      this.reportService.getSalesReport(this.startDate, this.endDate)
        .subscribe({
          next: (data) => {
            this.salesData = data;
          },
          error: (error) => {
            console.error('Error loading sales report:', error);
          }
        });
    }
  }

  getTotalQuantity(): number {
    return this.salesData.reduce((sum, item) => sum + item.quantity, 0);
  }

  getTotalRevenue(): number {
    return this.salesData.reduce((sum, item) => sum + item.revenue, 0);
  }
} 