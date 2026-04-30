import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { SlicePipe } from '@angular/common';
import { forkJoin, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ApiService } from '../../services/api.service';
import { CatalogosService } from '../../services/catalogos.service';
import { ToastService } from '../../services/toast.service';
import { Incidente, TipoIncidente, SeveridadIncidente } from '../../models/incidente.model';
import { Turno } from '../../models/turno.model';
import { Zona } from '../../models/zona.model';
import { SeveridadIncidentePipe } from '../../pipes/severidad.pipe';

@Component({
  selector: 'app-incidentes',
  standalone: true,
  imports: [ReactiveFormsModule, SlicePipe, SeveridadIncidentePipe],
  templateUrl: './incidentes.html',
  styleUrl: './incidentes.css'
})
export class IncidentesComponent implements OnInit, OnDestroy {
  incidentes: Incidente[] = [];
  turnos: Turno[] = [];
  zonas: Zona[] = [];
  loading = false;
  showModal = false;
  editId: number | null = null;
  form: FormGroup;
  tipos = Object.values(TipoIncidente);
  severidades = Object.values(SeveridadIncidente);
  filtroSeveridad = '';

  private destroy$ = new Subject<void>();

  constructor(
    private api: ApiService, 
    private fb: FormBuilder,
    private catalogos: CatalogosService,
    private toast: ToastService
  ) {
    this.form = this.fb.group({
      turno: [null, Validators.required],
      zona: [null, Validators.required],
      tipo: [TipoIncidente.CONVIVENCIA, Validators.required],
      severidad: [SeveridadIncidente.S1_LEVE, Validators.required],
      descripcion: ['', Validators.required],
      fechaHora: ['', Validators.required],
      observacionEstudiante: ['']
    });
  }

  ngOnInit(): void {
    this.cargar();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargar(): void {
    this.loading = true;
    forkJoin({
      incidentes: this.api.getAll<Incidente>('incidentes'),
      turnos: this.api.getAll<Turno>('turnos'),
      zonas: this.catalogos.getZonas()
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: (data) => {
        this.incidentes = data.incidentes;
        this.turnos = data.turnos;
        this.zonas = data.zonas;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  get incidentesFiltrados(): Incidente[] {
    if (!this.filtroSeveridad) return this.incidentes;
    return this.incidentes.filter(i => i.severidad === this.filtroSeveridad);
  }

  abrirCrear(): void {
    this.editId = null;
    this.form.reset({ tipo: TipoIncidente.CONVIVENCIA, severidad: SeveridadIncidente.S1_LEVE });
    this.showModal = true;
  }

  abrirEditar(i: Incidente): void {
    this.editId = i.id!;
    this.form.patchValue({ ...i, turno: i.turno?.id, zona: i.zona?.id, fechaHora: i.fechaHora?.substring(0, 16) });
    this.showModal = true;
  }

  guardar(): void {
    if (this.form.invalid) return;
    const val = this.form.value;
    const payload = { ...val, turno: { id: +val.turno }, zona: { id: +val.zona } };

    if (this.editId) {
      this.api.put<Incidente>('incidentes', this.editId, payload).subscribe(() => {
        this.showModal = false; 
        this.toast.success('Incidente actualizado');
        this.cargar();
      });
    } else {
      this.api.post<Incidente>('incidentes', payload).subscribe(() => {
        this.showModal = false; 
        this.toast.success('Incidente creado');
        this.cargar();
      });
    }
  }

  eliminar(id: number): void {
    if (!confirm('¿Eliminar este incidente?')) return;
    this.api.delete('incidentes', id).subscribe(() => {
      this.toast.success('Incidente eliminado');
      this.cargar();
    });
  }
}
