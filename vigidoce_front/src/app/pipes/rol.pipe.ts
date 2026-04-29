import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'rolDocente',
  standalone: true
})
export class RolDocentePipe implements PipeTransform {
  transform(rol: string | undefined | null, type: 'class' | 'label' = 'label'): string {
    if (!rol) return '';
    
    if (type === 'class') {
      const classMap: Record<string, string> = {
        DOCENTE: 'badge-docente',
        COORDINADOR: 'badge-coordinador',
        ADMINISTRADOR: 'badge-admin'
      };
      return classMap[rol] ?? 'badge bg-secondary';
    }
    
    return rol;
  }
}
