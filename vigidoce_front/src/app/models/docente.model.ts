export enum RolDocente {
  DOCENTE = 'DOCENTE',
  COORDINADOR = 'COORDINADOR',
  ADMINISTRADOR = 'ADMINISTRADOR'
}

export interface Docente {
  id?: number;
  nombre: string;
  apellido: string;
  email: string;
  password?: string;
  telefono: string;
  rol: RolDocente;
  activo: boolean;
}
