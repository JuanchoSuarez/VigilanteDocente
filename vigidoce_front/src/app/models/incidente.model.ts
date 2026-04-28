import { Turno } from './turno.model';
import { Zona } from './zona.model';

export enum TipoIncidente {
  SEGURIDAD_FISICA = 'SEGURIDAD_FISICA',
  CONVIVENCIA = 'CONVIVENCIA',
  USO_ESPACIO = 'USO_ESPACIO',
  OBSERVACION_SOCIAL = 'OBSERVACION_SOCIAL'
}

export enum SeveridadIncidente {
  S1_LEVE = 'S1_LEVE',
  S2_SEGUIMIENTO = 'S2_SEGUIMIENTO',
  S3_ATENCION_INMEDIATA = 'S3_ATENCION_INMEDIATA'
}

export interface Incidente {
  id?: number;
  turno: Turno;
  zona: Zona;
  tipo: TipoIncidente;
  severidad: SeveridadIncidente;
  descripcion: string;
  fechaHora: string;
  observacionEstudiante?: string;
}
