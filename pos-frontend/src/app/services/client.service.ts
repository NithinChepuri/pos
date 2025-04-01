import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Client } from '../models/client';

export type ClientSearchType = 'all' | 'name' | 'email';

interface ClientSearchForm {
  name?: string;
  email?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ClientService {
  private baseUrl = '/employee/api';

  constructor(private http: HttpClient) { }

  searchClients(term: string, searchType: ClientSearchType): Observable<Client[]> {
    const searchForm: ClientSearchForm = {};
    
    if (searchType === 'name' || searchType === 'all') {
      searchForm.name = term;
    }
    if (searchType === 'email' || searchType === 'all') {
      searchForm.email = term;
    }

    console.log('Sending client search request with:', searchForm);

    return this.http.post<Client[]>(`${this.baseUrl}/client/search`, searchForm).pipe(
      tap(results => console.log('Client search response:', results))
    );
  }

  getClients(): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.baseUrl}/client`);
  }

  getClient(id: number): Observable<Client> {
    return this.http.get<Client>(`${this.baseUrl}/client/${id}`);
  }

  createClient(client: Omit<Client, 'id'>): Observable<Client> {
    return this.http.post<Client>(`${this.baseUrl}/client`, client);
  }

  updateClient(id: number, client: Client): Observable<Client> {
    return this.http.put<Client>(`${this.baseUrl}/client/${id}`, client);
  }

  deleteClient(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/client/${id}`);
  }
} 