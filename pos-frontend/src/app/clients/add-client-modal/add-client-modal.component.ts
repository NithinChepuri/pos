import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Client } from '../../models/client';

@Component({
  selector: 'app-add-client-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="modal fade show" style="display: block; background: rgba(0,0,0,0.5)">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h4 class="modal-title">Add New Client</h4>
            <button type="button" class="btn-close" (click)="close()"></button>
          </div>
          <div class="modal-body">
            <form #clientForm="ngForm">
              <div class="mb-3">
                <label for="name" class="form-label">Name</label>
                <input type="text" 
                       class="form-control" 
                       id="name" 
                       name="name"
                       [(ngModel)]="client.name" 
                       required>
              </div>
              <div class="mb-3">
                <label for="phoneNumber" class="form-label">Phone</label>
                <input type="tel" 
                       class="form-control" 
                       id="phoneNumber" 
                       name="phoneNumber"
                       [(ngModel)]="client.phoneNumber" 
                       required>
              </div>
              <div class="mb-3">
                <label for="email" class="form-label">Email</label>
                <input type="email" 
                       class="form-control" 
                       id="email" 
                       name="email"
                       [(ngModel)]="client.email">
              </div>
            </form>
            <div *ngIf="error" class="alert alert-danger">
              {{ error }}
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" (click)="close()">Cancel</button>
            <button type="button" 
                    class="btn btn-primary" 
                    (click)="save()"
                    [disabled]="!clientForm.form.valid || loading">
              {{ loading ? 'Saving...' : 'Save' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      z-index: 1050;
    }
  `]
})
export class AddClientModalComponent {
  @Output() saved = new EventEmitter<Client>();
  @Output() cancelled = new EventEmitter<void>();

  client: Partial<Client> = {};
  error = '';
  loading = false;

  save() {
    if (!this.client.name || !this.client.phoneNumber) {
      this.error = 'Please fill in all required fields';
      return;
    }
    this.saved.emit(this.client as Client);
  }

  close() {
    this.cancelled.emit();
  }
} 