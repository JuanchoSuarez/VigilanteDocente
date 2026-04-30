package com.juan.vigidocente.rest;

import com.juan.vigidocente.model.Horario;
import com.juan.vigidocente.repository.HorarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios")
@RequiredArgsConstructor
public class HorarioRestController {

    private final HorarioRepository horarioRepository;

    @GetMapping
    public List<Horario> listar() {
        return horarioRepository.findAll();
    }

    @GetMapping("/{id}")
    public Horario obtener(@PathVariable Long id) {
        return horarioRepository.findById(id).orElseThrow();
    }

    @PostMapping
    public ResponseEntity<Horario> crear(@RequestBody Horario horario) {
        return ResponseEntity.status(201).body(horarioRepository.save(horario));
    }

    @PutMapping("/{id}")
    public Horario actualizar(@PathVariable Long id, @RequestBody Horario horario) {
        horario.setId(id);
        return horarioRepository.save(horario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        horarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
