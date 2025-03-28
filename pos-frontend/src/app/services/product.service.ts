import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { Product } from '../models/product';
import { UploadResponse } from '../models/upload-response';

export type SearchType = 'name' | 'barcode' | 'client' | 'mrp' | 'all';

interface ProductSearchForm {
  name?: string;
  barcode?: string;
  clientId?: number;
  clientName?: string;
  minMrp?: number;
  maxMrp?: number;
}

export interface ApiError {
  error: string;
  message?: string;
  status?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private baseUrl = '/employee/api';

  constructor(private http: HttpClient) { }

  getProducts(page: number = 0, size: number = 10): Observable<Product[]> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Product[]>(`${this.baseUrl}/products`, { params });
  }

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/products/${id}`);
  }

  createProduct(product: Omit<Product, 'id'>): Observable<Product> {
    return this.http.post<Product>(`${this.baseUrl}/products`, product);
  }

  updateProduct(id: number, product: Partial<Product>): Observable<Product> {
    // Only send the fields that are in ProductUpdateForm
    const updateData = {
      name: product.name,
      barcode: product.barcode,
      clientId: product.clientId,
      mrp: product.mrp
    };
    
    return this.http.put<Product>(`${this.baseUrl}/products/${id}`, updateData);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/products/${id}`).pipe(
      catchError((error: HttpErrorResponse) => {
        // Transform the error into a more user-friendly format
        let apiError: ApiError = {
          error: 'An error occurred while deleting the product',
          status: error.status
        };
        
        // Handle specific error messages from the backend
        if (error.status === 400) {
          if (error.error && typeof error.error === 'object' && 'error' in error.error) {
            apiError.message = error.error.error;
          } else if (typeof error.error === 'string') {
            apiError.message = error.error;
          }
        }
        
        return throwError(() => apiError);
      })
    );
  }

  uploadProducts(file: File): Observable<UploadResponse> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<UploadResponse>(`${this.baseUrl}/products/upload`, formData);
  }

  searchProducts(query: string, type: SearchType = 'all', mrpRange?: { min?: number; max?: number }, page: number = 0, size: number = 10): Observable<Product[]> {
    const searchForm: ProductSearchForm = {};

    // Add MRP range if provided
    if (mrpRange) {
      if (mrpRange.min !== undefined) searchForm.minMrp = mrpRange.min;
      if (mrpRange.max !== undefined) searchForm.maxMrp = mrpRange.max;
    }

    // If query is empty, return all products instead of searching
    if (!query?.trim()) {
      return this.getProducts(page, size);
    }

    // Add search criteria based on type
    switch (type) {
      case 'name':
        searchForm.name = query;
        break;
      case 'barcode':
        searchForm.barcode = query;
        break;
      case 'client':
        searchForm.clientName = query;
        break;
      case 'mrp':
        const mrp = parseFloat(query);
        if (!isNaN(mrp)) {
          searchForm.minMrp = mrp;
          searchForm.maxMrp = mrp;
        }
        break;
      case 'all':
      default:
        searchForm.name = query;
        searchForm.barcode = query;
        searchForm.clientName = query;
        break;
    }

    console.log('Sending search request with:', searchForm);

    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.post<Product[]>(`${this.baseUrl}/products/search`, searchForm, { params }).pipe(
      catchError(error => {
        if (error.status === 403) {
          // If forbidden, fall back to client-side filtering
          console.log('Search API forbidden, falling back to client-side filtering');
          return this.getProducts(page, size).pipe(
            map(products => this.filterProducts(products, searchForm))
          );
        }
        return throwError(() => error);
      }),
      tap(results => console.log('Search response:', results))
    );
  }

  // Helper method to filter products client-side
  private filterProducts(products: Product[], criteria: ProductSearchForm): Product[] {
    return products.filter(product => {
      if (criteria.name && !product.name.toLowerCase().includes(criteria.name.toLowerCase())) {
        return false;
      }
      if (criteria.barcode && !product.barcode.toLowerCase().includes(criteria.barcode.toLowerCase())) {
        return false;
      }
      if (criteria.clientName && product.client && 
          !product.client.name.toLowerCase().includes(criteria.clientName.toLowerCase())) {
        return false;
      }
      if (criteria.minMrp && product.mrp < criteria.minMrp) {
        return false;
      }
      if (criteria.maxMrp && product.mrp > criteria.maxMrp) {
        return false;
      }
      return true;
    });
  }
}