import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { InventoryService, InventorySearchType } from '../services/inventory.service';
import { Inventory } from '../models/inventory';
import { Subscription, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { UploadInventoryModalComponent } from './upload-inventory-modal/upload-inventory-modal.component';
import { ToastService } from '../services/toast.service';

@Component({
  selector: 'app-inventory',
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, UploadInventoryModalComponent]
})
export class InventoryComponent implements OnInit, OnDestroy {
  inventory: Inventory[] = [];
  loading = false;
  editingInventory: Inventory | null = null;
  private routerSubscription: Subscription;
  error = '';
  searchTerm = '';
  private searchSubject = new Subject<string>();
  searchType: InventorySearchType = 'all';
  currentPage: number = 0;
  pageSize: number = 10;
  searchPage: number = 0;
  isSearching: boolean = false;
  hasMoreRecords: boolean = true;
  showUploadModal = false;
  isSupervisor: boolean;

  constructor(
    private inventoryService: InventoryService,
    private router: Router,
    private authService: AuthService,
    private toastService: ToastService
  ) {
    this.isSupervisor = this.authService.isSupervisor();
    
    // Initialize router subscription
    this.routerSubscription = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      if (this.router.url === '/inventory') {
        this.loadInventory();
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
          return this.inventoryService.getInventory();
        }
        return this.inventoryService.searchInventory(term, this.searchType);
      })
    ).subscribe({
      next: (inventory) => {
        console.log('Search results:', inventory);
        this.inventory = inventory;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error searching inventory:', error);
        this.toastService.showError('Failed to search inventory. Please try again.');
        this.loading = false;
      }
    });
  }

  ngOnInit() {
    this.loadInventory();
  }

  ngOnDestroy() {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
  }

  loadInventory() {
    this.loading = true;
    this.inventoryService.getInventory(this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        this.inventory = data;
        this.loading = false;
        this.hasMoreRecords = data.length === this.pageSize;
      },
      error: (error) => {
        console.error('Error fetching inventory:', error);
        this.toastService.showError('Failed to load inventory. Please try again.');
        this.loading = false;
      }
    });
  }

  startEdit(item: Inventory): void {
    this.editingInventory = { ...item };
  }

  cancelEdit(): void {
    this.editingInventory = null;
  }

  saveEdit(): void {
    if (this.editingInventory && this.editingInventory.id) {
      this.inventoryService.updateInventory(this.editingInventory.id, this.editingInventory)
        .subscribe({
          next: (updatedInventory) => {
            this.editingInventory = null;
            // Reload the current page data
            if (this.isSearching) {
              this.searchInventory();
            } else {
              this.loadInventory();
            }
            this.toastService.showSuccess('Inventory updated successfully');
          },
          error: (error) => {
            console.error('Error updating inventory:', error);
            if (error.error && typeof error.error === 'object') {
              this.toastService.showError(Object.values(error.error).join(', '));
            } else {
              this.toastService.showError('An error occurred while updating the inventory.');
            }
          }
        });
    }
  }

  performSearch(): void {
    const term = this.searchTerm.trim();
    console.log('Searching for:', term, 'in:', this.searchType);
    this.isSearching = !!term;
    
    if (this.isSearching) {
      this.searchPage = 0;
      this.searchInventory();
    } else {
      this.currentPage = 0;
      this.loadInventory();
    }
  }

  searchInventory(): void {
    this.loading = true;
    this.inventoryService.searchInventory(this.searchTerm, this.searchType, this.searchPage, this.pageSize)
      .subscribe({
        next: (data) => {
          this.inventory = data;
          this.loading = false;
          this.hasMoreRecords = data.length === this.pageSize;
        },
        error: (error) => {
          console.error('Error searching inventory:', error);
          this.toastService.showError('Failed to search inventory. Please try again.');
          this.loading = false;
        }
      });
  }

  nextPage(): void {
    this.currentPage++;
    this.loadInventory();
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadInventory();
    }
  }

  nextSearchPage(): void {
    this.searchPage++;
    this.searchInventory();
  }

  previousSearchPage(): void {
    if (this.searchPage > 0) {
      this.searchPage--;
      this.searchInventory();
    }
  }

  openUploadModal(): void {
    this.showUploadModal = true;
  }

  closeUploadModal(refreshData: boolean): void {
    this.showUploadModal = false;
    
    if (refreshData) {
      console.log('Refreshing inventory data after upload');
      
      // Force a delay to ensure backend processing is complete
      setTimeout(() => {
        // Clear any cached data
        this.inventory = [];
        
        // Reset to first page
        this.currentPage = 0;
        this.searchPage = 0;
        this.isSearching = false;
        
        // Load fresh data
        this.loadInventory();
        
        // Show success message
        this.toastService.showSuccess('Inventory updated successfully');
      }, 500);
    }
  }
  clearSearch(): void {
    this.searchTerm = '';
    this.searchType = 'all';
    this.loadInventory();
  }
} 
