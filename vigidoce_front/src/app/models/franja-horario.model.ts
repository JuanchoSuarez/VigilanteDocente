export enum DiaSemana {
  LUNES = 'LUNES',
  MARTES = 'MARTES',
  MIERCOLES = 'MIERCOLES',
  JUEVES = 'JUEVES',
  VIERNES = 'VIERNES',
  SABADO = 'SABADO',
  DOMINGO = 'DOMINGO'
}

export interface FranjaHorario {
  id?: number;
  horario?: any;
  zona?: any;
  docente?: any;
  diaSemana: DiaSemana;
  horaInicio: string;
  horaFin: string;
  tipoFranja: string;
  activo: boolean;
}
