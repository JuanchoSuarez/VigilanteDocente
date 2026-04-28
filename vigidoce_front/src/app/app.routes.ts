import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./components/login/login').then(m => m.LoginComponent)
  },

  // ─── DOCENTE ───────────────────────────────────────────────
  {
    path: 'docente',
    loadComponent: () => import('./components/layout/layout').then(m => m.LayoutComponent),
    canActivate: [authGuard],
    data: { rol: 'DOCENTE' },
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () => import('./components/dashboard-docente/dashboard-docente').then(m => m.DashboardDocenteComponent)
      },
      {
        path: 'turnos',
        loadComponent: () => import('./components/turnos/turnos').then(m => m.TurnosComponent)
      },
      {
        path: 'incidentes',
        loadComponent: () => import('./components/incidentes/incidentes').then(m => m.IncidentesComponent)
      }
    ]
  },

  // ─── COORDINADOR ──────────────────────────────────────────
  {
    path: 'coordinador',
    loadComponent: () => import('./components/layout/layout').then(m => m.LayoutComponent),
    canActivate: [authGuard],
    data: { rol: 'COORDINADOR' },
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () => import('./components/dashboard-coordinador/dashboard-coordinador').then(m => m.DashboardCoordinadorComponent)
      },
      {
        path: 'tablero',
        loadComponent: () => import('./components/dashboard-coordinador/dashboard-coordinador').then(m => m.DashboardCoordinadorComponent)
      },
      {
        path: 'incidentes',
        loadComponent: () => import('./components/incidentes/incidentes').then(m => m.IncidentesComponent)
      },
      {
        path: 'reasignaciones',
        loadComponent: () => import('./components/reasignaciones/reasignaciones').then(m => m.ReasignacionesComponent)
      }
    ]
  },

  // ─── ADMINISTRADOR ────────────────────────────────────────
  {
    path: 'admin',
    loadComponent: () => import('./components/layout/layout').then(m => m.LayoutComponent),
    canActivate: [authGuard],
    data: { rol: 'ADMINISTRADOR' },
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () => import('./components/dashboard-admin/dashboard-admin').then(m => m.DashboardAdminComponent)
      },
      {
        path: 'docentes',
        loadComponent: () => import('./components/docentes/docentes').then(m => m.DocentesComponent)
      },
      {
        path: 'zonas',
        loadComponent: () => import('./components/zonas/zonas').then(m => m.ZonasComponent)
      },
      {
        path: 'turnos',
        loadComponent: () => import('./components/turnos/turnos').then(m => m.TurnosComponent)
      },
      {
        path: 'incidentes',
        loadComponent: () => import('./components/incidentes/incidentes').then(m => m.IncidentesComponent)
      },
      {
        path: 'registros',
        loadComponent: () => import('./components/registros/registros').then(m => m.RegistrosComponent)
      },
      {
        path: 'reasignaciones',
        loadComponent: () => import('./components/reasignaciones/reasignaciones').then(m => m.ReasignacionesComponent)
      }
    ]
  },

  { path: '**', redirectTo: '/login' }
];
