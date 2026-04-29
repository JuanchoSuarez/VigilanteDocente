import { Component, OnInit, AfterViewInit } from '@angular/core';
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
export class DashboardAdminComponent implements OnInit, AfterViewInit {
  totalDocentes = 0;
  totalZonas = 0;
  turnosHoy = 0;
  incidentesMes = 0;
  loading = false;

  constructor(private api: ApiService) { }

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

  ngAfterViewInit(): void {
    // Inicializar editor canvas después de que la vista esté lista
    setTimeout(() => this.initMapaEditor(), 300);
  }

  private initMapaEditor(): void {
    const canvas = document.getElementById('mapaCanvas') as HTMLCanvasElement;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const hint = document.getElementById('canvasHint') as HTMLElement;
    const zonaPanel = document.getElementById('zonaPanel') as HTMLElement;
    const zonasGuardadas = document.getElementById('zonasGuardadas') as HTMLElement;
    const btnSelectImg = document.getElementById('btnSelectImg') as HTMLButtonElement;
    const btnDelete = document.getElementById('btnDelete') as HTMLButtonElement;
    const btnClear = document.getElementById('btnClear') as HTMLButtonElement;
    const imgInput = document.getElementById('imgInput') as HTMLInputElement;
    const zonaNombre = document.getElementById('zonaNombre') as HTMLInputElement;
    const zonaArea = document.getElementById('zonaArea') as HTMLInputElement;
    const zonaHeat = document.getElementById('zonaHeat') as HTMLInputElement;
    const heatPreview = document.getElementById('heatPreview') as HTMLDivElement;
    const btnGuardar = document.getElementById('btnGuardarZona') as HTMLButtonElement;

    // Estado
    let bgImage: HTMLImageElement | null = null;
    let isDrawing = false;
    let startX = 0, startY = 0;
    let currentRect = { x: 0, y: 0, w: 0, h: 0 };
    let SCALE_X = 1, SCALE_Y = 1;

    interface ZonaRect { id: number; x: number; y: number; w: number; h: number; nombre: string; area: number; heat: number; color: string; }
    const zonas: ZonaRect[] = [];
    let selectedId: number | null = null;
    let nextId = 1;

    // Resize canvas to real pixels
    const resizeCanvas = () => {
      const rect = canvas.getBoundingClientRect();
      canvas.width = rect.width * window.devicePixelRatio;
      canvas.height = rect.height * window.devicePixelRatio;
      ctx.scale(window.devicePixelRatio, window.devicePixelRatio);
      SCALE_X = rect.width / (bgImage?.width || rect.width);
      SCALE_Y = rect.height / (bgImage?.height || rect.height);
      redraw();
    };

    const heatToColor = (heat: number, alpha = 0.4): string => {
      if (heat < 33) return `rgba(0,200,80,${alpha})`;
      if (heat < 66) return `rgba(255,180,0,${alpha})`;
      return `rgba(255,60,60,${alpha})`;
    };

    const redraw = () => {
      const w = canvas.getBoundingClientRect().width;
      const h = canvas.getBoundingClientRect().height;
      ctx.clearRect(0, 0, w, h);

      if (bgImage) {
        ctx.drawImage(bgImage, 0, 0, w, h);
      }

      zonas.forEach(z => {
        ctx.fillStyle = heatToColor(z.heat, 0.35);
        ctx.fillRect(z.x, z.y, z.w, z.h);
        ctx.strokeStyle = selectedId === z.id ? '#818CF8' : heatToColor(z.heat, 0.9);
        ctx.lineWidth = selectedId === z.id ? 2 : 1;
        ctx.strokeRect(z.x, z.y, z.w, z.h);
        // Label
        ctx.fillStyle = 'rgba(255,255,255,0.9)';
        ctx.font = '11px sans-serif';
        ctx.fillText(z.nombre || `Zona ${z.id}`, z.x + 6, z.y + 16);
      });

      if (isDrawing) {
        ctx.strokeStyle = '#818CF8';
        ctx.lineWidth = 2;
        ctx.setLineDash([5, 3]);
        ctx.strokeRect(currentRect.x, currentRect.y, currentRect.w, currentRect.h);
        ctx.setLineDash([]);
        ctx.fillStyle = 'rgba(129,140,248,0.1)';
        ctx.fillRect(currentRect.x, currentRect.y, currentRect.w, currentRect.h);
      }
    };

    const getPos = (e: MouseEvent | TouchEvent) => {
      const rect = canvas.getBoundingClientRect();
      const src = 'touches' in e ? (e as TouchEvent).touches[0] : (e as MouseEvent);
      return { x: src.clientX - rect.left, y: src.clientY - rect.top };
    };

    const mts2 = (z: ZonaRect): number => {
      if (!bgImage) return Math.round(Math.abs(z.w * z.h) / 100);
      const cRect = canvas.getBoundingClientRect();
      const px2m = 0.01; // calibration placeholder
      return Math.round(Math.abs(z.w * z.h) * px2m);
    };

    canvas.addEventListener('mousedown', (e: MouseEvent) => {
      const p = getPos(e);
      // Check click on existing zona
      const hit = zonas.find(z => p.x >= z.x && p.x <= z.x + z.w && p.y >= z.y && p.y <= z.y + z.h);
      if (hit) {
        selectedId = hit.id;
        zonaPanel.style.display = 'flex';
        zonaNombre.value = hit.nombre;
        zonaArea.value = String(hit.area);
        zonaHeat.value = String(hit.heat);
        heatPreview.style.background = heatToColor(hit.heat, 0.7);
        redraw();
        return;
      }
      isDrawing = true;
      startX = p.x; startY = p.y;
      currentRect = { x: startX, y: startY, w: 0, h: 0 };
      hint.style.display = 'none';
    });

    canvas.addEventListener('mousemove', (e: MouseEvent) => {
      if (!isDrawing) return;
      const p = getPos(e);
      currentRect = { x: startX, y: startY, w: p.x - startX, h: p.y - startY };
      redraw();
    });

    canvas.addEventListener('mouseup', () => {
      if (!isDrawing) return;
      isDrawing = false;
      if (Math.abs(currentRect.w) > 10 && Math.abs(currentRect.h) > 10) {
        const newZona: ZonaRect = {
          id: nextId++,
          x: currentRect.w < 0 ? currentRect.x + currentRect.w : currentRect.x,
          y: currentRect.h < 0 ? currentRect.y + currentRect.h : currentRect.y,
          w: Math.abs(currentRect.w),
          h: Math.abs(currentRect.h),
          nombre: '',
          area: 0,
          heat: 0,
          color: heatToColor(0)
        };
        newZona.area = mts2(newZona);
        zonas.push(newZona);
        selectedId = newZona.id;
        zonaPanel.style.display = 'flex';
        zonaNombre.value = '';
        zonaArea.value = String(newZona.area);
        zonaHeat.value = '0';
        heatPreview.style.background = heatToColor(0, 0.7);
      }
      redraw();
    });

    // Heat slider
    zonaHeat.addEventListener('input', () => {
      heatPreview.style.background = heatToColor(Number(zonaHeat.value), 0.7);
      const z = zonas.find(z => z.id === selectedId);
      if (z) { z.heat = Number(zonaHeat.value); redraw(); }
    });

    // Guardar zona
    btnGuardar.addEventListener('click', () => {
      const z = zonas.find(z => z.id === selectedId);
      if (z) {
        z.nombre = zonaNombre.value || `Zona ${z.id}`;
        z.heat = Number(zonaHeat.value);
        z.area = Number(zonaArea.value);
        renderZonasGuardadas();
        redraw();
      }
    });

    // Eliminar
    btnDelete.addEventListener('click', () => {
      if (selectedId === null) return;
      const idx = zonas.findIndex(z => z.id === selectedId);
      if (idx !== -1) { zonas.splice(idx, 1); selectedId = null; zonaPanel.style.display = 'none'; }
      renderZonasGuardadas();
      redraw();
    });

    // Limpiar todo
    btnClear.addEventListener('click', () => {
      zonas.length = 0;
      selectedId = null;
      zonaPanel.style.display = 'none';
      if (zonas.length === 0) hint.style.display = 'flex';
      renderZonasGuardadas();
      redraw();
    });

    // Cargar imagen
    btnSelectImg.addEventListener('click', () => imgInput.click());
    imgInput.addEventListener('change', () => {
      const file = imgInput.files?.[0];
      if (!file) return;
      const url = URL.createObjectURL(file);
      const img = new Image();
      img.onload = () => {
        bgImage = img;
        hint.style.display = 'none';
        resizeCanvas();
      };
      img.src = url;
    });

    const renderZonasGuardadas = () => {
      zonasGuardadas.innerHTML = zonas.map(z => `
        <div class="zona-item">
          <div class="zona-item-dot" style="background:${heatToColor(z.heat, 0.9)}"></div>
          <span class="zona-item-name">${z.nombre || `Zona ${z.id}`}</span>
          <span class="zona-item-area">${z.area} m²</span>
        </div>
      `).join('');
    };

    window.addEventListener('resize', resizeCanvas);
    resizeCanvas();
  }
}