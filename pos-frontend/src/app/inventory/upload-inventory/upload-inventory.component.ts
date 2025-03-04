import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { InventoryService } from '../../services/inventory.service';
import { HttpEventType } from '@angular/common/http';

@Component({
  selector: 'app-upload-inventory',
  templateUrl: './upload-inventory.component.html',
  styleUrls: ['./upload-inventory.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule]
})
export class UploadInventoryComponent {
  selectedFile: File | null = null;
  loading = false;
  error = '';
  success = '';
  progress = 0;

  constructor(
    private inventoryService: InventoryService,
    private router: Router
  ) {}

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.name.endsWith('.tsv')) {
        this.selectedFile = file;
        this.error = '';
        console.log('Selected file:', file);
      } else {
        this.error = 'Please select a valid TSV file (must have .tsv extension)';
        this.selectedFile = null;
        event.target.value = '';
      }
    }
  }

  onSubmit(): void {
    if (!this.selectedFile) {
      this.error = 'Please select a TSV file first';
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';
    this.progress = 0;

    this.inventoryService.uploadInventory(this.selectedFile)
      .subscribe({
        next: (event: any) => {
          if (event.type === HttpEventType.UploadProgress) {
            this.progress = Math.round(100 * event.loaded / event.total);
          } 
          else if (event.type === HttpEventType.Response) {
            if (event.status === 200) {
              this.success = 'Inventory uploaded successfully!';
              this.loading = false;
              setTimeout(() => {
                this.router.navigate(['/inventory']);
              }, 2000);
            }
          }
        },
        error: (error) => {
          console.error('Upload error:', error);
          this.loading = false;
          this.progress = 0;
          if (error.status === 0) {
            this.error = 'Cannot connect to server. Please check if server is running.';
          } else if (typeof error.error === 'string') {
            this.error = error.error;
          } else if (error.error?.message) {
            this.error = error.error.message;
          } else {
            this.error = 'Failed to upload inventory. Please check the file format and try again.';
          }
        },
        complete: () => {
          this.loading = false;
        }
      });
  }
} 