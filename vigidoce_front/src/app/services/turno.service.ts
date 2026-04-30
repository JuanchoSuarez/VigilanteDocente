import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Turno, EstadoTurno } from '../models/turno.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TurnoService {
  constructor(private api: ApiService) {}

  getTurnosDocente(docenteId: number): Observable<Turno[]> {
    return this.api.getCustom<Turno[]>(`turnos/docente/${docenteId}`);
  }

  getTurnosDocenteFecha(docenteId: number, fecha: string): Observable<Turno[]> {
    return this.api.getCustom<Turno[]>(`turnos/docente/${docenteId}/fecha/${fecha}`);
  }

  getTurnosPorFecha(fecha: string): Observable<Turno[]> {
    return this.api.getCustom<Turno[]>(`turnos/fecha/${fecha}`);
  }

  getTurnosHoy(): Observable<Turno[]> {
    const hoy = new Date().toISOString().split('T')[0];
    return this.getTurnosPorFecha(hoy);
  }

  cambiarEstado(id: number, estado: EstadoTurno): Observable<Turno> {
    return this.api.patchCustom<Turno>(`turnos/${id}/estado?estado=${estado}`);
  }
}
