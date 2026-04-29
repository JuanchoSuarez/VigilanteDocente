import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'severidadIncidente',
  standalone: true
})
export class SeveridadIncidentePipe implements PipeTransform {
  transform(severidad: string | undefined | null, type: 'class' | 'label' = 'label'): string {
    if (!severidad) return '';
    
    if (type === 'class') {
      const classMap: Record<string, string> = {
        S1_LEVE: 'badge bg-success',
        S2_SEGUIMIENTO: 'badge bg-warning text-dark',
        S3_ATENCION_INMEDIATA: 'badge bg-danger'
      };
      return classMap[severidad] ?? 'badge bg-secondary';
    }
    
    return severidad.replace(/_/g, ' ');
  }
}
