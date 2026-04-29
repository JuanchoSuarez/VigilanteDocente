import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Toast {
  id: number;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  public toasts$ = this.toastsSubject.asObservable();
  private idCounter = 0;

  success(message: string) { this.show(message, 'success'); }
  error(message: string) { this.show(message, 'error'); }
  warning(message: string) { this.show(message, 'warning'); }
  info(message: string) { this.show(message, 'info'); }

  private show(message: string, type: Toast['type']) {
    const id = ++this.idCounter;
    const toast: Toast = { id, message, type };
    
    const current = this.toastsSubject.value;
    if (current.length >= 3) {
      current.shift(); // Remove oldest to keep max 3
    }
    
    this.toastsSubject.next([...current, toast]);

    setTimeout(() => this.remove(id), 3500);
  }

  remove(id: number) {
    this.toastsSubject.next(this.toastsSubject.value.filter(t => t.id !== id));
  }
}
