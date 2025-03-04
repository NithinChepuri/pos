import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { OrderService } from '../services/order.service';
import { Order, OrderStatus, JavaDateTime } from '../models/order';

@Component({
  selector: 'app-orders',
  templateUrl: './orders.component.html',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  providers: [DatePipe],
  styles: [`
    .form-label {
      font-weight: 500;
      margin-bottom: 0.5rem;
    }
    .card {
      box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
      border: none;
    }
    .card-title {
      color: #495057;
      font-size: 1.1rem;
    }
    .form-control, .form-select {
      border-radius: 0.375rem;
      border: 1px solid #dee2e6;
      padding: 0.5rem 0.75rem;
    }
    .form-control:focus, .form-select:focus {
      border-color: #86b7fe;
      box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
    }
    .btn-primary {
      padding: 0.5rem 1rem;
      font-weight: 500;
    }
    .btn-primary:disabled {
      cursor: not-allowed;
      opacity: 0.65;
    }
  `]
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  loading = false;
  error = '';
  startDate?: Date;
  endDate?: Date;
  selectedStatus?: OrderStatus;
  
  // Make OrderStatus enum available to template
  OrderStatus = OrderStatus;

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
    
    this.orderService.getOrders().subscribe({
      next: (data) => {
        console.log('Received orders:', data); // Debug log
        this.orders = data;
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
    this.orderService.generateInvoice(orderId).subscribe({
      next: (response) => {
        // Handle success
        this.loading = false;
        // Maybe show a success message or download the invoice
      },
      error: (error) => {
        console.error('Error generating invoice:', error);
        this.error = 'Failed to generate invoice';
        this.loading = false;
      }
    });
  }

  resetFilters(): void {
    this.startDate = undefined;
    this.endDate = undefined;
    this.selectedStatus = undefined;
    this.loadOrders(); // Reload all orders
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

  // Add a method to get status color
  getStatusColor(status: string | undefined): string {
    if (!status) return 'secondary';
    
    switch (status) {
      case 'CREATED':
        return 'primary';  // Bootstrap blue
      case 'INVOICED':
        return 'success';  // Bootstrap green
      default:
        return 'secondary';
    }
  }
} 