import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportService } from '../../services/report.service';
import { SalesReportItem } from '../../models/sales-report';
import { InrCurrencyPipe } from '../../pipes/inr-currency.pipe';
@Component({
  selector: 'app-sales-report',
  standalone: true,
  imports: [CommonModule, FormsModule, InrCurrencyPipe],
  templateUrl: './sales-report.component.html',
  styleUrls: ['./sales-report.component.css']
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