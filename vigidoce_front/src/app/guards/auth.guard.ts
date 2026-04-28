import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  const requiredRol = route.data['rol'];
  if (requiredRol && auth.getRol() !== requiredRol) {
    const rol = auth.getRol();
    if (rol === 'DOCENTE') router.navigate(['/docente/dashboard']);
    else if (rol === 'COORDINADOR') router.navigate(['/coordinador/dashboard']);
    else if (rol === 'ADMINISTRADOR') router.navigate(['/admin/dashboard']);
    return false;
  }

  return true;
};
