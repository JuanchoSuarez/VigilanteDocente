package com.juan.vigidocente.rest;

import com.juan.vigidocente.exception.DatosInvalidosException;
import com.juan.vigidocente.model.EstadoTurno;
import com.juan.vigidocente.model.TipoFranja;
import com.juan.vigidocente.model.Turno;
import com.juan.vigidocente.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoRestController {

    private final TurnoService turnoService;

    @GetMapping
    public List<Turno> listar() {
        return turnoService.listarTodos();
    }

    @GetMapping("/{id}")
    public Turno obtener(@PathVariable Long id) {
        return turnoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Turno> crear(@RequestBody Turno turno) {
        if (turno.getFecha() == null) {
            throw new DatosInvalidosException("La fecha del turno no puede estar vacía");
        }
        if (turno.getDocente() == null) {
            throw new DatosInvalidosException("El turno debe tener un docente asignado");
        }
        if (turno.getZona() == null) {
            throw new DatosInvalidosException("El turno debe tener una zona asignada");
        }
        Turno guardado = turnoService.guardar(turno);
        return ResponseEntity.status(201).body(guardado);
    }

    @PutMapping("/{id}")
    public Turno actualizar(@PathVariable Long id, @RequestBody Turno turno) {
        turnoService.buscarPorId(id);
        turno.setId(id);
        return turnoService.guardar(turno);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        turnoService.buscarPorId(id);
        turnoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ── Endpoints adicionales ──────────────────────────────────────────────────

    @GetMapping("/pendientes-hoy")
    public List<Turno> pendientesHoy() {
        return turnoService.buscarPendientesHoy();
    }

    @GetMapping("/docente/{docenteId}")
    public List<Turno> porDocenteAlias(@PathVariable Long docenteId) {
        return turnoService.buscarPorDocente(docenteId);
    }

    @GetMapping("/fecha/{fecha}")
    public List<Turno> porFechaAlias(@PathVariable String fecha) {
        return turnoService.buscarPorFecha(LocalDate.parse(fecha));
    }

    @GetMapping("/por-docente/{docenteId}")
    public List<Turno> porDocente(@PathVariable Long docenteId) {
        return turnoService.buscarPorDocente(docenteId);
    }

    @GetMapping("/por-fecha/{fecha}")
    public List<Turno> porFecha(@PathVariable String fecha) {
        return turnoService.buscarPorFecha(LocalDate.parse(fecha));
    }

    @GetMapping("/por-franja/{franja}")
    public List<Turno> porFranja(@PathVariable TipoFranja franja) {
        return turnoService.buscarPorFranja(franja);
    }

    @GetMapping("/por-estado/{estado}")
    public List<Turno> porEstado(@PathVariable EstadoTurno estado) {
        return turnoService.buscarPorEstado(estado);
    }

    @PatchMapping("/{id}/estado")
    public Turno cambiarEstado(@PathVariable Long id, @RequestParam EstadoTurno estado) {
        Turno turno = turnoService.buscarPorId(id);
        turno.setEstado(estado);
        return turnoService.guardar(turno);
    }
}