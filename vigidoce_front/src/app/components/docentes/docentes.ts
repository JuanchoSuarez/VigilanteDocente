import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ApiService } from '../../services/api.service';
import { CatalogosService } from '../../services/catalogos.service';
import { ToastService } from '../../services/toast.service';
import { Docente, RolDocente } from '../../models/docente.model';
import { RolDocentePipe } from '../../pipes/rol.pipe';

@Component({
  selector: 'app-docentes',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, RolDocentePipe],
  templateUrl: './docentes.html',
  styleUrl: './docentes.css'
})
export class DocentesComponent implements OnInit, OnDestroy {
  docentes: Docente[] = [];
  loading = false;
  showModal = false;
  editId: number | null = null;
  form: FormGroup;
  buscador = '';
  roles = Object.values(RolDocente);
  
  private destroy$ = new Subject<void>();

  constructor(
    private api: ApiService, 
    private fb: FormBuilder,
    private catalogos: CatalogosService,
    private toast: ToastService
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      apellido: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      telefono: ['', Validators.required],
      rol: [RolDocente.DOCENTE, Validators.required],
      activo: [true]
    });
  }

  ngOnInit(): void { this.cargar(); }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargar(): void {
    this.loading = true;
    this.catalogos.getDocentes().pipe(takeUntil(this.destroy$)).subscribe({
      next: d => { this.docentes = d; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  get docentesFiltrados(): Docente[] {
    const q = this.buscador.toLowerCase();
    if (!q) return this.docentes;
    return this.docentes.filter(d =>
      d.nombre.toLowerCase().includes(q) ||
      d.apellido.toLowerCase().includes(q) ||
      d.email.toLowerCase().includes(q)
    );
  }

  abrirCrear(): void {
    this.editId = null;
    this.form.reset({ rol: RolDocente.DOCENTE, activo: true });
    this.form.get('password')?.setValidators(Validators.required);
    this.form.get('password')?.updateValueAndValidity();
    this.showModal = true;
  }

  abrirEditar(d: Docente): void {
    this.editId = d.id!;
    this.form.patchValue({ ...d, password: '' });
    this.form.get('password')?.clearValidators();
    this.form.get('password')?.updateValueAndValidity();
    this.showModal = true;
  }

  guardar(): void {
    if (this.form.invalid) return;
    const data = { ...this.form.value };
    if (!data.password) delete data.password;

    if (this.editId) {
      this.api.put<Docente>('docentes', this.editId, data).subscribe(() => {
        this.showModal = false; 
        this.toast.success('Docente actualizado');
        this.catalogos.invalidarDocentes();
        this.cargar();
      });
    } else {
      this.api.post<Docente>('docentes', data).subscribe(() => {
        this.showModal = false; 
        this.toast.success('Docente creado');
        this.catalogos.invalidarDocentes();
        this.cargar();
      });
    }
  }

  eliminar(id: number): void {
    if (!confirm('¿Seguro que deseas eliminar este docente?')) return;
    this.api.delete('docentes', id).subscribe(() => {
      this.toast.success('Docente eliminado');
      this.catalogos.invalidarDocentes();
      this.cargar();
    });
  }
}
