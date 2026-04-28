package com.juan.vigidocente.rest;

import com.juan.vigidocente.exception.DatosInvalidosException;
import com.juan.vigidocente.model.MetodoRegistro;
import com.juan.vigidocente.model.RegistroVigilancia;
import com.juan.vigidocente.service.RegistroVigilanciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registros")
@RequiredArgsConstructor
public class RegistroVigilanciaRestController {

    private final RegistroVigilanciaService registroVigilanciaService;

    @GetMapping
    public List<RegistroVigilancia> listar() {
        return registroVigilanciaService.listarTodos();
    }

    @GetMapping("/{id}")
    public RegistroVigilancia obtener(@PathVariable Long id) {
        return registroVigilanciaService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<RegistroVigilancia> crear(@RequestBody RegistroVigilancia registro) {
        if (registro.getTurno() == null) {
            throw new DatosInvalidosException("El registro debe estar asociado a un turno");
        }
        if (registro.getDocente() == null) {
            throw new DatosInvalidosException("El registro debe tener un docente asignado");
        }
        if (registro.getZona() == null) {
            throw new DatosInvalidosException("El registro debe tener una zona asignada");
        }
        if (registro.getFechaHoraCheckIn() == null) {
            throw new DatosInvalidosException("La fecha y hora de check-in no puede estar vacía");
        }
        RegistroVigilancia guardado = registroVigilanciaService.guardar(registro);
        return ResponseEntity.status(201).body(guardado);
    }

    @PutMapping("/{id}")
    public RegistroVigilancia actualizar(@PathVariable Long id, @RequestBody RegistroVigilancia registro) {
        registroVigilanciaService.buscarPorId(id);
        registro.setId(id);
        return registroVigilanciaService.guardar(registro);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        registroVigilanciaService.buscarPorId(id);
        registroVigilanciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ── Endpoints adicionales ──────────────────────────────────────────────────

    @GetMapping("/por-docente/{docenteId}")
    public List<RegistroVigilancia> porDocente(@PathVariable Long docenteId) {
        return registroVigilanciaService.buscarPorDocente(docenteId);
    }

    @GetMapping("/por-turno/{turnoId}")
    public List<RegistroVigilancia> porTurno(@PathVariable Long turnoId) {
        return registroVigilanciaService.buscarPorTurno(turnoId);
    }

    @GetMapping("/por-zona/{zonaId}")
    public List<RegistroVigilancia> porZona(@PathVariable Long zonaId) {
        return registroVigilanciaService.buscarPorZonaConDocente(zonaId);
    }

    @GetMapping("/con-recorrido")
    public List<RegistroVigilancia> conRecorrido() {
        return registroVigilanciaService.buscarConRecorridoRealizado();
    }

    @GetMapping("/promedio-limpieza")
    public List<Object[]> promedioLimpieza() {
        return registroVigilanciaService.obtenerPromedioLimpiezaPorZona();
    }

    @PostMapping("/qr-scan")
    public ResponseEntity<RegistroVigilancia> registrarPorQr(@RequestBody Map<String, Object> payload) {
        Long turnoId = Long.valueOf(payload.get("turnoId").toString());
        Long docenteId = Long.valueOf(payload.get("docenteId").toString());
        Long zonaId = Long.valueOf(payload.get("zonaId").toString());

        RegistroVigilancia registro = registroVigilanciaService.registrarCheckInQr(turnoId, docenteId, zonaId);
        return ResponseEntity.status(201).body(registro);
    }

    @PatchMapping("/{id}/recorrido")
    public RegistroVigilancia confirmarRecorrido(@PathVariable Long id) {
        return registroVigilanciaService.confirmarRecorrido(id);
    }

    @PatchMapping("/{id}/checkout")
    public RegistroVigilancia registrarCheckout(@PathVariable Long id,
                                                @RequestParam(required = false) Integer calificacionLimpieza) {
        return registroVigilanciaService.registrarCheckOut(id, calificacionLimpieza);
    }
}