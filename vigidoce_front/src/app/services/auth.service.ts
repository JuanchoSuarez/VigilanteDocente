import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap, map } from 'rxjs/operators';
import { Docente } from '../models/docente.model';
import { environment } from '../../environments/environment';

interface LoginResponse {
  token: string;
  docente: Docente;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly userKey  = 'usuarioActual';
  private readonly tokenKey = 'authToken';

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<Docente> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, { email, password }).pipe(
      tap(res => {
        this.setToken(res.token);
        this.setUsuarioActual(res.docente);
      }),
      map(res => res.docente)
    );
  }

  logout(): void {
    localStorage.removeItem(this.userKey);
    localStorage.removeItem(this.tokenKey);
  }

  getUsuarioActual(): Docente | null {
    const data = localStorage.getItem(this.userKey);
    return data ? JSON.parse(data) : null;
  }

  setUsuarioActual(docente: Docente): void {
    localStorage.setItem(this.userKey, JSON.stringify(docente));
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  getRol(): string | null {
    return this.getUsuarioActual()?.rol ?? null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken() && !!this.getUsuarioActual();
  }
}