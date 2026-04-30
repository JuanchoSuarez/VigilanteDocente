import { Component, OnDestroy } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { ToastService } from '../../services/toast.service';
import { Docente } from '../../models/docente.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent implements OnDestroy {
  // ── Login ───────────────────────────────────────────────────
  form: FormGroup;
  loading = false;
  error = '';
  tab: 'login' | 'info' | 'register' = 'login';
  focused: string | null = null;
  showPass = false;

  // ── Registro ────────────────────────────────────────────────
  regForm: FormGroup;
  regLoading = false;
  regError = '';
  regShowPass = false;
  regFocused: string | null = null;
  readonly ROLES = [
    { value: 'DOCENTE',      label: 'Docente' },
    { value: 'COORDINADOR',  label: 'Coordinador' },
  ];

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private api: ApiService,
    private router: Router,
    private toast: ToastService
  ) {
    this.form = this.fb.group({
      email:    ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });

    this.regForm = this.fb.group({
      nombre:          ['', Validators.required],
      apellido:        ['', Validators.required],
      email:           ['', [Validators.required, Validators.email]],
      password:        ['', [Validators.required, Validators.minLength(4)]],
      confirmPassword: ['', Validators.required],
      rol:             ['DOCENTE', Validators.required]
    }, { validators: this.passwordsCoinciden });
  }

  private passwordsCoinciden(group: AbstractControl): ValidationErrors | null {
    const pass    = group.get('password')?.value;
    const confirm = group.get('confirmPassword')?.value;
    return pass && confirm && pass !== confirm ? { mismatch: true } : null;
  }

  get mismatch(): boolean {
    return !!(this.regForm.hasError('mismatch') &&
      this.regForm.get('confirmPassword')?.touched);
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
    this.auth.login(email, password).pipe(takeUntil(this.destroy$)).subscribe({
      next: (docente) => {
        this.loading = false;
        this.toast.success(`Bienvenido, ${docente.nombre}`);
        this.redirigir(docente.rol);
      },
      error: () => {
        this.loading = false;
        this.error = 'Credenciales incorrectas. Verifica tu correo y contraseña.';
      }
    });
  }

  registrar(): void {
    if (this.regForm.invalid) return;
    this.regLoading = true;
    this.regError = '';
    const { nombre, apellido, email, password, rol } = this.regForm.value;

    this.api.postCustom<Docente>('auth/registro', { nombre, apellido, email, password, rol })
      .pipe(takeUntil(this.destroy$)).subscribe({
        next: (docente) => {
          this.regLoading = false;
          this.auth.setUsuarioActual(docente);
          this.toast.success(`¡Cuenta creada! Bienvenido, ${docente.nombre}`);
          this.redirigir(docente.rol);
        },
        error: (err) => {
          this.regLoading = false;
          this.regError = err?.error?.mensaje ?? 'Error al crear la cuenta. Inténtalo de nuevo.';
        }
      });
  }

  private redirigir(rol: string): void {
    if (rol === 'DOCENTE')        this.router.navigate(['/docente/dashboard']);
    else if (rol === 'COORDINADOR')  this.router.navigate(['/coordinador/dashboard']);
    else if (rol === 'ADMINISTRADOR') this.router.navigate(['/admin/dashboard']);
  }
}
