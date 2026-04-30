export enum TipoZona {
  PATIO = 'PATIO',
  CAFETERIA = 'CAFETERIA',
  CORREDOR = 'CORREDOR',
  ENTRADA = 'ENTRADA',
  AULA = 'AULA',
  OTRO = 'OTRO'
}

export interface Zona {
  id?: number;
  nombre: string;
  descripcion: string;
  tipo: TipoZona;
  capacidadMaxima: number;
  activa: boolean;
  posX?: number;
  posY?: number;
  posWidth?: number;
  posHeight?: number;
  imagenMapaUrl?: string;
}
