import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { forkJoin, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { CatalogosService } from '../../services/catalogos.service';
import { ToastService } from '../../services/toast.service';
import { Turno, EstadoTurno, TipoFranja } from '../../models/turno.model';
import { Docente } from '../../models/docente.model';
import { Zona } from '../../models/zona.model';
import { EstadoTurnoPipe } from '../../pipes/estado.pipe';

@Component({
  selector: 'app-turnos',
  standalone: true,
  imports: [ReactiveFormsModule, EstadoTurnoPipe],
  templateUrl: './turnos.html',
  styleUrl: './turnos.css'
})
export class TurnosComponent implements OnInit, OnDestroy {
  turnos: Turno[] = [];
  docentes: Docente[] = [];
  zonas: Zona[] = [];
  loading = false;
  showModal = false;
  editId: number | null = null;
  form: FormGroup;
  estados = Object.values(EstadoTurno);
  franjas = Object.values(TipoFranja);
  fechaHoy = new Date().toISOString().split('T')[0];

  /** Docente autenticado en la sesión actual */
  usuarioSesion: Docente | null = null;

  /** True cuando el usuario en sesión tiene rol DOCENTE */
  get esDocente(): boolean { return this.usuarioSesion?.rol === 'DOCENTE'; }

  private destroy$ = new Subject<void>();

  constructor(
    private api: ApiService,
    private fb: FormBuilder,
    private catalogos: CatalogosService,
    private toast: ToastService,
    private auth: AuthService
  ) {
    this.form = this.fb.group({
      docente: [null, Validators.required],
      zona: [null, Validators.required],
      fecha: ['', Validators.required],
      horaInicio: ['', Validators.required],
      horaFin: ['', Validators.required],
      estado: [EstadoTurno.PENDIENTE, Validators.required],
      tipoFranja: [TipoFranja.RECREO, Validators.required]
    });
  }

  // Inicializa la sesión del usuario y carga los datos al montar el componente
  ngOnInit(): void {
    this.usuarioSesion = this.auth.getUsuarioActual();
    this.cargar();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargar(): void {
    this.loading = true;
    forkJoin({
      turnos: this.api.getAll<Turno>('turnos'),
      docentes: this.catalogos.getDocentes(),
      zonas: this.catalogos.getZonas()
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: (data) => {
        this.turnos = data.turnos;
        this.docentes = data.docentes.filter(x => x.activo);
        this.zonas = data.zonas.filter(x => x.activa);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  // Abre el modal en modo creación; si el usuario es DOCENTE, pre-rellena su propio ID
  abrirCrear(): void {
    this.editId = null;
    this.form.reset({
      estado: EstadoTurno.PENDIENTE,
      tipoFranja: TipoFranja.RECREO,
      fecha: this.fechaHoy,
      docente: this.esDocente ? this.usuarioSesion?.id : null
    });
    this.showModal = true;
  }

  abrirEditar(t: Turno): void {
    this.editId = t.id!;
    this.form.patchValue({
      ...t,
      docente: t.docente?.id,
      zona: t.zona?.id
    });
    this.showModal = true;
  }

  guardar(): void {
    if (this.form.invalid) return;
    const val = this.form.value;
    const payload = {
      ...val,
      docente: { id: +val.docente },
      zona: { id: +val.zona }
    };

    if (this.editId) {
      this.api.put<Turno>('turnos', this.editId, payload).subscribe(() => {
        this.showModal = false; 
        this.toast.success('Turno editado');
        this.cargar();
      });
    } else {
      this.api.post<Turno>('turnos', payload).subscribe(() => {
        this.showModal = false; 
        this.toast.success('Turno creado');
        this.cargar();
      });
    }
  }

  eliminar(id: number): void {
    if (!confirm('¿Eliminar este turno?')) return;
    this.api.delete('turnos', id).subscribe(() => {
      this.toast.success('Turno eliminado');
      this.cargar();
    });
  }
}