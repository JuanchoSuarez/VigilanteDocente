import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { Turno, EstadoTurno, TipoFranja } from '../../models/turno.model';
import { Docente } from '../../models/docente.model';
import { Zona } from '../../models/zona.model';

@Component({
  selector: 'app-turnos',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './turnos.html',
  styleUrl: './turnos.css'
})
export class TurnosComponent implements OnInit {
  turnos: Turno[] = [];
  docentes: Docente[] = [];
  zonas: Zona[] = [];
  loading = false;
  showModal = false;
  editId: number | null = null;
  form: FormGroup;
  estados = Object.values(EstadoTurno);
  franjas = Object.values(TipoFranja);

  constructor(private api: ApiService, private fb: FormBuilder) {
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

  ngOnInit(): void {
    this.cargar();
    this.api.getAll<Docente>('docentes').subscribe(d => this.docentes = d.filter(x => x.activo));
    this.api.getAll<Zona>('zonas').subscribe(z => this.zonas = z.filter(x => x.activa));
  }

  cargar(): void {
    this.loading = true;
    this.api.getAll<Turno>('turnos').subscribe({
      next: t => { this.turnos = t; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  abrirCrear(): void {
    this.editId = null;
    this.form.reset({ estado: EstadoTurno.PENDIENTE, tipoFranja: TipoFranja.RECREO });
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
        this.showModal = false; this.cargar();
      });
    } else {
      this.api.post<Turno>('turnos', payload).subscribe(() => {
        this.showModal = false; this.cargar();
      });
    }
  }

  eliminar(id: number): void {
    if (!confirm('¿Eliminar este turno?')) return;
    this.api.delete('turnos', id).subscribe(() => this.cargar());
  }

  estadoClass(estado: EstadoTurno): string {
    const map: Record<string, string> = {
      EN_CURSO: 'badge bg-success', PENDIENTE: 'badge bg-warning text-dark',
      CERRADO: 'badge bg-secondary', AUSENTE: 'badge bg-danger'
    };
    return map[estado] ?? 'badge bg-secondary';
  }
}
