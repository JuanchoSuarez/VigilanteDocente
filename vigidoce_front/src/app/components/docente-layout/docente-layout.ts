import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Docente } from '../../models/docente.model';
import { RolDocentePipe } from '../../pipes/rol.pipe';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-docente-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, RolDocentePipe],
  templateUrl: './docente-layout.html',
  styleUrl: './docente-layout.css'
})
export class DocenteLayoutComponent implements OnInit, OnDestroy {
  usuario: Docente | null = null;
  horaActual = '';
  private intervalo: ReturnType<typeof setInterval> | null = null;
  sidebarOpen = false;

  constructor(private auth: AuthService, private router: Router, private api: ApiService) {}

  ngOnInit(): void {
    this.usuario = this.auth.getUsuarioActual();
    this.aplicarRolAlBody();
    // Mostrar hora local de inmediato; luego sincronizar con servidor
    this.iniciarRelojLocal();

    this.api.getCustom<{hora: string, fecha: string}>('servidor/fecha-hora').subscribe({
      next: (data) => {
        if (this.intervalo) clearInterval(this.intervalo);
        const parts = data.hora.split(':');
        const d = new Date();
        d.setHours(+parts[0], +parts[1], +parts[2] || 0);
        this.actualizarHora(d);
        this.intervalo = setInterval(() => {
          d.setSeconds(d.getSeconds() + 1);
          this.actualizarHora(d);
        }, 1000);
      },
      error: () => { /* ya corre reloj local */ }
    });
  }

  private iniciarRelojLocal(): void {
    this.actualizarHoraLocal();
    this.intervalo = setInterval(() => this.actualizarHoraLocal(), 1000);
  }

  private actualizarHoraLocal(): void {
    this.horaActual = new Date().toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  }

  private aplicarRolAlBody(): void {
    const body = document.body;
    body.classList.remove('rol-docente', 'rol-coordinador', 'rol-admin');
    body.classList.add('rol-docente');
  }

  ngOnDestroy(): void {
    if (this.intervalo) clearInterval(this.intervalo);
  }

  private actualizarHora(d: Date): void {
    this.horaActual = d.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  get iniciales(): string {
    if (!this.usuario) return '??';
    return `${this.usuario.nombre[0]}${this.usuario.apellido[0]}`.toUpperCase();
  }
}
