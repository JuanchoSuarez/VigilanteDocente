package com.juan.vigidocente.rest;

import com.juan.vigidocente.exception.DatosInvalidosException;
import com.juan.vigidocente.model.EstadoReasignacion;
import com.juan.vigidocente.model.Reasignacion;
import com.juan.vigidocente.service.ReasignacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reasignaciones")
@RequiredArgsConstructor
public class ReasignacionRestController {

    private final ReasignacionService reasignacionService;

    @GetMapping
    public List<Reasignacion> listar() {
        return reasignacionService.listarTodos();
    }

    @GetMapping("/{id}")
    public Reasignacion obtener(@PathVariable Long id) {
        return reasignacionService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Reasignacion> crear(@RequestBody Reasignacion reasignacion) {
        if (reasignacion.getMotivo() == null || reasignacion.getMotivo().isBlank()) {
            throw new DatosInvalidosException("El motivo de la reasignación no puede estar vacío");
        }
        if (reasignacion.getTurno() == null) {
            throw new DatosInvalidosException("La reasignación debe estar asociada a un turno");
        }
        if (reasignacion.getDocenteOriginal() == null) {
            throw new DatosInvalidosException("La reasignación debe tener un docente original");
        }
        Reasignacion guardada = reasignacionService.guardar(reasignacion);
        return ResponseEntity.status(201).body(guardada);
    }

    @PutMapping("/{id}")
    public Reasignacion actualizar(@PathVariable Long id, @RequestBody Reasignacion reasignacion) {
        reasignacionService.buscarPorId(id);
        reasignacion.setId(id);
        return reasignacionService.guardar(reasignacion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reasignacionService.buscarPorId(id);
        reasignacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ── Endpoints adicionales ──────────────────────────────────────────────────

    @GetMapping("/pendientes")
    public List<Reasignacion> pendientes() {
        return reasignacionService.buscarPendientes();
    }

    @GetMapping("/por-estado/{estado}")
    public List<Reasignacion> porEstado(@PathVariable EstadoReasignacion estado) {
        return reasignacionService.buscarPorEstado(estado);
    }

    @GetMapping("/historial-docente/{docenteId}")
    public List<Reasignacion> historialDocente(@PathVariable Long docenteId) {
        return reasignacionService.buscarHistorialPorDocente(docenteId);
    }

    @GetMapping("/por-turno/{turnoId}")
    public List<Reasignacion> porTurno(@PathVariable Long turnoId) {
        return reasignacionService.buscarPorTurnoConDocentes(turnoId);
    }

    @GetMapping("/conteo-por-estado")
    public List<Object[]> conteoPorEstado() {
        return reasignacionService.obtenerConteoPorEstado();
    }

    @PostMapping("/solicitar")
    public ResponseEntity<Reasignacion> solicitar(@RequestBody Map<String, Object> payload) {
        Long turnoId = Long.valueOf(payload.get("turnoId").toString());
        Long docenteId = Long.valueOf(payload.get("docenteId").toString());
        String motivo = payload.get("motivo").toString();

        Reasignacion reasignacion = reasignacionService.solicitarReasignacion(turnoId, docenteId, motivo);
        return ResponseEntity.status(201).body(reasignacion);
    }

    @PatchMapping("/{id}/aceptar")
    public Reasignacion aceptar(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Long docenteReemplazoId = Long.valueOf(payload.get("docenteReemplazoId").toString());
        return reasignacionService.aceptarReasignacion(id, docenteReemplazoId);
    }

    @PatchMapping("/{id}/rechazar")
    public Reasignacion rechazar(@PathVariable Long id) {
        return reasignacionService.rechazarReasignacion(id);
    }
}