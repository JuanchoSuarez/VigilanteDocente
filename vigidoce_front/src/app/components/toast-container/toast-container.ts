import { Component } from '@angular/core';
import { AsyncPipe, NgClass } from '@angular/common';
import { ToastService, Toast } from '../../services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [AsyncPipe, NgClass],
  template: `
    <div class="toast-container">
      @for (toast of toastService.toasts$ | async; track toast.id) {
        <div class="toast-item" [ngClass]="'toast-' + toast.type">
          <i class="bi" [ngClass]="getIcon(toast.type)"></i>
          <span>{{ toast.message }}</span>
          <button class="toast-close" (click)="toastService.remove(toast.id)">
            <i class="bi bi-x-lg"></i>
          </button>
          <div class="toast-progress"></div>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      bottom: 24px;
      right: 24px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 10px;
      pointer-events: none;
    }
    .toast-item {
      pointer-events: auto;
      min-width: 250px;
      padding: 14px 16px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      gap: 12px;
      color: white;
      font-weight: 500;
      font-size: 0.95rem;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      position: relative;
      overflow: hidden;
      animation: slideInRight 0.3s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
    }
    @keyframes slideInRight {
      from { transform: translateX(120%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
    .toast-success { background: #2E7D32; }
    .toast-error { background: #C62828; }
    .toast-warning { background: #E65100; }
    .toast-info { background: #1565C0; }
    
    .toast-item i.bi { font-size: 1.25rem; }
    
    .toast-close {
      background: none;
      border: none;
      color: rgba(255,255,255,0.7);
      margin-left: auto;
      cursor: pointer;
      display: grid;
      place-items: center;
      padding: 0;
    }
    .toast-close:hover { color: white; }
    
    .toast-progress {
      position: absolute;
      bottom: 0;
      left: 0;
      height: 3px;
      background: rgba(255,255,255,0.4);
      animation: progress 3.5s linear forwards;
    }
    @keyframes progress {
      from { width: 100%; }
      to { width: 0%; }
    }
  `]
})
export class ToastContainerComponent {
  constructor(public toastService: ToastService) {}

  getIcon(type: string): string {
    switch (type) {
      case 'success': return 'bi-check-circle';
      case 'error': return 'bi-x-circle';
      case 'warning': return 'bi-exclamation-triangle';
      case 'info': return 'bi-info-circle';
      default: return 'bi-info-circle';
    }
  }
}
