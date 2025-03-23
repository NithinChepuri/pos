import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportService } from '../../services/report.service';
import { SalesReportItem } from '../../models/sales-report';
import { Client } from '../../models/client';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-sales-report',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sales-report.component.html',
  styleUrls: ['./sales-report.component.css']
})
export class SalesReportComponent implements OnInit {
  startDate: string = '';
  endDate: string = '';
  selectedClientId: number | null = null;
  clients: Client[] = [];
  
  reportData: SalesReportItem[] = [];
  loading: boolean = false;
  error: string = '';
  
  constructor(
    private reportService: ReportService,
    private toastService: ToastService
  ) {}
  
  ngOnInit(): void {
    // Set default date range to last 30 days
    const today = new Date();
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(today.getDate() - 30);
    
    this.endDate = this.formatDateForInput(today);
    this.startDate = this.formatDateForInput(thirtyDaysAgo);
    
    // Load clients for the dropdown
    this.loadClients();
  }
  
  loadClients(): void {
    this.reportService.getAllClients().subscribe({
      next: (clients) => {
        this.clients = clients;
      },
      error: (error) => {
        console.error('Error loading clients:', error);
        this.toastService.showError('Failed to load clients');
      }
    });
  }
  
  generateReport(): void {
    if (!this.startDate || !this.endDate) {
      this.toastService.showError('Please select both start and end dates');
      return;
    }
    
    this.loading = true;
    this.error = '';
    
    this.reportService.getSalesReport(this.startDate, this.endDate, this.selectedClientId || undefined).subscribe({
      next: (data) => {
        this.reportData = data;
        this.loading = false;
        
        if (data.length === 0) {
          this.toastService.showInfo('No sales data found for the selected criteria');
        }
      },
      error: (error) => {
        console.error('Error generating report:', error);
        this.loading = false;
        
        // Extract error message
        let errorMessage = 'Failed to generate report';
        if (error.error && error.error.error) {
          errorMessage = error.error.error;
        } else if (typeof error.error === 'string') {
          errorMessage = error.error;
        } else if (error.message) {
          errorMessage = error.message;
        }
        
        this.toastService.showError(errorMessage);
      }
    });
  }
  
  resetFilters(): void {
    // Reset to last 30 days
    const today = new Date();
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(today.getDate() - 30);
    
    this.endDate = this.formatDateForInput(today);
    this.startDate = this.formatDateForInput(thirtyDaysAgo);
    this.selectedClientId = null;
    
    // Clear report data
    this.reportData = [];
  }
  
  calculateTotalRevenue(): number {
    return this.reportData.reduce((sum, item) => sum + item.revenue, 0);
  }
  
  calculateTotalQuantity(): number {
    return this.reportData.reduce((sum, item) => sum + item.quantity, 0);
  }
  
  private formatDateForInput(date: Date): string {
    // Format date as YYYY-MM-DD for input[type="date"]
    return date.toISOString().split('T')[0];
  }
} 