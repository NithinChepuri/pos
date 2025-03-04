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
  template: `
    <div class="container mt-4">
      <div class="card">
        <div class="card-header d-flex justify-content-between align-items-center">
          <h3 class="mb-0">Create New Order</h3>
          <button class="btn btn-secondary" routerLink="/orders">Back to Orders</button>
        </div>
        
        <div class="card-body">
          <!-- Error Alert -->
          <div *ngIf="error" class="alert alert-danger" role="alert">
            {{ error }}
          </div>

          <!-- Barcode Scanner Input with Suggestions -->
          <div class="mb-3">
            <label for="barcode" class="form-label">Scan or Enter Barcode</label>
            <div class="position-relative">
              <input type="text" 
                     class="form-control" 
                     id="barcode" 
                     [(ngModel)]="currentBarcode"
                     (ngModelChange)="onBarcodeChange($event)"
                     (keyup.enter)="onBarcodeEnter()"
                     [disabled]="loading"
                     autocomplete="off">
              <!-- Barcode Suggestions Dropdown -->
              <div class="dropdown-menu shadow" 
                   [class.show]="showSuggestions && filteredProducts.length > 0" 
                   style="width: 100%">
                <button class="dropdown-item" 
                        *ngFor="let product of filteredProducts"
                        (click)="selectProduct(product)">
                  {{product.barcode}} - {{product.name}}
                </button>
              </div>
            </div>
          </div>

          <!-- Order Items Table -->
          <div class="table-responsive mb-3">
            <table class="table table-striped">
              <thead>
                <tr>
                  <th>Barcode</th>
                  <th>Product Name</th>
                  <th>Quantity</th>
                  <th>Selling Price</th>
                  <th>Total</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let item of orderItems; let i = index">
                  <td>{{item.barcode}}</td>
                  <td>{{item.productName}}</td>
                  <td>
                    <input type="number" 
                           class="form-control form-control-sm w-75"
                           [(ngModel)]="item.quantity"
                           (ngModelChange)="updateTotal(item)"
                           min="1">
                  </td>
                  <td>
                    <input type="number" 
                           class="form-control form-control-sm w-75"
                           [(ngModel)]="item.sellingPrice"
                           (ngModelChange)="updateTotal(item)"
                           min="0">
                  </td>
                  <td>{{calculateItemTotal(item) | currency:'INR'}}</td>
                  <td>
                    <button class="btn btn-danger btn-sm"
                            (click)="removeItem(i)">
                      <i class="bi bi-trash"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
              <tfoot *ngIf="orderItems.length > 0">
                <tr>
                  <td colspan="4" class="text-end"><strong>Total:</strong></td>
                  <td colspan="2"><strong>{{calculateOrderTotal() | currency:'INR'}}</strong></td>
                </tr>
              </tfoot>
            </table>
          </div>

          <!-- Submit Button -->
          <div class="d-grid gap-2 d-md-flex justify-content-md-end">
            <button class="btn btn-primary"
                    [disabled]="loading || orderItems.length === 0"
                    (click)="submitOrder()">
              <span *ngIf="loading" class="spinner-border spinner-border-sm me-1"></span>
              {{ loading ? 'Creating Order...' : 'Create Order' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dropdown-menu {
      max-height: 200px;
      overflow-y: auto;
    }
    .dropdown-item {
      cursor: pointer;
    }
    .dropdown-item:hover {
      background-color: #f8f9fa;
    }
  `]
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
      this.error = 'Please add at least one item to the order';
      return;
    }

    this.loading = true;
    this.error = '';

    this.orderService.createOrder({ items: this.orderItems }).subscribe({
      next: () => {
        this.router.navigate(['/orders']);
      },
      error: (error) => {
        console.error('Error creating order:', error);
        this.error = error.error?.message || 'Failed to create order';
        this.loading = false;
      }
    });
  }
} 