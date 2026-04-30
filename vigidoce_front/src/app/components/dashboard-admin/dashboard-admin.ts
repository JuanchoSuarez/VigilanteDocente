import { Component, OnInit, AfterViewInit, OnDestroy, ViewChild, ElementRef, HostListener } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { forkJoin, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ApiService } from '../../services/api.service';
import { CatalogosService } from '../../services/catalogos.service';
import { ToastService } from '../../services/toast.service';
import { Docente } from '../../models/docente.model';
import { Zona, TipoZona } from '../../models/zona.model';
import { Turno } from '../../models/turno.model';
import { Incidente } from '../../models/incidente.model';

interface ZonaRect { id: number; x: number; y: number; w: number; h: number; nombre: string; area: number; heat: number; color: string; imagenMapaUrl?: string; }

@Component({
  selector: 'app-dashboard-admin',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './dashboard-admin.html',
  styleUrl: './dashboard-admin.css'
})
export class DashboardAdminComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('mapaCanvas') canvasRef!: ElementRef<HTMLCanvasElement>;
  
  totalDocentes = 0;
  totalZonas = 0;
  turnosHoy = 0;
  incidentesMes = 0;
  loading = false;
  generandoTurnos = false;

  // ── CRUD Zonas ───────────────────────────────────────────────
  zonasList: Zona[] = [];
  showZonaModal = false;
  zonaEditId: number | null = null;
  zonaForm: FormGroup;
  zonaError = '';
  zonaAEliminar: Zona | null = null;
  readonly tiposZona = Object.values(TipoZona);

  private destroy$ = new Subject<void>();

  // Canvas State
  private bgImage: HTMLImageElement | null = null;
  private isDrawing = false;
  private startX = 0;
  private startY = 0;
  private currentRect = { x: 0, y: 0, w: 0, h: 0 };
  private zonasRects: ZonaRect[] = [];
  private selectedId: number | null = null;
  private nextId = 1;

  constructor(
    private api: ApiService,
    private catalogos: CatalogosService,
    private toast: ToastService,
    private fb: FormBuilder
  ) {
    this.zonaForm = this.fb.group({
      nombre:         ['', Validators.required],
      descripcion:    [''],
      tipo:           [TipoZona.PATIO, Validators.required],
      capacidadMaxima: [50, [Validators.required, Validators.min(1)]],
      activa:         [true]
    });
  }

  ngOnInit(): void {
    this.loading = true;

    this.api.getCustom<{fecha: string, hora: string}>('servidor/fecha-hora').pipe(takeUntil(this.destroy$)).subscribe({
      next: (srv) => {
        const hoy = srv.fecha;
        const mesActual = hoy.substring(0, 7);
        this.cargarDatosConFecha(hoy, mesActual);
      },
      error: () => {
        const hoy = new Date().toISOString().split('T')[0];
        const mesActual = hoy.substring(0, 7);
        this.cargarDatosConFecha(hoy, mesActual);
      }
    });
  }

  private cargarDatosConFecha(hoy: string, mesActual: string): void {
    forkJoin({
      docentes: this.catalogos.getDocentes(),
      zonas: this.catalogos.getZonas(),
      turnos: this.api.getCustom<Turno[]>(`turnos/fecha/${hoy}`),
      incidentes: this.api.getAll<Incidente>('incidentes')
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: (data) => {
        this.totalDocentes = data.docentes.filter(x => x.activo).length;
        this.totalZonas = data.zonas.filter(x => x.activa).length;
        this.zonasList = data.zonas;
        this.turnosHoy = data.turnos.length;
        this.incidentesMes = data.incidentes.filter(x => x.fechaHora?.startsWith(mesActual)).length;
        
        // Cargar zonas en el mapa
        this.zonasRects = data.zonas.filter(z => z.activa).map(z => {
          const incidentesZona = data.incidentes.filter(i => i.zona?.id === z.id && i.fechaHora?.startsWith(mesActual)).length;
          const heatVal = Math.min(100, incidentesZona * 15);
          
          return {
            id: z.id || 0,
            x: z.posX || 0,
            y: z.posY || 0,
            w: z.posWidth || 100,
            h: z.posHeight || 100,
            nombre: z.nombre,
            area: z.capacidadMaxima || 0,
            heat: heatVal,
            color: this.heatToColor(heatVal),
            imagenMapaUrl: z.imagenMapaUrl
          };
        });

        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.initMapaEditor(), 300);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.bgImage = null; // Free up memory
  }

  @HostListener('window:resize')
  onResize() {
    this.resizeCanvas();
  }

  private initMapaEditor(): void {
    if (!this.canvasRef) return;
    const canvas = this.canvasRef.nativeElement;
    
    // Bind UI elements that are strictly part of canvas editor
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

    if (!hint || !zonaPanel || !btnSelectImg || !imgInput) return;

    this.resizeCanvas();

    // Setup Canvas mouse events directly on the element ref
    canvas.onmousedown = (e: MouseEvent) => {
      const p = this.getPos(e);
      const hit = this.zonasRects.find(z => p.x >= z.x && p.x <= z.x + z.w && p.y >= z.y && p.y <= z.y + z.h);
      if (hit) {
        this.selectedId = hit.id;
        zonaPanel.style.display = 'flex';
        zonaNombre.value = hit.nombre;
        zonaArea.value = String(hit.area);
        zonaHeat.value = String(hit.heat);
        heatPreview.style.background = this.heatToColor(hit.heat, 0.7);
        this.redraw();
        return;
      }
      this.isDrawing = true;
      this.startX = p.x; 
      this.startY = p.y;
      this.currentRect = { x: this.startX, y: this.startY, w: 0, h: 0 };
      hint.style.display = 'none';
    };

    canvas.onmousemove = (e: MouseEvent) => {
      if (!this.isDrawing) return;
      const p = this.getPos(e);
      this.currentRect = { x: this.startX, y: this.startY, w: p.x - this.startX, h: p.y - this.startY };
      this.redraw();
    };

    canvas.onmouseup = () => {
      if (!this.isDrawing) return;
      this.isDrawing = false;
      if (Math.abs(this.currentRect.w) > 10 && Math.abs(this.currentRect.h) > 10) {
        const newZona: ZonaRect = {
          id: this.nextId++,
          x: this.currentRect.w < 0 ? this.currentRect.x + this.currentRect.w : this.currentRect.x,
          y: this.currentRect.h < 0 ? this.currentRect.y + this.currentRect.h : this.currentRect.y,
          w: Math.abs(this.currentRect.w),
          h: Math.abs(this.currentRect.h),
          nombre: '',
          area: 0,
          heat: 0,
          color: this.heatToColor(0)
        };
        newZona.area = this.mts2(newZona);
        this.zonasRects.push(newZona);
        this.selectedId = newZona.id;
        zonaPanel.style.display = 'flex';
        zonaNombre.value = '';
        zonaArea.value = String(newZona.area);
        zonaHeat.value = '0';
        heatPreview.style.background = this.heatToColor(0, 0.7);
      }
      this.redraw();
    };

    zonaHeat.oninput = () => {
      heatPreview.style.background = this.heatToColor(Number(zonaHeat.value), 0.7);
      const z = this.zonasRects.find(z => z.id === this.selectedId);
      if (z) { z.heat = Number(zonaHeat.value); this.redraw(); }
    };

    btnGuardar.onclick = () => {
      const z = this.zonasRects.find(z => z.id === this.selectedId);
      if (z) {
        z.nombre = zonaNombre.value || `Zona ${z.id}`;
        z.heat = Number(zonaHeat.value);
        z.area = Number(zonaArea.value);

        // Update Zona on server
        this.api.getCustom<Zona>(`zonas/${z.id}`).subscribe(zonaServer => {
          zonaServer.posX = z.x;
          zonaServer.posY = z.y;
          zonaServer.posWidth = z.w;
          zonaServer.posHeight = z.h;
          
          this.api.putCustom<Zona>(`zonas/${z.id}`, zonaServer).subscribe(() => {
            this.catalogos.invalidarZonas();
            this.toast.success('Coordenadas de la zona guardadas exitosamente');
            this.renderZonasGuardadas(zonasGuardadas);
            this.redraw();
          });
        });
      }
    };

    // Botón papelera: limpia TODAS las zonas Y la imagen de fondo
    btnDelete.onclick = () => {
      this.zonasRects.length = 0;
      this.selectedId = null;
      this.bgImage = null;
      zonaPanel.style.display = 'none';
      hint.style.display = 'flex';
      this.renderZonasGuardadas(zonasGuardadas);
      this.redraw();
    };

    // Botón ✕: cancela la selección actual y cierra el panel sin borrar nada
    btnClear.onclick = () => {
      this.selectedId = null;
      zonaPanel.style.display = 'none';
    };

    // Botón imagen: abre el selector de archivos del sistema
    btnSelectImg.onclick = () => imgInput.click();

    // Al seleccionar imagen: lee con FileReader como DataURL y dibuja con proporciones cover
    imgInput.onchange = () => {
      const file = imgInput.files?.[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = (ev) => {
        const img = new Image();
        img.onload = () => {
          this.bgImage = img;
          hint.style.display = 'none';
          this.resizeCanvas();
        };
        img.src = ev.target?.result as string;
      };
      reader.readAsDataURL(file);
    };
  }

  private resizeCanvas() {
    if (!this.canvasRef) return;
    const canvas = this.canvasRef.nativeElement;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    const rect = canvas.getBoundingClientRect();
    if (rect.width === 0 || rect.height === 0) return;
    
    canvas.width = rect.width * window.devicePixelRatio;
    canvas.height = rect.height * window.devicePixelRatio;
    ctx.scale(window.devicePixelRatio, window.devicePixelRatio);
    this.redraw();
  }

  private heatToColor(heat: number, alpha = 0.4): string {
    if (heat < 33) return `rgba(0,200,80,${alpha})`;
    if (heat < 66) return `rgba(255,180,0,${alpha})`;
    return `rgba(255,60,60,${alpha})`;
  }

  // Dibuja la imagen de fondo con escalado object-fit: cover (mantiene proporciones)
  private drawBgImage(ctx: CanvasRenderingContext2D, w: number, h: number): void {
    if (!this.bgImage) return;
    const scale = Math.max(w / this.bgImage.width, h / this.bgImage.height);
    const dw = this.bgImage.width * scale;
    const dh = this.bgImage.height * scale;
    const dx = (w - dw) / 2;
    const dy = (h - dh) / 2;
    ctx.drawImage(this.bgImage, dx, dy, dw, dh);
  }

  // Redibuja el canvas: capa 1 imagen de fondo, capa 2 zonas con etiquetas
  private redraw() {
    if (!this.canvasRef) return;
    const canvas = this.canvasRef.nativeElement;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const w = canvas.getBoundingClientRect().width;
    const h = canvas.getBoundingClientRect().height;
    ctx.clearRect(0, 0, w, h);

    if (this.bgImage) {
      this.drawBgImage(ctx, w, h);
    }

    this.zonasRects.forEach(z => {
      ctx.fillStyle = this.heatToColor(z.heat, 0.35);
      ctx.fillRect(z.x, z.y, z.w, z.h);
      ctx.strokeStyle = this.selectedId === z.id ? '#818CF8' : this.heatToColor(z.heat, 0.9);
      ctx.lineWidth = this.selectedId === z.id ? 2 : 1;
      ctx.strokeRect(z.x, z.y, z.w, z.h);
      
      ctx.fillStyle = 'rgba(255,255,255,0.9)';
      ctx.font = '11px sans-serif';
      ctx.fillText(z.nombre || `Zona ${z.id}`, z.x + 6, z.y + 16);
    });

    if (this.isDrawing) {
      ctx.strokeStyle = '#818CF8';
      ctx.lineWidth = 2;
      ctx.setLineDash([5, 3]);
      ctx.strokeRect(this.currentRect.x, this.currentRect.y, this.currentRect.w, this.currentRect.h);
      ctx.setLineDash([]);
      ctx.fillStyle = 'rgba(129,140,248,0.1)';
      ctx.fillRect(this.currentRect.x, this.currentRect.y, this.currentRect.w, this.currentRect.h);
    }
  }

  private getPos(e: MouseEvent | TouchEvent) {
    if (!this.canvasRef) return { x: 0, y: 0 };
    const rect = this.canvasRef.nativeElement.getBoundingClientRect();
    const src = 'touches' in e ? (e as TouchEvent).touches[0] : (e as MouseEvent);
    return { x: src.clientX - rect.left, y: src.clientY - rect.top };
  }

  private mts2(z: ZonaRect): number {
    if (!this.bgImage) return Math.round(Math.abs(z.w * z.h) / 100);
    const px2m = 0.01;
    return Math.round(Math.abs(z.w * z.h) * px2m);
  }

  private renderZonasGuardadas(container: HTMLElement) {
    if (!container) return;
    container.innerHTML = this.zonasRects.map(z => `
      <div class="zona-item">
        <div class="zona-item-dot" style="background:${this.heatToColor(z.heat, 0.9)}"></div>
        <span class="zona-item-name">${z.nombre || `Zona ${z.id}`}</span>
        <span class="zona-item-area">${z.area} m²</span>
      </div>
    `).join('');
  }

  generarTurnos() {
    this.generandoTurnos = true;
    this.api.postCustom<Turno[]>('turnos/generar-desde-horario', {}).subscribe({
      next: (turnosGenerados) => {
        this.generandoTurnos = false;
        if (turnosGenerados.length > 0) {
          this.toast.success(`Se generaron ${turnosGenerados.length} nuevos turnos con éxito.`);
          this.turnosHoy += turnosGenerados.length;
        } else {
          this.toast.success('No se generaron turnos nuevos. Ya estaban creados.');
        }
      },
      error: () => {
        this.generandoTurnos = false;
        this.toast.error('Error al generar turnos automáticamente.');
      }
    });
  }

  // ── CRUD Zonas ───────────────────────────────────────────────
  abrirCrearZona(): void {
    this.zonaEditId = null;
    this.zonaError = '';
    this.zonaForm.reset({ tipo: TipoZona.PATIO, capacidadMaxima: 50, activa: true });
    this.showZonaModal = true;
  }

  abrirEditarZona(z: Zona): void {
    this.zonaEditId = z.id ?? null;
    this.zonaError = '';
    this.zonaForm.patchValue(z);
    this.showZonaModal = true;
  }

  guardarZona(): void {
    if (this.zonaForm.invalid) return;
    this.zonaError = '';
    const payload = this.zonaForm.value;

    const op = this.zonaEditId
      ? this.api.put<Zona>('zonas', this.zonaEditId, payload)
      : this.api.post<Zona>('zonas', payload);

    op.subscribe({
      next: () => {
        this.showZonaModal = false;
        this.toast.success(this.zonaEditId ? 'Zona actualizada' : 'Zona creada');
        this.catalogos.invalidarZonas();
        this.recargarZonas();
      },
      error: (err) => {
        this.zonaError = err?.error?.mensaje ?? 'Error al guardar la zona.';
      }
    });
  }

  confirmarEliminarZona(z: Zona): void {
    this.zonaAEliminar = z;
  }

  cancelarEliminar(): void {
    this.zonaAEliminar = null;
  }

  eliminarZona(z: Zona): void {
    if (!z.id) return;
    this.api.delete('zonas', z.id).subscribe({
      next: () => {
        this.zonaAEliminar = null;
        this.toast.success('Zona eliminada');
        this.catalogos.invalidarZonas();
        this.recargarZonas();
      },
      error: (err) => {
        this.zonaAEliminar = null;
        const msg = err?.error?.mensaje ?? 'No se puede eliminar esta zona.';
        this.toast.error(msg);
      }
    });
  }

  private recargarZonas(): void {
    this.catalogos.getZonas().subscribe(zonas => {
      this.zonasList = zonas;
      this.totalZonas = zonas.filter(z => z.activa).length;
    });
  }
}