import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TurnoService } from '../../services/turno.service';
import { ApiService } from '../../services/api.service';
import { Turno, EstadoTurno, TipoFranja } from '../../models/turno.model';
import { Incidente } from '../../models/incidente.model';
import { Zona } from '../../models/zona.model';
import { Reasignacion, EstadoReasignacion } from '../../models/reasignacion.model';
import { Docente } from '../../models/docente.model';

@Component({
  selector: 'app-dashboard-coordinador',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './dashboard-coordinador.html',
  styleUrl: './dashboard-coordinador.css'
})
export class DashboardCoordinadorComponent implements OnInit {
  turnosHoy: Turno[] = [];
  incidentes: Incidente[] = [];
  zonas: Zona[] = [];
  docentes: Docente[] = [];
  reasignacionesPendientes: Reasignacion[] = [];
  loading = false;
  filtroFranja: string = 'TODOS';

  // Modal reasignar
  showReasignarModal = false;
  turnoAReasignar: Turno | null = null;
  docenteReemplazoId: number | null = null;

  get hoy(): string {
    return new Date().toLocaleDateString('es-CO', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
  }

  get turnosFiltrados(): Turno[] {
    if (this.filtroFranja === 'TODOS') return this.turnosHoy;
    return this.turnosHoy.filter(t => t.tipoFranja === this.filtroFranja);
  }

  get turnosConProblema(): Turno[] {
    return this.turnosHoy.filter(t => t.estado === EstadoTurno.AUSENTE || t.estado === EstadoTurno.PENDIENTE);
  }

  constructor(private turnoService: TurnoService, private api: ApiService) {}

  ngOnInit(): void {
    this.loading = true;
    const hoy = new Date().toISOString().split('T')[0];

    this.turnoService.getTurnosPorFecha(hoy).subscribe({
      next: (t) => { this.turnosHoy = t; this.loading = false; },
      error: () => { this.loading = false; }
    });

    this.api.getAll<Incidente>('incidentes').subscribe(i => {
      this.incidentes = i.slice(0, 5);
    });

    this.api.getAll<Zona>('zonas').subscribe(z => this.zonas = z);
    this.api.getAll<Docente>('docentes').subscribe(d => this.docentes = d.filter(doc => doc.activo));
    this.api.getAll<Reasignacion>('reasignaciones').subscribe(r => {
      this.reasignacionesPendientes = r.filter(re => re.estado === EstadoReasignacion.PENDIENTE);
    });
  }

  getStatusZona(zona: Zona): 'verde' | 'amarillo' | 'rojo' {
    const turno = this.turnosHoy.find(t => t.zona?.id === zona.id);
    if (!turno) return 'rojo';
    if (turno.estado === EstadoTurno.EN_CURSO) return 'verde';
    if (turno.estado === EstadoTurno.PENDIENTE) return 'amarillo';
    return 'rojo';
  }

  getDocenteZona(zona: Zona): string {
    const turno = this.turnosHoy.find(t => t.zona?.id === zona.id);
    if (!turno) return 'Sin asignar';
    return `${turno.docente?.nombre} ${turno.docente?.apellido}`;
  }

  abrirReasignar(turno: Turno): void {
    this.turnoAReasignar = turno;
    this.docenteReemplazoId = null;
    this.showReasignarModal = true;
  }

  confirmarReasignar(): void {
    if (!this.turnoAReasignar || !this.docenteReemplazoId) return;
    const reas: Partial<Reasignacion> = {
      turno: { id: this.turnoAReasignar.id } as Turno,
      docenteOriginal: { id: this.turnoAReasignar.docente?.id } as Docente,
      docenteReemplazo: { id: this.docenteReemplazoId } as Docente,
      motivo: 'Reasignación por coordinador',
      fechaHoraSolicitud: new Date().toISOString(),
      estado: EstadoReasignacion.ACEPTADA
    };
    this.api.post('reasignaciones', reas).subscribe({
      next: () => { this.showReasignarModal = false; this.ngOnInit(); }
    });
  }

  estadoClass(estado: EstadoTurno): string {
    const map: Record<string, string> = {
      EN_CURSO: 'badge bg-success',
      PENDIENTE: 'badge bg-warning text-dark',
      CERRADO: 'badge bg-secondary',
      AUSENTE: 'badge bg-danger'
    };
    return map[estado] ?? 'badge bg-secondary';
  }

  severidadClass(sev: string): string {
    if (sev === 'S3_ATENCION_INMEDIATA') return 'badge bg-danger';
    if (sev === 'S2_SEGUIMIENTO') return 'badge bg-warning text-dark';
    return 'badge bg-success';
  }
}
