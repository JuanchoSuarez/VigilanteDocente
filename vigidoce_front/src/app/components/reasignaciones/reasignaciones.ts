import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SlicePipe } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { Reasignacion, EstadoReasignacion } from '../../models/reasignacion.model';
import { Docente } from '../../models/docente.model';

@Component({
  selector: 'app-reasignaciones',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './reasignaciones.html',
  styleUrl: './reasignaciones.css'
})
export class ReasignacionesComponent implements OnInit {
  reasignaciones: Reasignacion[] = [];
  docentes: Docente[] = [];
  loading = false;
  showAceptarModal = false;
  reasignacionSeleccionada: Reasignacion | null = null;
  docenteReemplazoId: number | null = null;

  constructor(private api: ApiService) { }

  ngOnInit(): void {
    this.cargar();
    this.api.getAll<Docente>('docentes').subscribe(d => this.docentes = d.filter(x => x.activo));
  }

  cargar(): void {
    this.loading = true;
    this.api.getAll<Reasignacion>('reasignaciones').subscribe({
      next: r => { this.reasignaciones = r; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  abrirAceptar(r: Reasignacion): void {
    this.reasignacionSeleccionada = r;
    this.docenteReemplazoId = null;
    this.showAceptarModal = true;
  }

  confirmarAceptar(): void {
    if (!this.reasignacionSeleccionada || !this.docenteReemplazoId) return;
    this.api.patchCustom(`reasignaciones/${this.reasignacionSeleccionada.id}/aceptar`, {
      docenteReemplazoId: this.docenteReemplazoId
    }).subscribe(() => { this.showAceptarModal = false; this.cargar(); });
  }

  rechazar(id: number): void {
    if (!confirm('¿Rechazar esta reasignación?')) return;
    this.api.patchCustom(`reasignaciones/${id}/rechazar`).subscribe(() => this.cargar());
  }

  estadoClass(estado: EstadoReasignacion): string {
    if (estado === EstadoReasignacion.PENDIENTE) return 'badge bg-warning text-dark';
    if (estado === EstadoReasignacion.ACEPTADA) return 'badge bg-success';
    return 'badge bg-danger';
  }
}