import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { ClientService, ClientSearchType } from '../services/client.service';
import { Client } from '../models/client';
import { Subscription } from 'rxjs';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { AddClientModalComponent } from './add-client-modal/add-client-modal.component';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-clients',
  templateUrl: './clients.component.html',
  styleUrls: ['./clients.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, AddClientModalComponent]
})
export class ClientsComponent implements OnInit, OnDestroy {
  clients: Client[] = [];
  loading = false;
  editingClient: Client | null = null;
  private routerSubscription: Subscription;
  error = '';
  searchTerm = '';
  private searchSubject = new Subject<string>();
  searchType: ClientSearchType = 'all';
  showAddModal = false;
  isSupervisor: boolean;

  constructor(
    private clientService: ClientService,
    private router: Router,
    private authService: AuthService
  ) {
    this.isSupervisor = this.authService.isSupervisor();
    
    // Initialize router subscription
    this.routerSubscription = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      if (this.router.url === '/clients') {
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
          return this.clientService.getClients();
        }
        return this.clientService.searchClients(term, this.searchType);
      })
    ).subscribe({
      next: (clients) => {
        console.log('Search results:', clients);
        this.clients = clients;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error searching clients:', error);
        this.error = 'Failed to search clients. Please try again.';
        this.loading = false;
      }
    });
  }

  ngOnInit() {
    this.loadClients();
  }

  ngOnDestroy() {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
  }

  loadClients() {
    this.loading = true;
    this.clientService.getClients().subscribe({
      next: (data) => {
        this.clients = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error fetching clients:', error);
        this.loading = false;
      }
    });
  }

  startEdit(client: Client): void {
    this.editingClient = { ...client };
  }

  cancelEdit(): void {
    this.editingClient = null;
  }

  saveEdit(): void {
    if (this.editingClient && this.editingClient.id) {
      this.clientService.updateClient(this.editingClient.id, this.editingClient)
        .subscribe({
          next: (updatedClient) => {
            const index = this.clients.findIndex(c => c.id === updatedClient.id);
            if (index !== -1) {
              this.clients[index] = updatedClient;
            }
            this.editingClient = null;
            this.error = '';
          },
          error: (error) => {
            console.error('Error updating client:', error);
            if (error.error && typeof error.error === 'object') {
              this.error = Object.values(error.error).join(', ');
            } else {
              this.error = 'An error occurred while updating the client.';
            }
          }
        });
    }
  }

  deleteClient(id: number): void {
    if (confirm('Are you sure you want to delete this client?')) {
      this.clientService.deleteClient(id)
        .subscribe({
          next: () => {
            this.clients = this.clients.filter(c => c.id !== id);
            this.error = '';
          },
          error: (error) => {
            console.error('Error deleting client:', error);
            this.error = error.error?.error || 'An error occurred while deleting the client.';
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

  openAddClientModal() {
    this.showAddModal = true;
  }

  onClientSaved(client: Client) {
    this.clientService.createClient(client).subscribe({
      next: (savedClient) => {
        console.log('Client created:', savedClient);
        this.loadClients();
        this.showAddModal = false;
        this.error = '';
      },
      error: (error) => {
        console.error('Error creating client:', error);
        this.error = error.error?.error || 'An error occurred while creating the client.';
      }
    });
  }

  onModalCancelled() {
    this.showAddModal = false;
  }
} 