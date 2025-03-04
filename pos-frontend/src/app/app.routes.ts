import { Routes } from '@angular/router';
import { ClientsComponent } from './clients/clients.component';
import { AddClientComponent } from './clients/add-client/add-client.component';
import { ProductsComponent } from './products/products.component';
import { AddProductComponent } from './products/add-product/add-product.component';
import { InventoryComponent } from './inventory/inventory.component';
import { AuthGuard } from './guards/auth.guard';
import { AuthResolver } from './resolvers/auth.resolver';
import { SupervisorGuard } from './guards/supervisor.guard';
import { UploadProductComponent } from './products/upload-product/upload-product.component';
import { UploadInventoryComponent } from './inventory/upload-inventory/upload-inventory.component';

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
    loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent)
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
        component: AddClientComponent
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
      },
      { 
        path: 'upload', 
        component: UploadProductComponent,
        canActivate: [SupervisorGuard]
      },
      {
        path: 'add',
        component: AddProductComponent,
        canActivate: [AuthGuard, SupervisorGuard]
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
      },
      { 
        path: 'upload', 
        component: UploadInventoryComponent,
        canActivate: [SupervisorGuard]
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
        path: 'add',
        loadComponent: () => import('./orders/add-order/add-order.component').then(m => m.AddOrderComponent)
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
  { 
    path: '**', 
    component: ClientsComponent,
    canActivate: [AuthGuard]
  }
];
