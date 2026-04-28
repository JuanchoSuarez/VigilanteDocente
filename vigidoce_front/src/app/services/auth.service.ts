import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Docente } from '../models/docente.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly storageKey = 'usuarioActual';

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<Docente> {
    return this.http.post<Docente>(`${environment.apiUrl}/auth/login`, { email, password }).pipe(
      tap(docente => this.setUsuarioActual(docente))
    );
  }

  logout(): void {
    localStorage.removeItem(this.storageKey);
  }

  getUsuarioActual(): Docente | null {
    const data = localStorage.getItem(this.storageKey);
    return data ? JSON.parse(data) : null;
  }

  setUsuarioActual(docente: Docente): void {
    localStorage.setItem(this.storageKey, JSON.stringify(docente));
  }

  getRol(): string | null {
    return this.getUsuarioActual()?.rol ?? null;
  }

  isLoggedIn(): boolean {
    return !!this.getUsuarioActual();
  }
}
