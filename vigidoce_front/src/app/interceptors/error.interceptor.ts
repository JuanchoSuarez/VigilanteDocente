import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 0) {
        toastService.error('Sin conexión al servidor');
      } else if (error.status === 401) {
        localStorage.removeItem('usuarioActual');
        toastService.warning('Sesión expirada o credenciales incorrectas');
        router.navigate(['/login']);
      } else if (error.status >= 500) {
        toastService.error('Error del servidor, intenta de nuevo');
      } else {
        toastService.error(error.error?.mensaje || 'Error desconocido');
      }
      return throwError(() => error);
    })
  );
};
