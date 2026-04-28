import { Turno } from './turno.model';
import { Docente } from './docente.model';
import { Zona } from './zona.model';

export enum MetodoRegistro {
  QR = 'QR',
  PIN = 'PIN',
  NFC = 'NFC'
}

export interface RegistroVigilancia {
  id?: number;
  turno: Turno;
  docente: Docente;
  zona: Zona;
  fechaHoraCheckIn: string;
  fechaHoraCheckOut?: string;
  metodoRegistro: MetodoRegistro;
  recorridoRealizado: boolean;
  calificacionLimpieza?: number;
}
