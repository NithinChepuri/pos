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

@Component({
  selector: 'app-inventory',
  templateUrl: './inventory.component.html',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule]
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

  constructor(private inventoryService: InventoryService, private authService: AuthService) {
    this.isSupervisor = this.authService.isSupervisor();
    // Set up search with debounce
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => {
        this.loading = true;
        if (!term.trim()) {
          return this.inventoryService.getInventory();
        }
        return this.inventoryService.searchInventory(term, this.searchType);
      })
    ).subscribe({
      next: (inventory) => {
        this.inventory = inventory;
        this.loading = false;
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

    this.inventoryService.getInventory().subscribe({
      next: (data) => {
        this.inventory = data;
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Error loading inventory:', error);
        this.error = 'Failed to load inventory. Please try again.';
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
    this.searchSubject.next(term);
  }
} 