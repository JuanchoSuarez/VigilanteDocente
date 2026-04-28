import { Turno } from './turno.model';
import { Docente } from './docente.model';

export enum EstadoReasignacion {
  PENDIENTE = 'PENDIENTE',
  ACEPTADA = 'ACEPTADA',
  RECHAZADA = 'RECHAZADA'
}

export interface Reasignacion {
  id?: number;
  turno: Turno;
  docenteOriginal: Docente;
  docenteReemplazo?: Docente;
  motivo: string;
  fechaHoraSolicitud: string;
  fechaHoraRespuesta?: string;
  estado: EstadoReasignacion;
}
