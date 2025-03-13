import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { OrderService } from '../../services/order.service';
import { ProductService } from '../../services/product.service';
import { OrderItem } from '../../models/order';
import { Product } from '../../models/product';
import { debounceTime, distinctUntilChanged, switchMap, of } from 'rxjs';

@Component({
  selector: 'app-add-order',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './add-order.component.html',
  styleUrls: ['./add-order.component.css']
})
export class AddOrderComponent implements OnInit {
  currentBarcode = '';
  orderItems: (OrderItem & { productName?: string })[] = [];
  loading = false;
  error = '';
  filteredProducts: Product[] = [];
  showSuggestions = false;

  constructor(
    private orderService: OrderService,
    private productService: ProductService,
    private router: Router
  ) {}

  ngOnInit(): void {}

  onBarcodeChange(value: string): void {
    if (!value.trim()) {
      this.filteredProducts = [];
      this.showSuggestions = false;
      return;
    }

    this.productService.searchProducts(value, 'barcode').subscribe({
      next: (products) => {
        this.filteredProducts = products;
        this.showSuggestions = true;
      },
      error: (error) => {
        console.error('Error searching products:', error);
        this.filteredProducts = [];
        this.showSuggestions = false;
      }
    });
  }

  selectProduct(product: Product): void {
    this.currentBarcode = product.barcode;
    this.showSuggestions = false;
    
    // Check if item already exists
    const existingItem = this.orderItems.find(item => item.barcode === product.barcode);
    if (existingItem) {
      existingItem.quantity += 1;
      this.updateTotal(existingItem);
    } else {
      // Add new item with product details
      const newItem = {
        barcode: product.barcode,
        productName: product.name,
        quantity: 1,
        sellingPrice: product.mrp, // Set selling price to MRP by default
        total: product.mrp // Initialize total
      };
      this.orderItems.push(newItem);
    }
    
    this.currentBarcode = ''; // Clear the input
  }

  onBarcodeEnter(): void {
    if (!this.currentBarcode.trim()) return;

    // If there's only one suggestion, select it
    if (this.filteredProducts.length === 1) {
      this.selectProduct(this.filteredProducts[0]);
      return;
    }

    // Otherwise, search for exact match
    this.productService.searchProducts(this.currentBarcode, 'barcode').subscribe({
      next: (products) => {
        const exactMatch = products.find(p => p.barcode === this.currentBarcode);
        if (exactMatch) {
          this.selectProduct(exactMatch);
        } else {
          this.error = 'Product not found';
        }
      },
      error: (error) => {
        console.error('Error searching product:', error);
        this.error = 'Failed to find product';
      }
    });
  }

  updateTotal(item: OrderItem): void {
    item.total = item.quantity * item.sellingPrice;
  }

  calculateItemTotal(item: OrderItem): number {
    return item.quantity * item.sellingPrice;
  }

  calculateOrderTotal(): number {
    return this.orderItems.reduce((sum, item) => sum + this.calculateItemTotal(item), 0);
  }

  removeItem(index: number): void {
    this.orderItems.splice(index, 1);
  }

  submitOrder(): void {
    if (this.orderItems.length === 0) {
      console.error('No items in order');
      return;
    }

    // Log the request payload
    const orderData = {
      clientId: 1,
      items: this.orderItems.map(item => ({
        barcode: item.barcode,
        quantity: item.quantity,
        sellingPrice: item.sellingPrice
      }))
    };
    console.log('Sending order data:', orderData);

    this.loading = true;
    this.error = '';

    this.orderService.createOrder(orderData).subscribe({
      next: (response) => {
        console.log('Order created successfully:', response);
        this.router.navigate(['/orders']);
      },
      error: (error) => {
        console.error('Full error object:', error);
        console.error('Error response:', error.error);
        if (typeof error.error === 'string') {
          this.error = 'Failed to create order: ' + error.error; // Display the error message from the backend
        } else {
          this.error = 'Failed to create order: ' + (error.error?.message || error.message);
        }
        this.loading = false;
      }
    });
  }
} 