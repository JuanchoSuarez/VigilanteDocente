import { Component, OnInit, OnDestroy } from '@angular/core';
import { SlicePipe } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ApiService } from '../../services/api.service';
import { RegistroVigilancia } from '../../models/registro-vigilancia.model';

@Component({
  selector: 'app-registros',
  standalone: true,
  imports: [SlicePipe],
  templateUrl: './registros.html',
  styleUrl: './registros.css'
})
export class RegistrosComponent implements OnInit, OnDestroy {
  registros: RegistroVigilancia[] = [];
  loading = false;
  private destroy$ = new Subject<void>();

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loading = true;
    this.api.getAll<RegistroVigilancia>('registros')
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: r => { this.registros = r; this.loading = false; },
        error: () => { this.loading = false; }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  metodoClass(m: string): string {
    if (m === 'QR') return 'badge bg-primary';
    if (m === 'NFC') return 'badge bg-info text-dark';
    return 'badge bg-secondary';
  }
}
