import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { UserRole } from './core/models/user.model';

export const routes: Routes = [
  { path: '', redirectTo: 'auth-init', pathMatch: 'full' },
  { 
    path: 'auth-init', 
    loadComponent: () => import('./features/auth/auth-init/auth-init.component').then(m => m.AuthInitComponent) 
  },
  { 
    path: 'no-access', 
    loadComponent: () => import('./features/auth/no-access/no-access.component').then(m => m.NoAccessComponent) 
  },
  {
    path: '',
    loadComponent: () => import('./shared/layout/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
    canActivate: [authGuard],
    children: [
      { 
        path: 'statistics', 
        loadComponent: () => import('./features/statistics/statistics.component').then(m => m.StatisticsComponent) 
      },
      { 
        path: 'requests', 
        loadComponent: () => import('./features/requests/requests.component').then(m => m.RequestsComponent),
        canActivate: [roleGuard],
        data: { roles: [UserRole.OWNER, UserRole.ADMIN] }
      },
      { 
        path: 'users', 
        loadComponent: () => import('./features/users/users.component').then(m => m.UsersComponent) 
      }
    ]
  },
  { path: '**', redirectTo: 'statistics' }
];
