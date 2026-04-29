import { Pipe, PipeTransform } from '@angular/core';
import { EstadoTurno } from '../models/turno.model';

@Pipe({
  name: 'estadoTurno',
  standalone: true
})
export class EstadoTurnoPipe implements PipeTransform {
  transform(estado: string | EstadoTurno | undefined | null, type: 'class' | 'label' = 'label'): string {
    if (!estado) return '';
    
    if (type === 'class') {
      const classMap: Record<string, string> = {
        EN_CURSO: 'badge bg-success',
        PENDIENTE: 'badge bg-warning text-dark',
        CERRADO: 'badge bg-secondary',
        AUSENTE: 'badge bg-danger'
      };
      return classMap[estado] ?? 'badge bg-secondary';
    }
    
    return estado.replace('_', ' ');
  }
}
