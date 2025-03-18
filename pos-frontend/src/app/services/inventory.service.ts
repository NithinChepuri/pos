import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, forkJoin, map, switchMap, of, catchError, throwError } from 'rxjs';
import { map as rxjsMap, tap } from 'rxjs/operators';
import { Inventory } from '../models/inventory';
import { Product } from '../models/product';
import { AuthService } from './auth.service';
import { UploadResponse } from '../models/upload-response';

export type InventorySearchType = 'barcode' | 'all' | 'product';

interface InventorySearchForm {
  barcode?: string;
  productName?: string;
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

  getInventory(page: number = 0, size: number = 10): Observable<Inventory[]> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return forkJoin({
      inventory: this.http.get<Inventory[]>(`${this.baseUrl}/inventory`, { params }),
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

  uploadInventory(file: File): Observable<UploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    
    return this.http.post<UploadResponse>(`${this.baseUrl}/inventory/upload`, formData);
  }

  searchInventory(query: string, type: InventorySearchType = 'all', page: number = 0, size: number = 10): Observable<Inventory[]> {
    const searchForm: InventorySearchForm = {};

    // If query is empty, return all inventory
    if (!query?.trim()) {
      return this.getInventory(page, size);
    }

    // Add search criteria based on type
    switch (type) {
      case 'barcode':
        searchForm.barcode = query;
        break;
      case 'product':
        searchForm.productName = query;
        break;
      case 'all':
        searchForm.barcode = query;
        searchForm.productName = query;
        break;
    }

    console.log('Sending inventory search request with:', searchForm);

    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    // If search fails, fall back to filtering the existing inventory client-side
    return this.http.post<Inventory[]>(`${this.baseUrl}/inventory/search`, searchForm, { params }).pipe(
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
          return this.getInventory(page, size).pipe(
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
      if (!item.product) return false;

      const matchesBarcode = criteria.barcode ? 
        item.product.barcode.toLowerCase().includes(criteria.barcode.toLowerCase()) : 
        true;

      const matchesName = criteria.productName ? 
        item.product.name.toLowerCase().includes(criteria.productName.toLowerCase()) : 
        true;

      // For 'all' search, match either barcode or name
      if (criteria.barcode && criteria.productName) {
        return matchesBarcode || matchesName;
      }

      // For specific searches, match the respective criterion
      return matchesBarcode && matchesName;
    });
  }
} 