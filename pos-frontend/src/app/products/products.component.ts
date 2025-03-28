import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { ProductService, SearchType, ApiError } from '../services/product.service';
import { ClientService } from '../services/client.service';
import { Product } from '../models/product';
import { Client } from '../models/client';
import { Subscription, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { UploadProductModalComponent } from './upload-product-modal/upload-product-modal.component';
import { ToastService } from '../services/toast.service';
import { AddProductModalComponent } from './add-product-modal/add-product-modal.component';
import { InrCurrencyPipe } from '../pipes/inr-currency.pipe';

@Component({
  selector: 'app-products',
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.css'],
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    RouterModule, 
    UploadProductModalComponent, 
    AddProductModalComponent,
    InrCurrencyPipe
  ]
})
export class ProductsComponent implements OnInit, OnDestroy {
  products: Product[] = [];
  clients: Client[] = [];
  loading = false;
  editingProduct: Product | null = null;
  private routerSubscription: Subscription;
  error = '';
  searchTerm = '';
  private searchSubject = new Subject<string>();
  searchType: SearchType = 'all';
  minMrp?: number;
  maxMrp?: number;
  isSupervisor: boolean;
  currentPage: number = 0;
  pageSize: number = 10;
  totalProducts: number = 0; // This will be updated based on the total number of products
  searchPage: number = 0;
  searchSize: number = 10;
  isSearching: boolean = false; // New property to track if a search is active
  showUploadModal = false;
  showAddModal = false;
  validationErrors: { [key: string]: string } = {};

  constructor(
    private productService: ProductService,
    private clientService: ClientService,
    private router: Router,
    private authService: AuthService,
    private toastService: ToastService
  ) {
    this.isSupervisor = this.authService.isSupervisor();
    // Initialize router subscription
    this.routerSubscription = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      if (this.router.url === '/products') {
        this.loadProducts();
        this.loadClients();
      }
    });

    // Set up search with debounce
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => {
        this.loading = true;
        console.log('Searching for:', term, 'in:', this.searchType);
        if (!term.trim()) {
          return this.productService.getProducts();
        }
        return this.productService.searchProducts(term, this.searchType, {
          min: this.minMrp,
          max: this.maxMrp
        });
      })
    ).subscribe({
      next: (products) => {
        console.log('Search results:', products); // Debug log
        this.products = products;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error searching products:', error);
        this.toastService.showError('Failed to search products. Please try again.');
        this.loading = false;
      }
    });
  }

  ngOnInit() {
    this.loadProducts();
    this.loadClients();
  }

  ngOnDestroy() {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
  }

  loadProducts() {
    this.loading = true;
    this.productService.getProducts(this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        this.products = data;
        this.loading = false;
        // Update totalProducts based on the response headers or a separate API call
        // this.totalProducts = response.total; // Example if total is returned in response
      },
      error: (error) => {
        console.error('Error loading products:', error);
        this.toastService.showError('Failed to load products. Please try again.');
        this.loading = false;
      }
    });
  }

  loadClients() {
    this.clientService.getClients().subscribe({
      next: (data) => {
        this.clients = data;
      },
      error: (error) => {
        console.error('Error fetching clients:', error);
        this.toastService.showError('Failed to load clients. Please try again.');
      }
    });
  }

  getClientName(clientId: number): string {
    const client = this.clients.find(c => c.id === clientId);
    return client ? client.name : 'Unknown Client';
  }

  startEdit(product: Product): void {
    this.editingProduct = { ...product };
  }

  cancelEdit(): void {
    this.editingProduct = null;
  }

  saveEdit(): void {
    if (this.editingProduct && this.editingProduct.id) {
      this.validationErrors = {}; // Clear previous errors
      
      this.productService.updateProduct(this.editingProduct.id, this.editingProduct)
        .subscribe({
          next: (updatedProduct) => {
            const index = this.products.findIndex(p => p.id === updatedProduct.id);
            if (index !== -1) {
              this.products[index] = updatedProduct;
            }
            this.editingProduct = null;
            this.toastService.showSuccess('Product updated successfully');
          },
          error: (error: ApiError) => {
            console.error('Error updating product:', error);
            
            if (error.validationErrors) {
              this.validationErrors = error.validationErrors;
              // Show first validation error in toast
              const firstError = Object.values(error.validationErrors)[0];
              this.toastService.showError(firstError);
            } else if (error.message) {
              this.toastService.showError(error.message);
            } else {
              this.toastService.showError('An error occurred while updating the product.');
            }
          }
        });
    }
  }

  deleteProduct(id: number): void {
    if (confirm('Are you sure you want to delete this product?')) {
      this.productService.deleteProduct(id)
        .subscribe({
          next: () => {
            this.products = this.products.filter(p => p.id !== id);
            this.toastService.showSuccess('Product deleted successfully');
          },
          error: (error: ApiError) => {
            console.error('Error deleting product:', error);
            
            // Display specific error message if available
            if (error.message && error.message.includes('inventory')) {
              this.toastService.showError('Cannot delete product that has inventory. Please remove inventory first.');
            } else if (error.message) {
              this.toastService.showError(error.message);
            } else {
              this.toastService.showError('An error occurred while deleting the product.');
            }
          }
        });
    }
  }

  performSearch(): void {
    const term = this.searchTerm.trim();
    this.loading = true;
    this.error = '';
    this.isSearching = !!term;
    
    // Reset search page to 0 when performing a new search
    this.searchPage = 0;
    // this.currentPage=0;

    if (!this.isSearching) {
      this.loadProducts();
      return;
    }

    this.productService.searchProducts(term, this.searchType, { min: this.minMrp, max: this.maxMrp }, this.searchPage, this.searchSize)
      .subscribe({
        next: (products) => {
          this.products = products;
          this.loading = false;
        },
        error: (error) => {
          console.error('Search error:', error);
          this.toastService.showError('Failed to search products. Please try again.');
          this.loading = false;
        }
      });
  }

  onMrpRangeChange(): void {
    if (this.searchTerm) {
      this.searchSubject.next(this.searchTerm);
    }
  }

  // Add methods to handle pagination
  nextPage() {
    this.currentPage++;
    this.loadProducts();
  }

  previousPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadProducts();
    }
  }

  // Add methods to handle pagination for search
  nextSearchPage() {
    this.searchPage++;
    this.performSearch();
  }

  previousSearchPage() {
    if (this.searchPage > 0) {
      this.searchPage--;
      this.performSearch();
    }
  }

  openUploadModal(): void {
    this.showUploadModal = true;
  }

  closeUploadModal(refreshData: boolean): void {
    this.showUploadModal = false;
    if (refreshData) {
      this.loadProducts();
      this.toastService.showSuccess('Products uploaded successfully');
    }
  }

  openAddModal(): void {
    this.showAddModal = true;
  }

  closeAddModal(refreshData: boolean): void {
    this.showAddModal = false;
    if (refreshData) {
      this.loadProducts();
      this.toastService.showSuccess('Product added successfully');
    }
  }
  clearSearch(): void {
    this.searchTerm = '';
    this.searchType = 'all';
    this.loadProducts();
  }
} 