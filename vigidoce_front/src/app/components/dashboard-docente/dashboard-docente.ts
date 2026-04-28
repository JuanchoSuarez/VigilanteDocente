import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { TurnoService } from '../../services/turno.service';
import { Docente } from '../../models/docente.model';
import { Turno, EstadoTurno } from '../../models/turno.model';
import { Incidente, TipoIncidente, SeveridadIncidente } from '../../models/incidente.model';
import { RegistroVigilancia, MetodoRegistro } from '../../models/registro-vigilancia.model';
import { Reasignacion, EstadoReasignacion } from '../../models/reasignacion.model';

@Component({
  selector: 'app-dashboard-docente',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './dashboard-docente.html',
  styleUrl: './dashboard-docente.css'
})
export class DashboardDocenteComponent implements OnInit {
  usuario: Docente | null = null;
  turnosHoy: Turno[] = [];
  turnoActivo: Turno | null = null;
  turnosProximos: Turno[] = [];
  incidentes: Incidente[] = [];
  loading = false;

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
    private turnoService: TurnoService
  ) {}

  ngOnInit(): void {
    this.usuario = this.auth.getUsuarioActual();
    this.cargarDatos();
  }

  cargarDatos(): void {
    if (!this.usuario?.id) return;
    this.loading = true;

    this.turnoService.getTurnosDocente(this.usuario.id).subscribe({
      next: (turnos) => {
        const hoy = new Date().toISOString().split('T')[0];
        this.turnosHoy = turnos.filter(t => t.fecha === hoy);
        this.turnoActivo = this.turnosHoy.find(t =>
          t.estado === EstadoTurno.EN_CURSO || t.estado === EstadoTurno.PENDIENTE
        ) ?? null;
        this.turnosProximos = turnos.filter(t => t.fecha > hoy).slice(0, 5);
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });

    this.api.getAll<Incidente>('incidentes').subscribe(inc => {
      this.incidentes = inc.filter(i => i.turno?.docente?.id === this.usuario?.id);
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
      fechaHoraCheckIn: new Date().toISOString(),
      metodoRegistro: this.metodoSeleccionado,
      recorridoRealizado: false
    };

    this.api.post<RegistroVigilancia>('registros', registro).subscribe({
      next: () => {
        this.showCheckinModal = false;
        if (this.turnoActivo?.id) {
          this.api.patchCustom(`turnos/${this.turnoActivo.id}/estado?estado=EN_CURSO`, {}).subscribe(() => this.cargarDatos());
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
      fechaHora: new Date().toISOString(),
      observacionEstudiante: this.observacionEstudiante || null
    };

    this.api.post('incidentes', incidente).subscribe({
      next: () => { this.showIncidenteModal = false; this.cargarDatos(); }
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
      fechaHoraSolicitud: new Date().toISOString(),
      estado: EstadoReasignacion.PENDIENTE
    };

    this.api.post('reasignaciones', reas).subscribe({
      next: () => { this.showReasignacionModal = false; }
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
}
