import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SlicePipe } from '@angular/common';
import { forkJoin, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ApiService } from '../../services/api.service';
import { CatalogosService } from '../../services/catalogos.service';
import { ToastService } from '../../services/toast.service';
import { Reasignacion, EstadoReasignacion } from '../../models/reasignacion.model';
import { Docente } from '../../models/docente.model';

@Component({
  selector: 'app-reasignaciones',
  standalone: true,
  imports: [FormsModule, SlicePipe],
  templateUrl: './reasignaciones.html',
  styleUrl: './reasignaciones.css'
})
export class ReasignacionesComponent implements OnInit, OnDestroy {
  reasignaciones: Reasignacion[] = [];
  docentes: Docente[] = [];
  loading = false;
  showAceptarModal = false;
  reasignacionSeleccionada: Reasignacion | null = null;
  docenteReemplazoId: number | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private api: ApiService,
    private catalogos: CatalogosService,
    private toast: ToastService
  ) { }

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
      reasignaciones: this.api.getAll<Reasignacion>('reasignaciones'),
      docentes: this.catalogos.getDocentes()
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: (data) => {
        this.reasignaciones = data.reasignaciones;
        this.docentes = data.docentes.filter(x => x.activo);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  abrirAceptar(r: Reasignacion): void {
    this.reasignacionSeleccionada = r;
    this.docenteReemplazoId = null;
    this.showAceptarModal = true;
  }

  confirmarAceptar(): void {
    if (!this.reasignacionSeleccionada || !this.docenteReemplazoId) return;
    this.api.patchCustom(`reasignaciones/${this.reasignacionSeleccionada.id}/aceptar?docenteReemplazoId=${this.docenteReemplazoId}`).subscribe(() => { 
      this.showAceptarModal = false; 
      this.toast.success('Reasignación aceptada');
      this.cargar(); 
    });
  }

  rechazar(id: number): void {
    if (!confirm('¿Rechazar esta reasignación?')) return;
    this.api.patchCustom(`reasignaciones/${id}/rechazar`).subscribe(() => {
      this.toast.success('Reasignación rechazada');
      this.cargar();
    });
  }

  estadoClass(estado: EstadoReasignacion): string {
    if (estado === EstadoReasignacion.PENDIENTE) return 'badge bg-warning text-dark';
    if (estado === EstadoReasignacion.ACEPTADA) return 'badge bg-success';
    return 'badge bg-danger';
  }
}