import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Docente } from '../../models/docente.model';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.html',
  styleUrl: './layout.css'
})
export class LayoutComponent implements OnInit, OnDestroy {
  usuario: Docente | null = null;
  horaActual = '';
  sidebarOpen = true;
  private intervalo: ReturnType<typeof setInterval> | null = null;

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.usuario = this.auth.getUsuarioActual();
    this.actualizarHora();
    this.intervalo = setInterval(() => this.actualizarHora(), 1000);
  }

  ngOnDestroy(): void {
    if (this.intervalo) clearInterval(this.intervalo);
  }

  private actualizarHora(): void {
    this.horaActual = new Date().toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  get iniciales(): string {
    if (!this.usuario) return '??';
    return `${this.usuario.nombre[0]}${this.usuario.apellido[0]}`.toUpperCase();
  }

  get rolLabel(): string {
    const map: Record<string, string> = {
      DOCENTE: 'Docente',
      COORDINADOR: 'Coordinador',
      ADMINISTRADOR: 'Administrador'
    };
    return map[this.usuario?.rol ?? ''] ?? this.usuario?.rol ?? '';
  }

  get rolClass(): string {
    const map: Record<string, string> = {
      DOCENTE: 'badge-docente',
      COORDINADOR: 'badge-coordinador',
      ADMINISTRADOR: 'badge-admin'
    };
    return map[this.usuario?.rol ?? ''] ?? 'bg-secondary';
  }

  get isDocente(): boolean { return this.usuario?.rol === 'DOCENTE'; }
  get isCoordinador(): boolean { return this.usuario?.rol === 'COORDINADOR'; }
  get isAdmin(): boolean { return this.usuario?.rol === 'ADMINISTRADOR'; }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }
}
