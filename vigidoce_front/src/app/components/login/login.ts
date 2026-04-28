import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {
  form: FormGroup;
  loading = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  ingresar(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';

    const { email, password } = this.form.value;
    this.auth.login(email, password).subscribe({
      next: (docente) => {
        this.loading = false;
        const rol = docente.rol;
        if (rol === 'DOCENTE') this.router.navigate(['/docente/dashboard']);
        else if (rol === 'COORDINADOR') this.router.navigate(['/coordinador/dashboard']);
        else if (rol === 'ADMINISTRADOR') this.router.navigate(['/admin/dashboard']);
      },
      error: () => {
        this.loading = false;
        this.error = 'Credenciales incorrectas. Verifique email y contraseña.';
      }
    });
  }
}
