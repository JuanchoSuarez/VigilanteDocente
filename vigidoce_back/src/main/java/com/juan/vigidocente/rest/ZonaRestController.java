package com.juan.vigidocente.rest;

import com.juan.vigidocente.exception.DatosInvalidosException;
import com.juan.vigidocente.model.Zona;
import com.juan.vigidocente.service.ZonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/zonas")
@RequiredArgsConstructor
public class ZonaRestController {

    private final ZonaService zonaService;

    @GetMapping
    public List<Zona> listar() {
        return zonaService.listarTodos();
    }

    @GetMapping("/{id}")
    public Zona obtener(@PathVariable Long id) {
        return zonaService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Zona> crear(@RequestBody Zona zona) {
        if (zona.getNombre() == null || zona.getNombre().isBlank()) {
            throw new DatosInvalidosException("El nombre de la zona no puede estar vacío");
        }
        if (zona.getTipo() == null) {
            throw new DatosInvalidosException("El tipo de zona no puede estar vacío");
        }
        Zona guardada = zonaService.guardar(zona);
        return ResponseEntity.status(201).body(guardada);
    }

    @PutMapping("/{id}")
    public Zona actualizar(@PathVariable Long id, @RequestBody Zona zona) {
        zonaService.buscarPorId(id);
        zona.setId(id);
        return zonaService.guardar(zona);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        zonaService.buscarPorId(id);
        try {
            zonaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (DatosInvalidosException e) {
            return ResponseEntity.status(409).body(Map.of("mensaje", e.getMessage()));
        }
    }
}