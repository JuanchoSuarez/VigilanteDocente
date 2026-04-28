package com.juan.vigidocente.rest;

import com.juan.vigidocente.exception.DatosInvalidosException;
import com.juan.vigidocente.model.PerfilDocente;
import com.juan.vigidocente.service.PerfilDocenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/perfiles")
@RequiredArgsConstructor
public class PerfilDocenteRestController {

    private final PerfilDocenteService perfilDocenteService;

    @GetMapping
    public List<PerfilDocente> listar() {
        return perfilDocenteService.listarTodos();
    }

    @GetMapping("/{id}")
    public PerfilDocente obtener(@PathVariable Long id) {
        return perfilDocenteService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<PerfilDocente> crear(@RequestBody PerfilDocente perfil) {
        if (perfil.getDocente() == null) {
            throw new DatosInvalidosException("El perfil debe estar asociado a un docente");
        }
        if (perfil.getEspecialidad() == null || perfil.getEspecialidad().isBlank()) {
            throw new DatosInvalidosException("La especialidad no puede estar vacía");
        }
        PerfilDocente guardado = perfilDocenteService.guardar(perfil);
        return ResponseEntity.status(201).body(guardado);
    }

    @PutMapping("/{id}")
    public PerfilDocente actualizar(@PathVariable Long id, @RequestBody PerfilDocente perfil) {
        perfilDocenteService.buscarPorId(id);
        perfil.setId(id);
        return perfilDocenteService.guardar(perfil);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        perfilDocenteService.buscarPorId(id);
        perfilDocenteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}