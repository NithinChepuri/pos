import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { OrderService } from '../services/order.service';
import { Order, OrderStatus, JavaDateTime } from '../models/order';
import { AddOrderModalComponent } from './add-order-modal/add-order-modal.component';

@Component({
  selector: 'app-orders',
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css'],
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    RouterModule,
    AddOrderModalComponent
  ],
  providers: [DatePipe]
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  loading = false;
  error = '';
  successMessage = '';
  startDate: Date | null = null;
  endDate: Date | null = null;
  currentPage: number = 0;
  pageSize: number = 10;
  
  // Keep OrderStatus for the table display
  OrderStatus = OrderStatus;
  
  showAddOrderModal = false;
  
  constructor(
    private orderService: OrderService,
    private datePipe: DatePipe
  ) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.error = '';
    
    this.orderService.getOrders(this.currentPage, this.pageSize).subscribe({
      next: (orders) => {
        console.log('Received orders:', orders); // Debug log
        this.orders = orders;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.error = 'Failed to load orders. Please try again.';
        this.loading = false;
      }
    });
  }

  filterByDate(): void {
    if (this.startDate && this.endDate) {
      this.loading = true;
      
      // Create start date at beginning of day (00:00:00)
      const start = new Date(this.startDate);
      start.setHours(0, 0, 0, 0);
      
      // Create end date at end of day (23:59:59)
      const end = new Date(this.endDate);
      end.setHours(23, 59, 59, 999);
      
      console.log('Filtering with dates:', { start, end }); // Debug log
      
      this.orderService.getOrdersByDateRange(start, end).subscribe({
        next: (data) => {
          console.log('Filter results:', data); // Debug log
          this.orders = data;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error filtering orders:', error);
          this.error = 'Failed to filter orders. Please try again.';
          this.loading = false;
        }
      });
    }
  }

  generateInvoice(orderId: number | undefined): void {
    if (!orderId) return;
    
    this.loading = true;
    this.error = '';
    this.successMessage = '';
    
    this.orderService.generateInvoice(orderId).subscribe({
      next: (response) => {
        console.log('Invoice generated successfully');
        this.successMessage = 'Invoice downloaded successfully';
        this.loading = false;
        // Refresh orders list to update status
        this.loadOrders();
      },
      error: (error) => {
        console.error('Error generating invoice:', error);
        this.error = 'Failed to generate invoice';
        this.successMessage = '';
        this.loading = false;
      }
    });
  }

  downloadInvoice(orderId: number | undefined): void {
    if (!orderId) return;

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    // Reuse the generateInvoice method for downloading
    this.orderService.generateInvoice(orderId).subscribe({
      next: (response: Blob) => {
        console.log('Invoice downloaded successfully');
        this.successMessage = 'Invoice downloaded successfully';
        this.loading = false;
        // Logic to handle the file download
        const blob = new Blob([response], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `invoice_${orderId}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error: any) => {
        console.error('Error downloading invoice:', error);
        this.error = 'Failed to download invoice';
        this.successMessage = '';
        this.loading = false;
      }
    });
  }

  // Remove selectedStatus property as it's no longer needed
  resetFilters(): void {
    this.startDate = null;
    this.endDate = null;
    this.loadOrders();
  }

  // Keep the status color method
  getStatusColor(status: OrderStatus | undefined): string {
    if (!status) return 'secondary';
    
    switch (status) {
      case OrderStatus.CREATED:
        return 'primary';
      case OrderStatus.INVOICED:
        return 'success';
      default:
        return 'secondary';
    }
  }

  formatDate(date: string | JavaDateTime | undefined): string {
    if (!date) return '-';
    
    if (typeof date === 'object') {
      // Handle Java ZonedDateTime format
      const javaDate = date as JavaDateTime;
      if ('year' in javaDate) {
        // Create date in local timezone
        const dateObj = new Date(
          javaDate.year,
          javaDate.monthValue - 1,
          javaDate.dayOfMonth,
          javaDate.hour,
          javaDate.minute,
          javaDate.second,
          javaDate.nano / 1000000 // Convert nanoseconds to milliseconds
        );

        // Adjust for timezone offset
        if (javaDate.offset?.totalSeconds) {
          const localOffset = dateObj.getTimezoneOffset() * 60; // Local offset in seconds
          const targetOffset = javaDate.offset.totalSeconds;
          const offsetDiff = targetOffset + localOffset;
          dateObj.setSeconds(dateObj.getSeconds() + offsetDiff);
        }

        return this.datePipe.transform(dateObj, 'medium') || '-';
      }
      return '-';
    }
    
    // If it's a string, try to parse it as a date first
    const parsedDate = new Date(date);
    if (!isNaN(parsedDate.getTime())) {
      return this.datePipe.transform(parsedDate, 'medium') || '-';
    }
    
    return '-';
  }

  // Add methods to handle pagination
  nextPage() {
    this.currentPage++;
    this.loadOrders();
  }

  previousPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadOrders();
    }
  }

  openAddOrderModal(): void {
    this.showAddOrderModal = true;
  }

  closeAddOrderModal(refreshData: boolean): void {
    this.showAddOrderModal = false;
    if (refreshData) {
      this.loadOrders();
    }
  }
} 