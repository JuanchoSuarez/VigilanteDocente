import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { Docente } from '../../models/docente.model';
import { Zona } from '../../models/zona.model';
import { Turno } from '../../models/turno.model';
import { Incidente } from '../../models/incidente.model';

@Component({
  selector: 'app-dashboard-admin',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard-admin.html',
  styleUrl: './dashboard-admin.css'
})
export class DashboardAdminComponent implements OnInit {
  totalDocentes = 0;
  totalZonas = 0;
  turnosHoy = 0;
  incidentesMes = 0;
  loading = false;

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loading = true;
    const hoy = new Date().toISOString().split('T')[0];
    const mesActual = new Date().toISOString().substring(0, 7);

    this.api.getAll<Docente>('docentes').subscribe(d => {
      this.totalDocentes = d.filter(x => x.activo).length;
    });

    this.api.getAll<Zona>('zonas').subscribe(z => {
      this.totalZonas = z.filter(x => x.activa).length;
    });

    this.api.getCustom<Turno[]>(`turnos/fecha/${hoy}`).subscribe(t => {
      this.turnosHoy = t.length;
      this.loading = false;
    });

    this.api.getAll<Incidente>('incidentes').subscribe(i => {
      this.incidentesMes = i.filter(x => x.fechaHora?.startsWith(mesActual)).length;
    });
  }
}
