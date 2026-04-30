import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { Docente } from '../../models/docente.model';

import { RolDocentePipe } from '../../pipes/rol.pipe';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, RolDocentePipe],
  templateUrl: './layout.html',
  styleUrl: './layout.css'
})
export class LayoutComponent implements OnInit, OnDestroy {
  usuario: Docente | null = null;
  horaActual = '';
  sidebarOpen = false;
  private intervalo: ReturnType<typeof setInterval> | null = null;

  constructor(private auth: AuthService, private router: Router, private api: ApiService) {}

  ngOnInit(): void {
    this.usuario = this.auth.getUsuarioActual();
    this.aplicarRolAlBody();

    // Sync clock with server
    this.api.getCustom<{hora: string, fecha: string}>('servidor/fecha-hora').subscribe({
      next: (data) => {
        const parts = data.hora.split(':');
        const d = new Date();
        d.setHours(+parts[0], +parts[1], +parts[2] || 0);
        this.actualizarHoraDesde(d);
        this.intervalo = setInterval(() => {
          d.setSeconds(d.getSeconds() + 1);
          this.actualizarHoraDesde(d);
        }, 1000);
      },
      error: () => {
        // Fallback to local time
        this.actualizarHora();
        this.intervalo = setInterval(() => this.actualizarHora(), 1000);
      }
    });
  }

  private aplicarRolAlBody(): void {
    const body = document.body;
    body.classList.remove('rol-docente', 'rol-coordinador', 'rol-admin');
    const rol = this.usuario?.rol;
    if (rol === 'DOCENTE') body.classList.add('rol-docente');
    else if (rol === 'COORDINADOR') body.classList.add('rol-coordinador');
    else if (rol === 'ADMINISTRADOR') body.classList.add('rol-admin');
  }

  ngOnDestroy(): void {
    if (this.intervalo) clearInterval(this.intervalo);
  }

  private actualizarHora(): void {
    this.horaActual = new Date().toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  }

  private actualizarHoraDesde(d: Date): void {
    this.horaActual = d.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  get iniciales(): string {
    if (!this.usuario) return '??';
    return `${this.usuario.nombre[0]}${this.usuario.apellido[0]}`.toUpperCase();
  }

  get isDocente(): boolean { return this.usuario?.rol === 'DOCENTE'; }
  get isCoordinador(): boolean { return this.usuario?.rol === 'COORDINADOR'; }
  get isAdmin(): boolean { return this.usuario?.rol === 'ADMINISTRADOR'; }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }
}
