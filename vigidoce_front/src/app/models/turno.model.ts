import { Docente } from './docente.model';
import { Zona } from './zona.model';

export enum EstadoTurno {
  PENDIENTE = 'PENDIENTE',
  EN_CURSO = 'EN_CURSO',
  CERRADO = 'CERRADO',
  AUSENTE = 'AUSENTE'
}

export enum TipoFranja {
  RECREO = 'RECREO',
  ALMUERZO = 'ALMUERZO'
}

export interface Turno {
  id?: number;
  docente: Docente;
  zona: Zona;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  estado: EstadoTurno;
  tipoFranja: TipoFranja;
}
