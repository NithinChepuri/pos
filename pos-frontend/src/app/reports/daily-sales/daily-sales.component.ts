import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DailySalesService } from '../../services/daily-sales.service';
import { DailySalesData } from '../../models/daily-sales';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-daily-sales',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './daily-sales.component.html',
  styleUrls: ['./daily-sales.component.css']
})
export class DailySalesComponent implements OnInit {
  startDate: string = '';
  endDate: string = '';
  
  salesData: DailySalesData[] = [];
  latestData: DailySalesData | null = null;
  loading: boolean = false;
  error: string = '';
  
  // Chart data
  chartLabels: string[] = [];
  chartRevenue: number[] = [];
  chartOrders: number[] = [];
  chartItems: number[] = [];
  
  constructor(
    private dailySalesService: DailySalesService,
    private toastService: ToastService
  ) {}
  
  ngOnInit(): void {
    // Set default date range to last 30 days
    const today = new Date();
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(today.getDate() - 30);
    
    this.endDate = this.formatDateForInput(today);
    this.startDate = this.formatDateForInput(thirtyDaysAgo);
    
    // Load latest data
    this.loadLatestData();
    
    // Load data for default date range
    this.loadSalesData();
  }
  
  loadLatestData(): void {
    this.dailySalesService.getLatestDailySales().subscribe({
      next: (data: DailySalesData) => {
        console.log('Latest data received:', data);
        
        // Format the date for display
        if (data && data.date) {
          data.formattedDate = this.formatDateForDisplay(data.date);
        }
        
        this.latestData = data;
      },
      error: (error: any) => {
        console.error('Error loading latest sales data:', error);
        this.toastService.showError('Failed to load latest sales data');
      }
    });
  }
  
  loadSalesData(): void {
    if (!this.startDate || !this.endDate) {
      this.toastService.showError('Please select both start and end dates');
      return;
    }
    
    this.loading = true;
    this.error = '';
    
    this.dailySalesService.getDailySalesByDateRange(this.startDate, this.endDate).subscribe({
      next: (data: DailySalesData[]) => {
        console.log('Sales data received:', data);
        
        // Format dates for display
        this.salesData = data.map(item => {
          if (item.date) {
            item.formattedDate = this.formatDateForDisplay(item.date);
          }
          return item;
        });
        
        this.loading = false;
        
        if (data.length === 0) {
          this.toastService.showInfo('No sales data found for the selected date range');
        } else {
          this.prepareChartData();
        }
      },
      error: (error: any) => {
        console.error('Error loading sales data:', error);
        this.loading = false;
        
        // Extract error message
        let errorMessage = 'Failed to load sales data';
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
  
  prepareChartData(): void {
    // Reset chart data
    this.chartLabels = [];
    this.chartRevenue = [];
    this.chartOrders = [];
    this.chartItems = [];
    
    // Prepare chart data from sales data
    this.salesData.forEach(data => {
      // Use the formatted date
      this.chartLabels.push(data.formattedDate || 'Unknown Date');
      this.chartRevenue.push(data.totalRevenue);
      this.chartOrders.push(data.totalOrders);
      this.chartItems.push(data.totalItems);
    });
  }
  
  resetFilters(): void {
    // Reset to last 30 days
    const today = new Date();
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(today.getDate() - 30);
    
    this.endDate = this.formatDateForInput(today);
    this.startDate = this.formatDateForInput(thirtyDaysAgo);
    
    // Load data for default date range
    this.loadSalesData();
  }
  
  calculateTotalRevenue(): number {
    return this.salesData.reduce((sum, item) => sum + item.totalRevenue, 0);
  }
  
  calculateTotalOrders(): number {
    return this.salesData.reduce((sum, item) => sum + item.totalOrders, 0);
  }
  
  calculateTotalItems(): number {
    return this.salesData.reduce((sum, item) => sum + item.totalItems, 0);
  }
  
  calculateTotalInvoicedOrders(): number {
    return this.salesData.reduce((sum, item) => sum + item.invoicedOrderCount, 0);
  }
  
  calculateTotalInvoicedItems(): number {
    return this.salesData.reduce((sum, item) => sum + item.invoicedItemCount, 0);
  }
  
  private formatDateForInput(date: Date): string {
    // Format date as YYYY-MM-DD for input[type="date"]
    return date.toISOString().split('T')[0];
  }
  
  private formatDateForDisplay(dateInput: any): string {
    console.log('Formatting date:', dateInput);
    
    try {
      // If it's already a string in the format we want, return it
      if (typeof dateInput === 'string' && dateInput.match(/^\d{2}\/\d{2}\/\d{4}$/)) {
        return dateInput;
      }
      
      let date: Date;
      
      // Handle different date formats
      if (dateInput instanceof Date) {
        date = dateInput;
      } else if (typeof dateInput === 'string') {
        // Try to parse the string date
        date = new Date(dateInput);
      } else if (typeof dateInput === 'object' && dateInput !== null) {
        // Handle Java ZonedDateTime format (from the console log)
        if (dateInput.monthValue !== undefined && dateInput.dayOfMonth !== undefined && dateInput.year !== undefined) {
          return `${dateInput.dayOfMonth.toString().padStart(2, '0')}/${dateInput.monthValue.toString().padStart(2, '0')}/${dateInput.year}`;
        } else if (dateInput.year !== undefined && dateInput.month !== undefined && dateInput.day !== undefined) {
          // Handle alternative Java date format
          date = new Date(dateInput.year, dateInput.month - 1, dateInput.day);
        } else if (dateInput.date) {
          // Handle nested date object
          return this.formatDateForDisplay(dateInput.date);
        } else {
          // Try to convert using JSON
          date = new Date(dateInput);
        }
      } else {
        throw new Error('Unsupported date format');
      }
      
      // Check if date is valid
      if (isNaN(date.getTime())) {
        throw new Error('Invalid date');
      }
      
      // Format as DD/MM/YYYY
      return `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1).toString().padStart(2, '0')}/${date.getFullYear()}`;
    } catch (error) {
      console.error('Error formatting date:', error, dateInput);
      
      // If we have a string, try to extract date parts directly
      if (typeof dateInput === 'string') {
        const parts = dateInput.split('T')[0].split('-');
        if (parts.length === 3) {
          return `${parts[2]}/${parts[1]}/${parts[0]}`;
        }
      }
      
      // If it's an object with date parts (additional check for Java ZonedDateTime)
      if (typeof dateInput === 'object' && dateInput !== null) {
        // Check for Java ZonedDateTime format
        if (dateInput.monthValue !== undefined && dateInput.dayOfMonth !== undefined && dateInput.year !== undefined) {
          return `${dateInput.dayOfMonth.toString().padStart(2, '0')}/${dateInput.monthValue.toString().padStart(2, '0')}/${dateInput.year}`;
        }
      }
      
      // Fallback
      return 'Invalid Date';
    }
  }
} 