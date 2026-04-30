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
  incidentes: Incidente[] = [];
  loading = false;
  fechaServidor = '';
  horaServidor = '';   // M6 — hora real del servidor

  // ── F4: Estado de check-in y rondas ─────────────────────────
  /** True si el docente ya registró llegada al turno activo */
  checkinRealizado = false;
  /** Hora formateada HH:MM del primer check-in del turno activo */
  checkinHora = '';
  /** Cantidad de registros de vigilancia del docente en el día actual */
  rondasHoy = 0;

  private destroy$ = new Subject<void>();
  private intervaloPreturno: ReturnType<typeof setInterval> | null = null;

  // ── M1: Cuenta regresiva ─────────────────────────────────────
  cuentaRegresivaSegundos = -1;

  get esModoPreturno(): boolean {
    if (!this.turnoActivo || this.turnoActivo.estado !== EstadoTurno.PENDIENTE) return false;
    return this.cuentaRegresivaSegundos >= 0 && this.cuentaRegresivaSegundos <= 600;
  }

  get cuentaRegresivaTexto(): string {
    const s = this.cuentaRegresivaSegundos;
    if (s <= 0) return '¡Ahora!';
    const min = Math.floor(s / 60);
    const sec = s % 60;
    return min > 0
      ? `${min} min ${String(sec).padStart(2, '0')} seg`
      : `${sec} seg`;
  }

  // ── M2: Check-in 1 tap ───────────────────────────────────────
  readonly CHECKIN_METHOD_KEY = 'vigidoc_checkin_method';
  showCheckinModal = false;
  checkinPaso = 1;
  metodoSeleccionado: MetodoRegistro | null = null;
  pinInput = '';
  metodos = [
    { value: MetodoRegistro.QR,  label: 'Código QR', icon: 'bi-qr-code', desc: 'Escanea el código de la zona' },
    { value: MetodoRegistro.PIN, label: 'PIN',        icon: 'bi-123',     desc: 'Ingresa el PIN de 4 dígitos' },
    { value: MetodoRegistro.NFC, label: 'NFC',        icon: 'bi-wifi',    desc: 'Acerca tu dispositivo al lector' },
  ];

  get labelMetodoGuardado(): string {
    return this.metodos.find(m => m.value === this.metodoSeleccionado)?.label ?? '';
  }

  get iconMetodoGuardado(): string {
    return this.metodos.find(m => m.value === this.metodoSeleccionado)?.icon ?? 'bi-check-circle';
  }

  // ── M3: Feedback visual ──────────────────────────────────────
  checkinSuccess = false;

  // ── M5: Incidente en 2 pasos ─────────────────────────────────
  showIncidenteModal = false;
  incidentePaso = 1;
  tipoIncidente: TipoIncidente | null = null;
  severidadIncidente: SeveridadIncidente | null = null;
  descripcionIncidente = '';
  observacionEstudiante = '';

  readonly combos = [
    {
      tipo: TipoIncidente.SEGURIDAD_FISICA,
      severidad: SeveridadIncidente.S3_ATENCION_INMEDIATA,
      label: 'Emergencia', sublabel: 'Seg. Física S3',
      icon: 'bi-shield-fill-exclamation',
      bg: 'rgba(127,29,29,0.9)', color: '#ef4444', border: 'rgba(239,68,68,0.6)'
    },
    {
      tipo: TipoIncidente.SEGURIDAD_FISICA,
      severidad: SeveridadIncidente.S2_SEGUIMIENTO,
      label: 'Seg. Física', sublabel: 'Seguimiento S2',
      icon: 'bi-shield-exclamation',
      bg: 'rgba(153,27,27,0.7)', color: '#f87171', border: 'rgba(248,113,113,0.5)'
    },
    {
      tipo: TipoIncidente.CONVIVENCIA,
      severidad: SeveridadIncidente.S2_SEGUIMIENTO,
      label: 'Convivencia', sublabel: 'Seguimiento S2',
      icon: 'bi-people-fill',
      bg: 'rgba(124,45,18,0.8)', color: '#fb923c', border: 'rgba(251,146,60,0.5)'
    },
    {
      tipo: TipoIncidente.CONVIVENCIA,
      severidad: SeveridadIncidente.S1_LEVE,
      label: 'Conflicto', sublabel: 'Leve S1',
      icon: 'bi-people',
      bg: 'rgba(113,63,18,0.7)', color: '#fbbf24', border: 'rgba(251,191,36,0.5)'
    },
    {
      tipo: TipoIncidente.USO_ESPACIO,
      severidad: SeveridadIncidente.S1_LEVE,
      label: 'Espacio', sublabel: 'Uso',
      icon: 'bi-building',
      bg: 'rgba(30,58,95,0.8)', color: '#60a5fa', border: 'rgba(96,165,250,0.5)'
    },
    {
      tipo: TipoIncidente.OBSERVACION_SOCIAL,
      severidad: SeveridadIncidente.S1_LEVE,
      label: 'Observación', sublabel: 'Social',
      icon: 'bi-eye',
      bg: 'rgba(30,27,75,0.8)', color: '#a78bfa', border: 'rgba(167,139,250,0.5)'
    },
  ];

  // ── M7: Gamificación ─────────────────────────────────────────
  readonly RACHA_KEY = 'vigidoc_racha';
  readonly RACHA_FECHA_KEY = 'vigidoc_racha_fecha';
  rachaActual = 0;
  showTodosCompletados = false;

  // ── M9: Reasignación ─────────────────────────────────────────
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
    this.rachaActual = parseInt(localStorage.getItem(this.RACHA_KEY) || '0', 10);
    this.cargarDatos();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.intervaloPreturno) clearInterval(this.intervaloPreturno);
  }

  cargarDatos(): void {
    if (!this.usuario?.id) return;
    this.loading = true;

    this.api.getCustom<{ fecha: string; hora: string }>('servidor/fecha-hora')
      .pipe(takeUntil(this.destroy$)).subscribe({
        next: (servidor) => {
          this.fechaServidor = servidor.fecha;
          this.horaServidor = servidor.hora;  // M6

          forkJoin({
            turnos:    this.turnoService.getTurnosDocenteFecha(this.usuario!.id!, this.fechaServidor),
            franjas:   this.api.getCustom<FranjaHorario[]>(`franjas/docente/${this.usuario!.id!}/hoy`),
            incidentes: this.api.getAll<Incidente>('incidentes'),
            // F4: Registros del docente para conocer check-in y rondas del día
            registros: this.api.getCustom<RegistroVigilancia[]>(`registros/por-docente/${this.usuario!.id!}`)
          }).pipe(takeUntil(this.destroy$)).subscribe({
            next: (data) => {
              this.turnosHoy = data.turnos;
              this.franjasHoy = data.franjas;
              this.turnoActivo = this.turnosHoy.find(t =>
                t.estado === EstadoTurno.EN_CURSO || t.estado === EstadoTurno.PENDIENTE
              ) ?? null;
              this.incidentes = data.incidentes.filter(
                i => i.turno?.docente?.id === this.usuario?.id
              );
              // F4: Determinar estado de check-in desde los registros cargados
              const registroActivo = data.registros.find(
                r => r.turno?.id === this.turnoActivo?.id
              );
              this.checkinRealizado = !!registroActivo;
              this.checkinHora = registroActivo?.fechaHoraCheckIn?.substring(11, 16) ?? '';
              this.rondasHoy = data.registros.filter(
                r => r.fechaHoraCheckIn?.startsWith(this.fechaServidor)
              ).length;
              this.loading = false;
              this.evaluarCuentaRegresiva();  // M1
            },
            error: () => (this.loading = false)
          });
        },
        error: () => (this.loading = false)
      });
  }

  // ── M1: Cuenta regresiva ─────────────────────────────────────
  private evaluarCuentaRegresiva(): void {
    if (this.intervaloPreturno) {
      clearInterval(this.intervaloPreturno);
      this.intervaloPreturno = null;
    }
    this.cuentaRegresivaSegundos = -1;
    if (!this.turnoActivo || this.turnoActivo.estado !== EstadoTurno.PENDIENTE) return;

    const tick = () => {
      if (!this.turnoActivo || this.turnoActivo.estado !== EstadoTurno.PENDIENTE) {
        if (this.intervaloPreturno) clearInterval(this.intervaloPreturno);
        this.intervaloPreturno = null;
        return;
      }
      const secs = this.calcularSegundosHastaInicio(this.turnoActivo.horaInicio);
      this.cuentaRegresivaSegundos = Math.max(0, secs);
      if (secs <= 0) {
        // M1: transición a EN_CURSO en UI sin recargar
        this.turnoActivo = { ...this.turnoActivo, estado: EstadoTurno.EN_CURSO };
        if (this.intervaloPreturno) clearInterval(this.intervaloPreturno);
        this.intervaloPreturno = null;
      }
    };

    tick();
    this.intervaloPreturno = setInterval(tick, 1000);
  }

  private calcularSegundosHastaInicio(horaInicio: string): number {
    const partes = horaInicio.split(':').map(Number);
    const ahora = new Date();
    const inicio = new Date(ahora);
    inicio.setHours(partes[0], partes[1] ?? 0, partes[2] ?? 0, 0);
    return Math.floor((inicio.getTime() - ahora.getTime()) / 1000);
  }

  // ── M2: Check-in 1 tap ───────────────────────────────────────
  abrirCheckin(): void {
    this.pinInput = '';
    const saved = localStorage.getItem(this.CHECKIN_METHOD_KEY) as MetodoRegistro | null;
    if (saved && (Object.values(MetodoRegistro) as string[]).includes(saved)) {
      this.metodoSeleccionado = saved;
      this.checkinPaso = 2;
    } else {
      this.metodoSeleccionado = null;
      this.checkinPaso = 1;
    }
    this.showCheckinModal = true;
  }

  seleccionarMetodo(m: MetodoRegistro): void {
    this.metodoSeleccionado = m;
    localStorage.setItem(this.CHECKIN_METHOD_KEY, m);
    this.checkinPaso = 2;
  }

  cambiarMetodo(): void {
    this.checkinPaso = 1;
    this.metodoSeleccionado = null;
  }

  // F4: Abre el modal de check-in con QR preseleccionado para registrar una ronda adicional
  abrirRonda(): void {
    this.pinInput = '';
    this.metodoSeleccionado = MetodoRegistro.QR;
    this.checkinPaso = 2;
    this.showCheckinModal = true;
  }

  // ── M6: Hora real del servidor ───────────────────────────────
  confirmarCheckin(): void {
    if (!this.turnoActivo || !this.usuario || !this.metodoSeleccionado) return;

    const enviarCheckin = (fechaHora: string) => {
      const registro: Partial<RegistroVigilancia> = {
        turno:            { id: this.turnoActivo!.id } as Turno,
        docente:          { id: this.usuario!.id } as Docente,
        zona:             { id: this.turnoActivo!.zona.id } as any,
        fechaHoraCheckIn: fechaHora,
        metodoRegistro:   this.metodoSeleccionado!,
        recorridoRealizado: false
      };

      this.api.post<RegistroVigilancia>('registros', registro).subscribe({
        next: () => {
          this.showCheckinModal = false;
          this.checkinSuccess = true;  // M3
          // F4: actualizar estado de check-in inmediatamente sin esperar reload
          this.checkinRealizado = true;
          this.checkinHora = new Date().toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' });
          this.rondasHoy++;
          this.toast.success(`Check-in registrado a las ${this.checkinHora}`);

          setTimeout(() => {
            this.checkinSuccess = false;
            if (this.turnoActivo?.id) {
              this.api.patchCustom(`turnos/${this.turnoActivo.id}/estado?estado=EN_CURSO`, {})
                .subscribe(() => {
                  this.cargarDatos();
                  this.verificarGamificacion();  // M7
                });
            } else {
              this.cargarDatos();
              this.verificarGamificacion();
            }
          }, 2500);
        }
      });
    };

    if (this.horaServidor) {
      enviarCheckin(`${this.fechaServidor}T${this.horaServidor}`);
    } else {
      this.api.getCustom<{ fecha: string; hora: string }>('servidor/fecha-hora').subscribe({
        next:  srv  => enviarCheckin(`${srv.fecha}T${srv.hora}`),
        error: ()   => enviarCheckin(new Date().toISOString())
      });
    }
  }

  // ── M5: Incidente 2 pasos ────────────────────────────────────
  abrirIncidente(): void {
    this.incidentePaso = 1;
    this.tipoIncidente = null;
    this.severidadIncidente = null;
    this.descripcionIncidente = '';
    this.observacionEstudiante = '';
    this.showIncidenteModal = true;
  }

  seleccionarCombo(c: { tipo: TipoIncidente; severidad: SeveridadIncidente }): void {
    this.tipoIncidente = c.tipo;
    this.severidadIncidente = c.severidad;
    this.incidentePaso = 2;
  }

  confirmarIncidente(): void {
    if (!this.turnoActivo || !this.tipoIncidente || !this.severidadIncidente) return;

    const incidente = {
      turno:    { id: this.turnoActivo.id },
      zona:     { id: this.turnoActivo.zona.id },
      tipo:     this.tipoIncidente,
      severidad: this.severidadIncidente,
      descripcion: this.descripcionIncidente.trim() ||
        `${this.tipoIncidente} — ${this.severidadIncidente}`,
      fechaHora: this.horaServidor
        ? `${this.fechaServidor}T${this.horaServidor}`
        : new Date().toISOString(),
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

  // ── M7: Gamificación ─────────────────────────────────────────
  private verificarGamificacion(): void {
    if (this.turnosHoy.length === 0) return;
    const todosEnCurso = this.turnosHoy.every(t => t.estado === EstadoTurno.EN_CURSO);
    if (!todosEnCurso) return;

    const hoy = this.fechaServidor || new Date().toISOString().split('T')[0];
    if (localStorage.getItem(this.RACHA_FECHA_KEY) === hoy) return;

    this.rachaActual++;
    localStorage.setItem(this.RACHA_KEY, String(this.rachaActual));
    localStorage.setItem(this.RACHA_FECHA_KEY, hoy);
    this.showTodosCompletados = true;
    setTimeout(() => (this.showTodosCompletados = false), 4000);
  }

  // ── M9: Reasignación ─────────────────────────────────────────
  abrirReasignacion(): void {
    this.motivoReasignacion = '';
    this.showReasignacionModal = true;
  }

  confirmarReasignacion(): void {
    if (!this.turnoActivo || !this.usuario || !this.motivoReasignacion.trim()) return;

    const reas: Partial<Reasignacion> = {
      turno:              { id: this.turnoActivo.id } as Turno,
      docenteOriginal:    { id: this.usuario.id } as Docente,
      motivo:             this.motivoReasignacion,
      fechaHoraSolicitud: this.horaServidor
        ? `${this.fechaServidor}T${this.horaServidor}`
        : `${this.fechaServidor}T${new Date().toLocaleTimeString('en-GB')}`,
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
