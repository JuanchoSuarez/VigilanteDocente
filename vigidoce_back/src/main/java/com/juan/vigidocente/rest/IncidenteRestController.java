package com.juan.vigidocente.rest;

import com.juan.vigidocente.exception.DatosInvalidosException;
import com.juan.vigidocente.model.Incidente;
import com.juan.vigidocente.model.SeveridadIncidente;
import com.juan.vigidocente.model.TipoIncidente;
import com.juan.vigidocente.service.IncidenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidentes")
@RequiredArgsConstructor
public class IncidenteRestController {

    private final IncidenteService incidenteService;

    @GetMapping
    public List<Incidente> listar() {
        return incidenteService.listarTodos();
    }

    @GetMapping("/{id}")
    public Incidente obtener(@PathVariable Long id) {
        return incidenteService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Incidente> crear(@RequestBody Incidente incidente) {
        if (incidente.getDescripcion() == null || incidente.getDescripcion().isBlank()) {
            throw new DatosInvalidosException("La descripción del incidente no puede estar vacía");
        }
        if (incidente.getTurno() == null) {
            throw new DatosInvalidosException("El incidente debe estar asociado a un turno");
        }
        if (incidente.getZona() == null) {
            throw new DatosInvalidosException("El incidente debe tener una zona asignada");
        }
        if (incidente.getTipo() == null) {
            throw new DatosInvalidosException("El tipo de incidente no puede estar vacío");
        }
        if (incidente.getSeveridad() == null) {
            throw new DatosInvalidosException("La severidad del incidente no puede estar vacía");
        }
        if (incidente.getFechaHora() == null) {
            throw new DatosInvalidosException("La fecha y hora del incidente no puede estar vacía");
        }
        Incidente guardado = incidenteService.guardar(incidente);
        return ResponseEntity.status(201).body(guardado);
    }

    @PutMapping("/{id}")
    public Incidente actualizar(@PathVariable Long id, @RequestBody Incidente incidente) {
        incidenteService.buscarPorId(id);
        incidente.setId(id);
        return incidenteService.guardar(incidente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        incidenteService.buscarPorId(id);
        incidenteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ── Endpoints adicionales ──────────────────────────────────────────────────

    @GetMapping("/por-turno/{turnoId}")
    public List<Incidente> porTurno(@PathVariable Long turnoId) {
        return incidenteService.buscarPorTurno(turnoId);
    }

    @GetMapping("/por-zona/{zonaId}")
    public List<Incidente> porZona(@PathVariable Long zonaId) {
        return incidenteService.buscarPorZona(zonaId);
    }

    @GetMapping("/por-tipo/{tipo}")
    public List<Incidente> porTipo(@PathVariable TipoIncidente tipo) {
        return incidenteService.buscarPorTipo(tipo);
    }

    @GetMapping("/por-severidad/{severidad}")
    public List<Incidente> porSeveridad(@PathVariable SeveridadIncidente severidad) {
        return incidenteService.buscarPorSeveridad(severidad);
    }

    @GetMapping("/criticos")
    public List<Incidente> criticos() {
        return incidenteService.buscarPorSeveridad(SeveridadIncidente.S3_ATENCION_INMEDIATA);
    }
}