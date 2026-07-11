import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then(m => m.Login)
  },
  {
    path: 'signup',
    loadComponent: () => import('./features/auth/signup/signup').then(m => m.Signup)
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/main-layout/main-layout').then(m => m.MainLayout),
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard').then(m => m.Dashboard)
      },
      {
        path: 'transactions',
        loadComponent: () => import('./features/transactions/transactions').then(m => m.Transactions)
      },
      {
        path: 'budgets',
        loadComponent: () => import('./features/budgets/budgets').then(m => m.Budgets)
      },
      {
        path: 'export',
        loadComponent: () => import('./features/export/export').then(m => m.Export)
      }
    ]
  },
  { path: '**', redirectTo: '/login' }
];