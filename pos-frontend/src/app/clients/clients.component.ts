import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { ClientService, ClientSearchType } from '../services/client.service';
import { Client } from '../models/client';
import { Subscription } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';
import { AddClientModalComponent } from './add-client-modal/add-client-modal.component';

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
  searchType: ClientSearchType = 'all';
  showAddModal = false;
  isSupervisor: boolean;

  constructor(
    private clientService: ClientService,
    private router: Router,
    private authService: AuthService,
    private toastService: ToastService
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
    this.clients = []; // Clear previous data
    
    this.clientService.getClients().subscribe({
      next: (data) => {
        this.clients = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error fetching clients:', error);
        this.loading = false;
        this.toastService.showError('Failed to load clients. Please try again.');
      }
    });
  }

  searchClients() {
    if (!this.searchTerm.trim()) {
      this.loadClients();
      return;
    }
    
    this.loading = true;
    this.clients = []; // Clear previous data
    
    console.log('Searching for:', this.searchTerm, 'in:', this.searchType);
    
    this.clientService.searchClients(this.searchTerm, this.searchType).subscribe({
      next: (clients) => {
        console.log('Search results:', clients);
        this.clients = clients;
        this.loading = false;
        
        if (clients.length === 0) {
          this.toastService.showInfo('No clients found matching your search criteria.');
        } else {
          this.toastService.showSuccess(`Found ${clients.length} clients.`);
        }
      },
      error: (error) => {
        console.error('Error searching clients:', error);
        this.loading = false;
        this.toastService.showError('Failed to search clients. Please try again.');
      }
    });
  }

  clearSearch() {
    this.searchTerm = '';
    this.loadClients();
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
            this.toastService.showSuccess('Client updated successfully');
          },
          error: (error) => {
            console.error('Error updating client:', error);
            if (error.error && typeof error.error === 'object') {
              this.toastService.showError(Object.values(error.error).join(', '));
            } else {
              this.toastService.showError('An error occurred while updating the client.');
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
            this.toastService.showSuccess('Client deleted successfully');
          },
          error: (error) => {
            console.error('Error deleting client:', error);
            this.toastService.showError(error.error?.error || 'An error occurred while deleting the client.');
          }
        });
    }
  }

  openAddClientModal() {
    this.showAddModal = true;
  }

  closeAddModal(refreshData: any): void {
    this.showAddModal = false;
    if (refreshData === true) {
      this.loadClients();
      this.toastService.showSuccess('Client added successfully');
    }
  }
} 