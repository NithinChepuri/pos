import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, forkJoin, map, switchMap, of, catchError, throwError } from 'rxjs';
import { map as rxjsMap, tap } from 'rxjs/operators';
import { Inventory } from '../models/inventory';
import { Product } from '../models/product';
import { AuthService } from './auth.service';

export type InventorySearchType = 'barcode' | 'all' | 'product';

interface InventorySearchForm {
  barcode?: string;
}

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private baseUrl = '/employee/api';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getInventory(): Observable<Inventory[]> {
    return forkJoin({
      inventory: this.http.get<Inventory[]>(`${this.baseUrl}/inventory`),
      products: this.http.get<Product[]>(`${this.baseUrl}/products`)
    }).pipe(
      map(({ inventory, products }) => {
        return inventory.map(item => ({
          ...item,
          product: products.find(p => p.id === item.productId)
        }));
      })
    );
  }

  updateInventory(id: number, inventory: Inventory): Observable<Inventory> {
    // Match the exact format expected by the backend
    const updateData = {
      id: id,
      productId: inventory.productId,
      quantity: inventory.quantity
    };

    console.log('Updating inventory:', updateData);

    return this.http.put<Inventory>(`${this.baseUrl}/inventory/${id}`, updateData).pipe(
      switchMap(updatedInventory => {
        // If the update was successful, get the complete inventory item
        return this.getInventory().pipe(
          map(inventoryList => {
            const updated = inventoryList.find(item => item.id === id);
            if (!updated) {
              throw new Error('Updated inventory not found');
            }
            return updated;
          })
        );
      })
    );
  }

  uploadInventory(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file, file.name);

    const uploadUrl = `${this.baseUrl}/inventory/upload`;
    console.log('Uploading to:', uploadUrl);

    return this.http.post(uploadUrl, formData, {
      reportProgress: true,
      observe: 'events',
      responseType: 'text'
    });
  }

  searchInventory(query: string, type: InventorySearchType = 'all'): Observable<Inventory[]> {
    const searchForm: InventorySearchForm = {};

    // If query is empty, return all inventory
    if (!query?.trim()) {
      return this.getInventory();
    }

    // Add search criteria based on type
    switch (type) {
      case 'barcode':
        searchForm.barcode = query;
        break;
      case 'all':
      default:
        searchForm.barcode = query;
        break;
    }

    console.log('Sending inventory search request with:', searchForm);

    // If search fails, fall back to filtering the existing inventory client-side
    return this.http.post<Inventory[]>(`${this.baseUrl}/inventory/search`, searchForm).pipe(
      switchMap(inventory => {
        // Get all products to merge with inventory
        return this.http.get<Product[]>(`${this.baseUrl}/products`).pipe(
          map(products => {
            // Merge products with inventory items
            return inventory.map(item => ({
              ...item,
              product: products.find(p => p.id === item.productId)
            }));
          })
        );
      }),
      catchError(error => {
        if (error.status === 403) {
          // If forbidden, fall back to client-side filtering
          console.log('Search API forbidden, falling back to client-side filtering');
          return this.getInventory().pipe(
            map(inventory => this.filterInventory(inventory, searchForm))
          );
        }
        return throwError(() => error);
      }),
      tap(results => console.log('Inventory search response:', results))
    );
  }

  // Helper method to filter inventory client-side
  private filterInventory(inventory: Inventory[], criteria: InventorySearchForm): Inventory[] {
    return inventory.filter(item => {
      if (criteria.barcode && item.product) {
        return item.product.barcode.toLowerCase().includes(criteria.barcode.toLowerCase());
      }
      return true;
    });
  }
} 