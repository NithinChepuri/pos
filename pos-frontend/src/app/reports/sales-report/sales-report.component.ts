import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportService } from '../../services/report.service';
import { SalesReportItem } from '../../models/sales-report';
import { InrCurrencyPipe } from '../../pipes/inr-currency.pipe';
import { ToastService } from '../../services/toast.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-sales-report',
  standalone: true,
  imports: [CommonModule, FormsModule, InrCurrencyPipe],
  templateUrl: './sales-report.component.html',
  styleUrls: ['./sales-report.component.css']
})
export class SalesReportComponent implements OnInit {
  startDate = '';
  endDate = '';
  salesData: SalesReportItem[] = [];
  isLoading = false;

  constructor(
    private reportService: ReportService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    // Set default dates to current month
    this.setDefaultDateRange();
  }

  setDefaultDateRange(): void {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    
    this.startDate = this.formatDateTimeForInput(firstDay);
    this.endDate = this.formatDateTimeForInput(today);
  }

  formatDateTimeForInput(date: Date): string {
    // Format: YYYY-MM-DDThh:mm
    return date.toISOString().slice(0, 16);
  }

  loadReport(): void {
    if (!this.validateDates()) {
      return;
    }

    this.isLoading = true;
    this.salesData = []; // Clear previous data

    this.reportService.getSalesReport(this.startDate, this.endDate)
      .subscribe({
        next: (data) => {
          this.salesData = data;
          this.isLoading = false;
          if (data.length === 0) {
            this.toastService.showInfo('No sales data found for the selected date range.');
          } else {
            this.toastService.showSuccess(`Found ${data.length} sales records.`);
          }
        },
        error: (error: HttpErrorResponse) => {
          console.error('Error loading sales report:', error);
          this.isLoading = false;
          
          // Extract the specific error message from the response
          let errorMessage = 'Failed to load sales report. Please try again.';
          
          if (error.error) {
            if (typeof error.error === 'string') {
              errorMessage = error.error;
            } else if (error.error.error) {
              errorMessage = error.error.error;
            } else if (typeof error.error === 'object') {
              // Try to extract any property that might contain the error message
              const firstErrorKey = Object.keys(error.error)[0];
              if (firstErrorKey) {
                errorMessage = error.error[firstErrorKey];
              }
            }
          }
          
          this.toastService.showError(errorMessage);
        }
      });
  }

  validateDates(): boolean {
    if (!this.startDate || !this.endDate) {
      this.toastService.showWarning('Please select both start and end dates');
      return false;
    }

    const start = new Date(this.startDate);
    const end = new Date(this.endDate);

    if (start > end) {
      this.toastService.showWarning('Start date cannot be after end date');
      return false;
    }

    // Add client-side validation for future dates
    const now = new Date();
    if (start > now || end > now) {
      this.toastService.showWarning('Dates cannot be in the future');
      return false;
    }

    return true;
  }

  getTotalQuantity(): number {
    return this.salesData.reduce((sum, item) => sum + item.quantity, 0);
  }

  getTotalRevenue(): number {
    return this.salesData.reduce((sum, item) => sum + item.revenue, 0);
  }
} 