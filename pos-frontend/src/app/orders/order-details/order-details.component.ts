import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { OrderService } from '../../services/order.service';
import { Order, OrderItem, OrderStatus } from '../../models/order';
import { JavaDateTime } from '../../models/order';

@Component({
  selector: 'app-order-details',
  standalone: true,
  imports: [CommonModule, RouterModule],
  providers: [DatePipe],
  templateUrl: './order-details.component.html'
})
export class OrderDetailsComponent implements OnInit {
  order: Order | undefined;
  loading = false;
  error = '';
  
  // Make OrderStatus available to template
  OrderStatus = OrderStatus;

  constructor(
    private orderService: OrderService,
    private route: ActivatedRoute,
    private datePipe: DatePipe
  ) {}

  ngOnInit(): void {
    this.loadOrder();
  }

  loadOrder(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loading = true;
      this.orderService.getOrder(id).subscribe({
        next: (data) => {
          console.log('Received order:', data);
          this.order = data;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading order:', error);
          this.error = 'Failed to load order details';
          this.loading = false;
        }
      });
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

  // Calculate total for an order item
  calculateItemTotal(item: OrderItem): number {
    return item.quantity * item.sellingPrice;
  }

  // Calculate total for the entire order
  calculateOrderTotal(): number {
    if (!this.order) return 0;
    return this.order.items.reduce((total, item) => 
      total + this.calculateItemTotal(item), 0);
  }

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
} 