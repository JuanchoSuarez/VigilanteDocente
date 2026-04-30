import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TurnoService } from '../../services/turno.service';
import { ApiService } from '../../services/api.service';
import { CatalogosService } from '../../services/catalogos.service';
import { ToastService } from '../../services/toast.service';
import { Turno, EstadoTurno, TipoFranja } from '../../models/turno.model';
import { Incidente } from '../../models/incidente.model';
import { Zona } from '../../models/zona.model';
import { Reasignacion, EstadoReasignacion } from '../../models/reasignacion.model';
import { Docente } from '../../models/docente.model';
import { EstadoTurnoPipe } from '../../pipes/estado.pipe';
import { SeveridadIncidentePipe } from '../../pipes/severidad.pipe';

@Component({
  selector: 'app-dashboard-coordinador',
  standalone: true,
  imports: [FormsModule, EstadoTurnoPipe, SeveridadIncidentePipe],
  templateUrl: './dashboard-coordinador.html',
  styleUrl: './dashboard-coordinador.css'
})
export class DashboardCoordinadorComponent implements OnInit, OnDestroy {
  turnosHoy: Turno[] = [];
  incidentes: Incidente[] = [];
  zonas: Zona[] = [];
  docentes: Docente[] = [];
  reasignacionesPendientes: Reasignacion[] = [];
  loading = false;
  filtroFranja = 'TODOS';
  fechaServidor = '';

  // ── M4: Polling silencioso ───────────────────────────────────
  private pollingInterval: ReturnType<typeof setInterval> | null = null;
  cargandoSilencioso = false;
  ultimaActualizacion: Date | null = null;

  get ultimaActualizacionLabel(): string {
    if (!this.ultimaActualizacion) return '';
    const secs = Math.floor((Date.now() - this.ultimaActualizacion.getTime()) / 1000);
    if (secs < 10) return 'Ahora mismo';
    if (secs < 60) return `hace ${secs}s`;
    return `hace ${Math.floor(secs / 60)}m`;
  }

  private destroy$ = new Subject<void>();

  // Modal reasignar
  showReasignarModal = false;
  turnoAReasignar: Turno | null = null;
  docenteReemplazoId: number | null = null;

  // ── M8: Zonas sin cobertura ──────────────────────────────────
  get zonasConProblema(): Zona[] {
    return this.zonas.filter(z => z.activa && this.getStatusZona(z) === 'rojo');
  }

  get hoy(): string {
    if (this.fechaServidor) {
      const [y, m, d] = this.fechaServidor.split('-').map(Number);
      return new Date(y, m - 1, d).toLocaleDateString('es-CO', {
        weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
      });
    }
    return new Date().toLocaleDateString('es-CO', {
      weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
    });
  }

  get turnosFiltrados(): Turno[] {
    if (this.filtroFranja === 'TODOS') return this.turnosHoy;
    return this.turnosHoy.filter(t => t.tipoFranja === this.filtroFranja);
  }

  get turnosConProblema(): Turno[] {
    return this.turnosHoy.filter(
      t => t.estado === EstadoTurno.AUSENTE || t.estado === EstadoTurno.PENDIENTE
    );
  }

  constructor(
    private turnoService: TurnoService,
    private api: ApiService,
    private catalogos: CatalogosService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.loading = true;
    this.api.getCustom<{ fecha: string; hora: string }>('servidor/fecha-hora')
      .pipe(takeUntil(this.destroy$)).subscribe({
        next: (srv) => {
          this.fechaServidor = srv.fecha;
          this.cargarDatosConFecha(srv.fecha);
        },
        error: () => {
          this.fechaServidor = new Date().toISOString().split('T')[0];
          this.cargarDatosConFecha(this.fechaServidor);
        }
      });
  }

  private cargarDatosConFecha(hoy: string): void {
    forkJoin({
      turnos:         this.turnoService.getTurnosPorFecha(hoy),
      incidentes:     this.api.getAll<Incidente>('incidentes'),
      zonas:          this.catalogos.getZonas(),
      docentes:       this.catalogos.getDocentes(),
      reasignaciones: this.api.getAll<Reasignacion>('reasignaciones')
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: (data) => {
        this.turnosHoy               = data.turnos;
        this.incidentes              = data.incidentes.slice(0, 5);
        this.zonas                   = data.zonas;
        this.docentes                = data.docentes.filter(d => d.activo);
        this.reasignacionesPendientes = data.reasignaciones.filter(
          r => r.estado === EstadoReasignacion.PENDIENTE
        );
        this.loading              = false;
        this.cargandoSilencioso   = false;
        this.ultimaActualizacion  = new Date();  // M4
        if (!this.pollingInterval) this.iniciarPolling();  // M4
      },
      error: () => {
        this.loading            = false;
        this.cargandoSilencioso = false;
      }
    });
  }

  // ── M4: Polling silencioso cada 30s ──────────────────────────
  private iniciarPolling(): void {
    this.pollingInterval = setInterval(() => {
      if (!this.fechaServidor) return;
      this.cargandoSilencioso = true;
      this.api.getCustom<{ fecha: string; hora: string }>('servidor/fecha-hora')
        .pipe(takeUntil(this.destroy$)).subscribe({
          next: (srv) => this.cargarDatosConFecha(srv.fecha),
          error: ()   => { this.cargandoSilencioso = false; }
        });
    }, 30000);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.pollingInterval) clearInterval(this.pollingInterval);
  }

  getStatusZona(zona: Zona): 'verde' | 'amarillo' | 'rojo' {
    const turno = this.turnosHoy.find(t => t.zona?.id === zona.id);
    if (!turno) return 'rojo';
    if (turno.estado === EstadoTurno.EN_CURSO)  return 'verde';
    if (turno.estado === EstadoTurno.PENDIENTE) return 'amarillo';
    return 'rojo';
  }

  getDocenteZona(zona: Zona): string {
    const turno = this.turnosHoy.find(t => t.zona?.id === zona.id);
    if (!turno) return 'Sin asignar';
    return `${turno.docente?.nombre} ${turno.docente?.apellido}`;
  }

  abrirReasignar(turno: Turno): void {
    this.turnoAReasignar    = turno;
    this.docenteReemplazoId = null;
    this.showReasignarModal = true;
  }

  // ── M8: Abrir reasignación desde banner de emergencia ────────
  abrirReasignarEmergencia(zona: Zona): void {
    const turno = this.turnosHoy.find(t =>
      t.zona?.id === zona.id &&
      (t.estado === EstadoTurno.AUSENTE || t.estado === EstadoTurno.PENDIENTE)
    );
    if (turno) {
      this.abrirReasignar(turno);
    }
  }

  confirmarReasignar(): void {
    if (!this.turnoAReasignar || !this.docenteReemplazoId) return;
    const reas: Partial<Reasignacion> = {
      turno:            { id: this.turnoAReasignar.id } as Turno,
      docenteOriginal:  { id: this.turnoAReasignar.docente?.id } as Docente,
      docenteReemplazo: { id: this.docenteReemplazoId } as Docente,
      motivo:           'Reasignación por coordinador',
      fechaHoraSolicitud: new Date().toISOString(),
      estado:           EstadoReasignacion.ACEPTADA
    };
    this.api.post('reasignaciones', reas).subscribe({
      next: () => {
        this.showReasignarModal = false;
        this.toast.success('Reasignación completada');
        this.ngOnInit();
      }
    });
  }
}
