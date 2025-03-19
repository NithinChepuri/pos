import { Routes } from '@angular/router';
import { ClientsComponent } from './clients/clients.component';
import { AddClientModalComponent } from './clients/add-client-modal/add-client-modal.component';
import { ProductsComponent } from './products/products.component';
import { InventoryComponent } from './inventory/inventory.component';
import { AuthGuard } from './guards/auth.guard';
import { AuthResolver } from './resolvers/auth.resolver';
import { SupervisorGuard } from './guards/supervisor.guard';
// import { UploadInventoryComponent } from './inventory/upload-inventory/upload-inventory.component';
import { LoginComponent } from './auth/login/login.component';

export const routes: Routes = [
  { 
    path: '', 
    pathMatch: 'full',
    canActivate: [AuthGuard],
    resolve: { auth: AuthResolver },
    component: ClientsComponent
  },
  { 
    path: 'login',
    component: LoginComponent
  },
  { 
    path: 'signup',
    loadComponent: () => import('./auth/signup/signup.component').then(m => m.SignupComponent)
  },
  { 
    path: 'clients',
    canActivate: [AuthGuard],
    children: [
      { 
        path: '', 
        component: ClientsComponent
      },
      { 
        path: 'add', 
        component: AddClientModalComponent
      }
    ]
  },
  { 
    path: 'products',
    canActivate: [AuthGuard],
    resolve: { auth: AuthResolver },
    children: [
      { 
        path: '', 
        component: ProductsComponent 
      }
    ]
  },
  { 
    path: 'inventory',
    canActivate: [AuthGuard],
    children: [
      { 
        path: '', 
        component: InventoryComponent 
      }
    ]
  },
  {
    path: 'orders',
    canActivate: [AuthGuard],
    children: [
      { 
        path: '', 
        loadComponent: () => import('./orders/orders.component').then(m => m.OrdersComponent)
      },
      { 
        path: ':id', 
        loadComponent: () => import('./orders/order-details/order-details.component').then(m => m.OrderDetailsComponent)
      }
    ]
  },
  {
    path: 'sales',
    loadComponent: () => import('./reports/sales-report/sales-report.component')
      .then(m => m.SalesReportComponent)
  },
  { path: '**', redirectTo: '' }
];
