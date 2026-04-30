import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { TurnoService } from '../../services/turno.service';
import { ToastService } from '../../services/toast.service';
import { Docente } from '../../models/docente.model';
import { Turno, EstadoTurno } from '../../models/turno.model';
import { Incidente, TipoIncidente, SeveridadIncidente } from '../../models/incidente.model';
import { RegistroVigilancia, MetodoRegistro } from '../../models/registro-vigilancia.model';
import { Reasignacion, EstadoReasignacion } from '../../models/reasignacion.model';
import { FranjaHorario } from '../../models/franja-horario.model';
import { EstadoTurnoPipe } from '../../pipes/estado.pipe';

@Component({
  selector: 'app-dashboard-docente',
  standalone: true,
  imports: [FormsModule, EstadoTurnoPipe],
  templateUrl: './dashboard-docente.html',
  styleUrl: './dashboard-docente.css'
})
export class DashboardDocenteComponent implements OnInit, OnDestroy {
  usuario: Docente | null = null;
  turnosHoy: Turno[] = [];
  turnoActivo: Turno | null = null;
  franjasHoy: FranjaHorario[] = [];
  turnosProximos: Turno[] = [];
  incidentes: Incidente[] = [];
  loading = false;
  fechaServidor = '';
  
  private destroy$ = new Subject<void>();

  // Modal Check-in
  showCheckinModal = false;
  checkinPaso = 1;
  metodoSeleccionado: MetodoRegistro | null = null;
  pinInput = '';
  metodos = [
    { value: MetodoRegistro.QR, label: 'Código QR', icon: 'bi-qr-code', desc: 'Escanea el código de la zona' },
    { value: MetodoRegistro.PIN, label: 'PIN', icon: 'bi-123', desc: 'Ingresa el PIN de 4 dígitos' },
    { value: MetodoRegistro.NFC, label: 'NFC', icon: 'bi-wifi', desc: 'Acerca tu dispositivo al lector' },
  ];

  // Modal Incidente
  showIncidenteModal = false;
  incidentePaso = 1;
  tipoIncidente: TipoIncidente | null = null;
  severidadIncidente: SeveridadIncidente | null = null;
  descripcionIncidente = '';
  observacionEstudiante = '';

  tipos = [
    { value: TipoIncidente.SEGURIDAD_FISICA, label: 'Seguridad Física', icon: 'bi-shield-exclamation', color: '#dc3545' },
    { value: TipoIncidente.CONVIVENCIA, label: 'Convivencia', icon: 'bi-people', color: '#fd7e14' },
    { value: TipoIncidente.USO_ESPACIO, label: 'Uso del Espacio', icon: 'bi-building', color: '#0d6efd' },
    { value: TipoIncidente.OBSERVACION_SOCIAL, label: 'Observación Social', icon: 'bi-eye', color: '#6f42c1' },
  ];

  severidades = [
    { value: SeveridadIncidente.S1_LEVE, label: 'S1 - Leve', desc: 'Situación menor, sin riesgo inmediato', color: '#198754', icon: 'bi-circle-fill' },
    { value: SeveridadIncidente.S2_SEGUIMIENTO, label: 'S2 - Seguimiento', desc: 'Requiere monitoreo y seguimiento posterior', color: '#ffc107', icon: 'bi-circle-fill' },
    { value: SeveridadIncidente.S3_ATENCION_INMEDIATA, label: 'S3 - Atención Inmediata', desc: 'Requiere intervención urgente', color: '#dc3545', icon: 'bi-circle-fill' },
  ];

  // Modal Reasignación
  showReasignacionModal = false;
  motivoReasignacion = '';

  get saludo(): string {
    const h = new Date().getHours();
    if (h < 12) return 'Buenos días';
    if (h < 18) return 'Buenas tardes';
    return 'Buenas noches';
  }

