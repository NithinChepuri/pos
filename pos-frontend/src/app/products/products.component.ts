import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { ProductService } from '../services/product.service';
import { ClientService } from '../services/client.service';
import { Product } from '../models/product';
import { Client } from '../models/client';
import { Subscription, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { SearchType } from '../services/product.service';
import { AuthService } from '../services/auth.service';
import { UploadProductModalComponent } from './upload-product-modal/upload-product-modal.component';
import { ToastService } from '../services/toast.service';

@Component({
  selector: 'app-products',
  templateUrl: './products.component.html',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, UploadProductModalComponent]
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
  searchSize: number = 5;
  isSearching: boolean = false; // New property to track if a search is active
  showUploadModal = false;

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
      this.productService.updateProduct(this.editingProduct.id, this.editingProduct)
        .subscribe({
          next: (updatedProduct) => {
            if (updatedProduct && updatedProduct.id) {
              const index = this.products.findIndex(p => p.id === updatedProduct.id);
              if (index !== -1) {
                this.products[index] = updatedProduct;
              } else {
                console.warn('Updated product not found in the list:', updatedProduct);
              }
            } else {
              console.error('Invalid updated product received:', updatedProduct);
            }
            this.editingProduct = null;
            this.toastService.showSuccess('Product updated successfully');
          },
          error: (error) => {
            console.error('Error updating product:', error);
            if (error.error && error.error.name) {
              this.toastService.showError(error.error.name);
            } else {
              this.toastService.showError('An error occurred while updating the product.');
            }
          }
        });
    } else {
      console.error('Editing product is null or does not have a valid id');
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
          error: (error) => {
            console.error('Error deleting product:', error);
            this.toastService.showError('An error occurred while deleting the product.');
          }
        });
    }
  }

  onSearch(term: string): void {
    this.searchTerm = term;
    this.loading = true;
    this.error = '';
    this.isSearching = !!term.trim();

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
    this.onSearch(this.searchTerm);
  }

  previousSearchPage() {
    if (this.searchPage > 0) {
      this.searchPage--;
      this.onSearch(this.searchTerm);
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
} 