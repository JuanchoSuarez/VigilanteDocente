import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { Docente } from '../models/docente.model';
import { Zona } from '../models/zona.model';

@Injectable({
  providedIn: 'root'
})
export class CatalogosService {
  private docentesSubject = new BehaviorSubject<Docente[] | null>(null);
  private zonasSubject = new BehaviorSubject<Zona[] | null>(null);

  constructor(private api: ApiService) {}

  getDocentes(): Observable<Docente[]> {
    if (this.docentesSubject.value) {
      return of(this.docentesSubject.value);
    }
    return this.api.getAll<Docente>('docentes').pipe(
      tap(docentes => this.docentesSubject.next(docentes))
    );
  }

  getZonas(): Observable<Zona[]> {
    if (this.zonasSubject.value) {
      return of(this.zonasSubject.value);
    }
    return this.api.getAll<Zona>('zonas').pipe(
      tap(zonas => this.zonasSubject.next(zonas))
    );
  }

  invalidarDocentes(): void {
    this.docentesSubject.next(null);
  }

  invalidarZonas(): void {
    this.zonasSubject.next(null);
  }
}
