import { Component, OnInit } from '@angular/core';
import { SlicePipe } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { RegistroVigilancia } from '../../models/registro-vigilancia.model';

@Component({
  selector: 'app-registros',
  standalone: true,
  imports: [SlicePipe],
  templateUrl: './registros.html',
  styleUrl: './registros.css'
})
export class RegistrosComponent implements OnInit {
  registros: RegistroVigilancia[] = [];
  loading = false;

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loading = true;
    this.api.getAll<RegistroVigilancia>('registros').subscribe({
      next: r => { this.registros = r; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  metodoClass(m: string): string {
    if (m === 'QR') return 'badge bg-primary';
    if (m === 'NFC') return 'badge bg-info text-dark';
    return 'badge bg-secondary';
  }
}
