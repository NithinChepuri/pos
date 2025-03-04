import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
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

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private baseUrl = '/employee/api';

  constructor(private http: HttpClient) { }

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.baseUrl}/products`);
  }

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/products/${id}`);
  }

  createProduct(product: Omit<Product, 'id'>): Observable<Product> {
    return this.http.post<Product>(`${this.baseUrl}/products`, product);
  }

  updateProduct(id: number, product: Product): Observable<Product> {
    return this.http.put<Product>(`${this.baseUrl}/products/${id}`, product);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/products/${id}`);
  }

  uploadProducts(file: File): Observable<UploadResponse> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<UploadResponse>(`${this.baseUrl}/products/upload`, formData);
  }

  searchProducts(query: string, type: SearchType = 'all', mrpRange?: { min?: number; max?: number }): Observable<Product[]> {
    const searchForm: ProductSearchForm = {};

    // Add MRP range if provided
    if (mrpRange) {
      if (mrpRange.min !== undefined) searchForm.minMrp = mrpRange.min;
      if (mrpRange.max !== undefined) searchForm.maxMrp = mrpRange.max;
    }

    // If query is empty, return all products instead of searching
    if (!query?.trim()) {
      return this.getProducts();
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

    // If search fails, fall back to filtering the existing products client-side
    return this.http.post<Product[]>(`${this.baseUrl}/products/search`, searchForm).pipe(
      catchError(error => {
        if (error.status === 403) {
          // If forbidden, fall back to client-side filtering
          console.log('Search API forbidden, falling back to client-side filtering');
          return this.getProducts().pipe(
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
      if (criteria.minMrp && product.mrp < criteria.minMrp) {
        return false;
      }
      if (criteria.maxMrp && product.mrp > criteria.maxMrp) {
        return false;
      }
      return true;
    });
  }

  addProduct(product: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/products`, product);
  }
} 