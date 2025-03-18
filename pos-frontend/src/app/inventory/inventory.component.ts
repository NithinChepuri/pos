import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { InventoryService } from '../services/inventory.service';
import { Inventory } from '../models/inventory';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { InventorySearchType } from '../services/inventory.service';
import { UploadInventoryModalComponent } from './upload-inventory-modal/upload-inventory-modal.component';

@Component({
  selector: 'app-inventory',
  templateUrl: './inventory.component.html',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    RouterModule,
    UploadInventoryModalComponent
  ]
})
export class InventoryComponent implements OnInit {
  inventory: Inventory[] = [];
  loading = false;
  error = '';
  editingInventory: Inventory | null = null;
  searchTerm = '';
  private searchSubject = new Subject<string>();
  searchType: InventorySearchType = 'all';
  isSupervisor: boolean;
  showUploadModal = false;
  
  // Pagination properties
  currentPage = 0;
  pageSize = 3;
  searchPage = 0;
  searchSize = 3;
  isSearching = false;
  hasMoreRecords = true;

  constructor(private inventoryService: InventoryService, private authService: AuthService) {
    this.isSupervisor = this.authService.isSupervisor();
    // Set up search with debounce
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => {
        this.loading = true;
        this.searchPage = 0; // Reset to first page on new search
        this.isSearching = !!term.trim();
        if (!term.trim()) {
          return this.inventoryService.getInventory(this.currentPage, this.pageSize);
        }
        return this.inventoryService.searchInventory(term, this.searchType, this.searchPage, this.searchSize);
      })
    ).subscribe({
      next: (inventory) => {
        this.inventory = inventory;
        this.loading = false;
        this.checkHasMoreRecords();
      },
      error: (error) => {
        console.error('Error searching inventory:', error);
        this.error = 'Failed to search inventory. Please try again.';
        this.loading = false;
      }
    });
  }

  ngOnInit(): void {
    this.loadInventory();
  }

  loadInventory(): void {
    this.loading = true;
    this.error = '';

    this.inventoryService.getInventory(this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        this.inventory = data;
        this.loading = false;
        this.checkHasMoreRecords();
      },
      error: (error: any) => {
        console.error('Error loading inventory:', error);
        this.error = 'Failed to load inventory. Please try again.';
        this.loading = false;
      }
    });
  }

  checkHasMoreRecords(): void {
    this.hasMoreRecords = this.inventory.length === this.pageSize;
  }

  startEdit(item: Inventory): void {
    this.editingInventory = { ...item };
  }

  cancelEdit(): void {
    this.editingInventory = null;
  }

  saveEdit(): void {
    if (this.editingInventory && this.editingInventory.id) {
      this.loading = true;
      this.error = '';

      this.inventoryService.updateInventory(this.editingInventory.id, this.editingInventory)
        .subscribe({
          next: (updatedInventory) => {
            const index = this.inventory.findIndex(i => i.id === this.editingInventory!.id);
            if (index !== -1) {
              this.inventory[index] = updatedInventory;
            }
            this.editingInventory = null;
            this.loading = false;
          },
          error: (error: any) => {
            console.error('Error updating inventory:', error);
            this.error = 'Failed to update inventory. Please try again.';
            this.loading = false;
          }
        });
    }
  }

  onSearch(value: string): void {
    const term = value.trim();
    console.log('Search term:', term, 'Type:', this.searchType);
    this.searchTerm = term;
    this.isSearching = !!term.trim();
    this.searchPage = 0;
    
    if (!this.isSearching) {
      this.loadInventory();
      return;
    }
    
    this.loading = true;
    this.inventoryService.searchInventory(term, this.searchType, this.searchPage, this.searchSize)
      .subscribe({
        next: (data) => {
          this.inventory = data;
          this.loading = false;
          this.checkHasMoreRecords();
        },
        error: (error) => {
          console.error('Search error:', error);
          this.error = 'Failed to search inventory. Please try again.';
          this.loading = false;
        }
      });
  }

  openUploadModal(): void {
    this.showUploadModal = true;
  }

  closeUploadModal(refreshData: boolean): void {
    this.showUploadModal = false;
    if (refreshData) {
      this.loadInventory();
    }
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
    this.onSearch(this.searchTerm);
  }

  previousSearchPage(): void {
    if (this.searchPage > 0) {
      this.searchPage--;
      this.onSearch(this.searchTerm);
    }
  }
} 
