import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { Zona, TipoZona } from '../../models/zona.model';

@Component({
  selector: 'app-zonas',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './zonas.html',
  styleUrl: './zonas.css'
})
export class ZonasComponent implements OnInit {
  zonas: Zona[] = [];
  loading = false;
  showModal = false;
  editId: number | null = null;
  form: FormGroup;
  tipos = Object.values(TipoZona);

  tipoIcon: Record<string, string> = {
    PATIO: 'bi-tree', CAFETERIA: 'bi-cup-straw', CORREDOR: 'bi-arrows-expand',
    ENTRADA: 'bi-door-open', AULA: 'bi-building', OTRO: 'bi-geo'
  };

  constructor(private api: ApiService, private fb: FormBuilder) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      descripcion: ['', Validators.required],
      tipo: [TipoZona.PATIO, Validators.required],
      capacidadMaxima: [50, [Validators.required, Validators.min(1)]],
      activa: [true]
    });
  }

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.loading = true;
    this.api.getAll<Zona>('zonas').subscribe({
      next: z => { this.zonas = z; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  abrirCrear(): void {
    this.editId = null;
    this.form.reset({ tipo: TipoZona.PATIO, capacidadMaxima: 50, activa: true });
    this.showModal = true;
  }

  abrirEditar(z: Zona): void {
    this.editId = z.id!;
    this.form.patchValue(z);
    this.showModal = true;
  }

  guardar(): void {
    if (this.form.invalid) return;
    if (this.editId) {
      this.api.put<Zona>('zonas', this.editId, this.form.value).subscribe(() => {
        this.showModal = false; this.cargar();
      });
    } else {
      this.api.post<Zona>('zonas', this.form.value).subscribe(() => {
        this.showModal = false; this.cargar();
      });
    }
  }

  eliminar(id: number): void {
    if (!confirm('¿Eliminar esta zona?')) return;
    this.api.delete('zonas', id).subscribe(() => this.cargar());
  }
}
