import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAll<T>(resource: string): Observable<T[]> {
    return this.http.get<T[]>(`${this.base}/${resource}`);
  }

  getById<T>(resource: string, id: number): Observable<T> {
    return this.http.get<T>(`${this.base}/${resource}/${id}`);
  }

  post<T>(resource: string, body: unknown): Observable<T> {
    return this.http.post<T>(`${this.base}/${resource}`, body);
  }

  put<T>(resource: string, id: number, body: unknown): Observable<T> {
    return this.http.put<T>(`${this.base}/${resource}/${id}`, body);
  }

  delete(resource: string, id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${resource}/${id}`);
  }

  getCustom<T>(path: string): Observable<T> {
    return this.http.get<T>(`${this.base}/${path}`);
  }

  postCustom<T>(path: string, body: unknown): Observable<T> {
    return this.http.post<T>(`${this.base}/${path}`, body);
  }

  patchCustom<T>(path: string, body?: unknown): Observable<T> {
    return this.http.patch<T>(`${this.base}/${path}`, body ?? {});
  }
}