  constructor(
    private auth: AuthService,
    private api: ApiService,
    private turnoService: TurnoService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.usuario = this.auth.getUsuarioActual();
    this.cargarDatos();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarDatos(): void {
    if (!this.usuario?.id) return;
    this.loading = true;

    this.api.getCustom<{fecha: string, hora: string}>('servidor/fecha-hora').pipe(takeUntil(this.destroy$)).subscribe({
      next: (servidor) => {
        this.fechaServidor = servidor.fecha;

        forkJoin({
          turnos: this.turnoService.getTurnosDocenteFecha(this.usuario!.id!, this.fechaServidor),
          franjas: this.api.getCustom<FranjaHorario[]>(`franjas/docente/${this.usuario!.id!}/hoy`),
          incidentes: this.api.getAll<Incidente>('incidentes')
        }).pipe(takeUntil(this.destroy$)).subscribe({
          next: (data) => {
            this.turnosHoy = data.turnos;
            this.franjasHoy = data.franjas;
            this.turnoActivo = this.turnosHoy.find(t =>
              t.estado === EstadoTurno.EN_CURSO || t.estado === EstadoTurno.PENDIENTE
            ) ?? null;
            this.incidentes = data.incidentes.filter(i => i.turno?.docente?.id === this.usuario?.id);
            this.loading = false;
          },
          error: () => this.loading = false
        });
      },
      error: () => this.loading = false
    });
  }

  // ─── CHECK-IN ────────────────────────────────────────────────
  abrirCheckin(): void {
    this.checkinPaso = 1;
    this.metodoSeleccionado = null;
    this.pinInput = '';
    this.showCheckinModal = true;
  }

  seleccionarMetodo(m: MetodoRegistro): void {
    this.metodoSeleccionado = m;
    this.checkinPaso = 2;
  }

  confirmarCheckin(): void {
    if (!this.turnoActivo || !this.usuario || !this.metodoSeleccionado) return;

    const registro: Partial<RegistroVigilancia> = {
      turno: { id: this.turnoActivo.id } as Turno,
      docente: { id: this.usuario.id } as Docente,
      zona: { id: this.turnoActivo.zona.id } as any,
      fechaHoraCheckIn: `${this.fechaServidor}T12:00:00`, // Idealmente usar servidor hora actual real
      metodoRegistro: this.metodoSeleccionado,
      recorridoRealizado: false
    };

    this.api.post<RegistroVigilancia>('registros', registro).subscribe({
      next: () => {
        this.showCheckinModal = false;
        this.toast.success('Check-in completado exitosamente');
        if (this.turnoActivo?.id) {
          this.api.patchCustom(`turnos/${this.turnoActivo.id}/estado?estado=EN_CURSO`, {}).subscribe(() => this.cargarDatos());
        } else {
          this.cargarDatos();
        }
      }
    });
  }

  // ─── INCIDENTE ───────────────────────────────────────────────
  abrirIncidente(): void {
    this.incidentePaso = 1;
    this.tipoIncidente = null;
    this.severidadIncidente = null;
    this.descripcionIncidente = '';
    this.observacionEstudiante = '';
    this.showIncidenteModal = true;
  }

  seleccionarTipo(tipo: TipoIncidente): void {
    this.tipoIncidente = tipo;
    this.incidentePaso = 2;
  }

  seleccionarSeveridad(sev: SeveridadIncidente): void {
    this.severidadIncidente = sev;
    this.incidentePaso = 3;
  }

  confirmarIncidente(): void {
    if (!this.turnoActivo || !this.tipoIncidente || !this.severidadIncidente) return;

    const incidente = {
      turno: { id: this.turnoActivo.id },
      zona: { id: this.turnoActivo.zona.id },
      tipo: this.tipoIncidente,
      severidad: this.severidadIncidente,
      descripcion: this.descripcionIncidente,
      fechaHora: `${this.fechaServidor}T12:00:00`, // Omitir hora para simplificar por ahora, backend puede usar actual
      observacionEstudiante: this.observacionEstudiante || null
    };

    this.api.post('incidentes', incidente).subscribe({
      next: () => { 
        this.showIncidenteModal = false; 
        this.toast.success('Incidente reportado exitosamente');
        this.cargarDatos(); 
      }
    });
  }

  // ─── REASIGNACIÓN ────────────────────────────────────────────
  abrirReasignacion(): void {
    this.motivoReasignacion = '';
    this.showReasignacionModal = true;
  }

  confirmarReasignacion(): void {
    if (!this.turnoActivo || !this.usuario || !this.motivoReasignacion.trim()) return;

    const reas: Partial<Reasignacion> = {
      turno: { id: this.turnoActivo.id } as Turno,
      docenteOriginal: { id: this.usuario.id } as Docente,
      motivo: this.motivoReasignacion,
      fechaHoraSolicitud: `${this.fechaServidor}T${new Date().toLocaleTimeString('en-GB')}`,
      estado: EstadoReasignacion.PENDIENTE
    };

    this.api.post('reasignaciones', reas).subscribe({
      next: () => { 
        this.showReasignacionModal = false; 
        this.toast.success('Solicitud enviada al coordinador');
      }
    });
  }
}
