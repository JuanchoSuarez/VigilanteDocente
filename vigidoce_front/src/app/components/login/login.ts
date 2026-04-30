import { Component, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent implements OnDestroy {
  form: FormGroup;
  loading = false;
  error = '';
  tab: 'login' | 'info' = 'login';
  focused: string | null = null;
  showPass = false;
  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private toast: ToastService
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ingresar(): void {
    if (this.form.invalid) return;
    this.loading = true;

    this.error = '';
    const { email, password } = this.form.value;
    this.auth.login(email, password)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (docente) => {
          this.loading = false;
          this.toast.success(`Bienvenido, ${docente.nombre}`);
          const rol = docente.rol;
          if (rol === 'DOCENTE') this.router.navigate(['/docente/dashboard']);
          else if (rol === 'COORDINADOR') this.router.navigate(['/coordinador/dashboard']);
          else if (rol === 'ADMINISTRADOR') this.router.navigate(['/admin/dashboard']);
        },
        error: () => {
          this.loading = false;
          this.error = 'Credenciales incorrectas. Verifica tu correo y contraseña.';
        }
      });
  }
}
