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

  getInventory(page: number = 0, size: number = 3): Observable<Inventory[]> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    // Add a timestamp to prevent caching
    const timestamp = new Date().getTime();
    
    return forkJoin({
      inventory: this.http.get<Inventory[]>(`${this.baseUrl}/inventory`, { 
        params: params.set('_t', timestamp.toString()) 
      }),
      products: this.http.get<Product[]>(`${this.baseUrl}/products`, { 
        params: new HttpParams().set('_t', timestamp.toString()).set('size', '1000') // Request more products
      })
    }).pipe(
      switchMap(({ inventory, products }) => {
        console.log('Products loaded:', products.length);
        console.log('Inventory loaded:', inventory.length);
        
        // Find product IDs that aren't in the products list
        const missingProductIds = inventory
          .map(item => item.productId)
          .filter(productId => !products.some(p => p.id === productId));
        
        console.log('Missing product IDs:', missingProductIds);
        
        if (missingProductIds.length === 0) {
          // All products found, proceed with mapping
          return of(this.mapInventoryWithProducts(inventory, products));
        }
        
        // Fetch missing products individually
        const missingProductRequests = missingProductIds.map(id => 
          this.http.get<Product>(`${this.baseUrl}/products/${id}`).pipe(
            catchError(error => {
              console.error(`Failed to fetch product with ID ${id}:`, error);
              return of(null);
            })
          )
        );
        
        return forkJoin(missingProductRequests).pipe(
          map(missingProducts => {
            // Filter out null responses and add to products list
            const validMissingProducts = missingProducts.filter(p => p !== null) as Product[];
            console.log('Fetched missing products:', validMissingProducts);
            
            // Combine with existing products
            const allProducts = [...products, ...validMissingProducts];
            
            // Map inventory with complete product list
            return this.mapInventoryWithProducts(inventory, allProducts);
          })
        );
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

  searchInventory(query: string, type: InventorySearchType = 'all', page: number = 0, size: number = 3): Observable<Inventory[]> {
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

    return this.http.post<Inventory[]>(`${this.baseUrl}/inventory/search`, searchForm, { params }).pipe(
      switchMap(inventory => {
        // Get all products with a large page size
        return this.http.get<Product[]>(`${this.baseUrl}/products`, { 
          params: new HttpParams().set('size', '1000')
        }).pipe(
          switchMap(products => {
            // Find missing product IDs
            const missingProductIds = inventory
              .map(item => item.productId)
              .filter(productId => !products.some(p => p.id === productId));
            
            if (missingProductIds.length === 0) {
              // All products found, proceed with mapping
              return of(this.mapInventoryWithProducts(inventory, products));
            }
            
            // Fetch missing products individually
            const missingProductRequests = missingProductIds.map(id => 
              this.http.get<Product>(`${this.baseUrl}/products/${id}`).pipe(
                catchError(error => of(null))
              )
            );
            
            return forkJoin(missingProductRequests).pipe(
              map(missingProducts => {
                // Filter out null responses and add to products list
                const validMissingProducts = missingProducts.filter(p => p !== null) as Product[];
                
                // Combine with existing products
                const allProducts = [...products, ...validMissingProducts];
                
                // Map inventory with complete product list
                return this.mapInventoryWithProducts(inventory, allProducts);
              })
            );
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
      // If no criteria, return all items
      if (!criteria) return true;
      
      // Check if item has the necessary properties
      if (!item.barcode && !item.productName) return false;
      
      // Filter by barcode if provided
      if (criteria.barcode) {
        const matchesBarcode = item.barcode ? 
          item.barcode.toLowerCase().includes(criteria.barcode.toLowerCase()) : 
          false;
        
        if (criteria.productName) {
          // For 'all' search, match either barcode or name
          if (!matchesBarcode) {
            // If barcode doesn't match, check if name matches
            const matchesName = item.productName ? 
              item.productName.toLowerCase().includes(criteria.productName.toLowerCase()) : 
              false;
            
            return matchesName; // Return true if name matches
          }
          return true; // Barcode matches, so return true
        }
        
        return matchesBarcode; // Only barcode criteria, so return barcode match result
      }
      
      // Filter by product name if provided (and no barcode criteria)
      if (criteria.productName) {
        const matchesName = item.productName ? 
          item.productName.toLowerCase().includes(criteria.productName.toLowerCase()) : 
          false;
        
        return matchesName;
      }
      
      return true; // No criteria provided
    });
  }

  // Helper method to map inventory with products
  private mapInventoryWithProducts(inventory: Inventory[], products: Product[]): Inventory[] {
    return inventory.map(item => {
      const product = products.find(p => p.id === item.productId);
      console.log(`Mapping inventory item with productId ${item.productId} to product:`, product);
      
      return {
        ...item,
        productName: product?.name || `Not Found (ID: ${item.productId})`,
        barcode: product?.barcode || `Not Found (ID: ${item.productId})`
      };
    });
  }
} 