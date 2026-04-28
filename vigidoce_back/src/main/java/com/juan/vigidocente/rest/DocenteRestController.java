package com.juan.vigidocente.rest;

import com.juan.vigidocente.exception.DatosInvalidosException;
import com.juan.vigidocente.exception.RecursoNoEncontradoException;
import com.juan.vigidocente.model.Docente;
import com.juan.vigidocente.service.DocenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docentes")
@RequiredArgsConstructor
public class DocenteRestController {

    private final DocenteService docenteService;

    @GetMapping
    public List<Docente> listar() {
        return docenteService.listarTodos();
    }

    @GetMapping("/{id}")
    public Docente obtener(@PathVariable Long id) {
        return docenteService.buscarPorIdConPerfil(id);
    }

    @PostMapping
    public ResponseEntity<Docente> crear(@RequestBody Docente docente) {
        if (docente.getNombre() == null || docente.getNombre().isBlank()) {
            throw new DatosInvalidosException("El nombre del docente no puede estar vacío");
        }
        if (docente.getEmail() == null || docente.getEmail().isBlank()) {
            throw new DatosInvalidosException("El email del docente no puede estar vacío");
        }
        Docente guardado = docenteService.guardar(docente);
        return ResponseEntity.status(201).body(guardado);
    }

    @PutMapping("/{id}")
    public Docente actualizar(@PathVariable Long id, @RequestBody Docente docente) {
        docenteService.buscarPorId(id);
        docente.setId(id);
        return docenteService.guardar(docente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        docenteService.buscarPorId(id);
        docenteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}