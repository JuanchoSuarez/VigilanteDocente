import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * Adjunta el header "Authorization: Bearer <token>" a cada petición saliente,
 * excepto a las rutas de auth (login/registro) donde aún no hay token.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getToken();

  // No tocar las rutas de autenticación
  const isAuthRoute = req.url.includes('/auth/');

  if (token && !isAuthRoute) {
    const cloned = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(cloned);
  }

  return next(req);
};