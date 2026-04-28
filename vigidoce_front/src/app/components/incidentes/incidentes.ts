import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { SlicePipe } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { Incidente, TipoIncidente, SeveridadIncidente } from '../../models/incidente.model';
import { Turno } from '../../models/turno.model';
import { Zona } from '../../models/zona.model';

@Component({
  selector: 'app-incidentes',
  standalone: true,
  imports: [ReactiveFormsModule, SlicePipe],
  templateUrl: './incidentes.html',
  styleUrl: './incidentes.css'
})
export class IncidentesComponent implements OnInit {
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

  constructor(private api: ApiService, private fb: FormBuilder) {
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
    this.api.getAll<Turno>('turnos').subscribe(t => this.turnos = t);
    this.api.getAll<Zona>('zonas').subscribe(z => this.zonas = z);
  }

  cargar(): void {
    this.loading = true;
    this.api.getAll<Incidente>('incidentes').subscribe({
      next: i => { this.incidentes = i; this.loading = false; },
      error: () => { this.loading = false; }
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
        this.showModal = false; this.cargar();
      });
    } else {
      this.api.post<Incidente>('incidentes', payload).subscribe(() => {
        this.showModal = false; this.cargar();
      });
    }
  }

  eliminar(id: number): void {
    if (!confirm('¿Eliminar este incidente?')) return;
    this.api.delete('incidentes', id).subscribe(() => this.cargar());
  }

  severidadClass(sev: string): string {
    if (sev === 'S3_ATENCION_INMEDIATA') return 'badge bg-danger';
    if (sev === 'S2_SEGUIMIENTO') return 'badge bg-warning text-dark';
    return 'badge bg-success';
  }
}
