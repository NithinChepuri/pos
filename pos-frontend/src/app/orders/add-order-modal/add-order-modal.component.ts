import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../services/order.service';
import { ProductService } from '../../services/product.service';
import { ToastService } from '../../services/toast.service';
import { OrderItem } from '../../models/order';
import { Product } from '../../models/product';

@Component({
  selector: 'app-add-order-modal',
  templateUrl: './add-order-modal.component.html',
  styleUrls: ['./add-order-modal.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class AddOrderModalComponent implements OnInit {
  @Output() closeModal = new EventEmitter<boolean>();
  
  currentBarcode = '';
  orderItems: (OrderItem & { productName?: string, error?: string })[] = [];
  loading = false;
  error = '';
  success = '';
  filteredProducts: Product[] = [];
  showSuggestions = false;

  constructor(
    private orderService: OrderService,
    private productService: ProductService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {}

  searchBarcode(): void {
    if (!this.currentBarcode.trim()) {
      this.filteredProducts = [];
      this.showSuggestions = false;
      return;
    }

    this.productService.searchProducts(this.currentBarcode, 'barcode').subscribe({
      next: (products) => {
        this.filteredProducts = products;
        this.showSuggestions = true;

        // If there's only one product, select it automatically
        if (products.length === 1) {
          this.selectProduct(products[0]);
        }
      },
      error: (error) => {
        console.error('Error searching products:', error);
        this.filteredProducts = [];
        this.showSuggestions = false;
        this.error = 'Failed to find product';
        setTimeout(() => {
          this.error = '';
        }, 3000);
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
    this.searchBarcode();
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

  updateSellingPrice(item: OrderItem & { productName?: string, error?: string }): void {
    // Clear any previous error
    item.error = undefined;
    
    // Get the product to check MRP
    this.productService.searchProducts(item.barcode, 'barcode').subscribe({
      next: (products) => {
        if (products.length > 0) {
          const product = products[0];
          
          // Validate selling price against MRP
          if (item.sellingPrice > product.mrp) {
            item.error = `Selling price cannot exceed MRP (${product.mrp})`;
          }
        }
        
        // Update total regardless
        this.updateTotal(item);
      },
      error: (error) => {
        console.error('Error fetching product details:', error);
      }
    });
  }

  submitOrder(): void {
    if (this.orderItems.length === 0) {
      this.error = 'Please add at least one item to the order';
      return;
    }

    // Check for any validation errors
    const itemWithError = this.orderItems.find(item => item.error);
    if (itemWithError) {
      this.toastService.showError(`Please fix the error: ${itemWithError.error}`);
      return;
    }

    // Log the request payload - remove clientId
    const orderData = {
      // Remove clientId field
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
        this.success = 'Order created successfully!';
        this.loading = false;
        
        // Close modal after successful creation with a delay
        setTimeout(() => {
          this.close(true);
        }, 2000);
      },
      error: (error) => {
        console.error('Full error object:', error);
        this.loading = false;
        
        // Extract error message from response
        let errorMessage = 'Failed to create order';
        if (error.error && error.error.error) {
          errorMessage = error.error.error;
          
          // Check if it's a selling price validation error
          if (errorMessage.includes('Selling price') && errorMessage.includes('cannot be greater than MRP')) {
            // Try to extract the barcode from the error message
            const match = errorMessage.match(/barcode\s+(\w+)/i);
            const barcode = match ? match[1] : null;
            
            if (barcode) {
              // Find the item with this barcode and mark it with an error
              const item = this.orderItems.find(i => i.barcode === barcode);
              if (item) {
                item.error = errorMessage;
                this.toastService.showError('Please fix the highlighted item');
                return;
              }
            }
          }
          
          this.toastService.showError(errorMessage);
        } else if (typeof error.error === 'string') {
          this.toastService.showError(error.error);
        } else if (error.message) {
          this.toastService.showError(error.message);
        } else {
          this.toastService.showError(errorMessage);
        }
      }
    });
  }

  close(refreshData: boolean = false): void {
    this.closeModal.emit(refreshData);
  }

  clearBarcode(): void {
    this.currentBarcode = '';
    this.filteredProducts = [];
    this.showSuggestions = false;
  }
} 