package com.juan.vigidocente.rest;

import com.juan.vigidocente.model.DiaSemana;
import com.juan.vigidocente.model.FranjaHorario;
import com.juan.vigidocente.repository.FranjaHorarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/franjas")
@RequiredArgsConstructor
public class FranjaHorarioRestController {

    private final FranjaHorarioRepository franjaRepository;

    @GetMapping
    public List<FranjaHorario> listar() {
        return franjaRepository.findAll();
    }

    @GetMapping("/{id}")
    public FranjaHorario obtener(@PathVariable Long id) {
        return franjaRepository.findById(id).orElseThrow();
    }

    @PostMapping
    public ResponseEntity<FranjaHorario> crear(@RequestBody FranjaHorario franja) {
        return ResponseEntity.status(201).body(franjaRepository.save(franja));
    }

    @PutMapping("/{id}")
    public FranjaHorario actualizar(@PathVariable Long id, @RequestBody FranjaHorario franja) {
        franja.setId(id);
        return franjaRepository.save(franja);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        franjaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/hoy")
    public List<FranjaHorario> getFranjasHoy() {
        DiaSemana dia = obtenerDiaHoy();
        return franjaRepository.findByDiaSemanaAndActivo(dia, true);
    }

    @GetMapping("/docente/{docenteId}/hoy")
    public List<FranjaHorario> getFranjasDocenteHoy(@PathVariable Long docenteId) {
        DiaSemana dia = obtenerDiaHoy();
        return franjaRepository.findByDocenteIdAndDiaSemanaAndActivo(docenteId, dia, true);
    }

    @GetMapping("/zona/{zonaId}")
    public List<FranjaHorario> getFranjasPorZona(@PathVariable Long zonaId) {
        return franjaRepository.findByZonaId(zonaId);
    }

    private DiaSemana obtenerDiaHoy() {
        String diaStr = LocalDate.now().getDayOfWeek().name();
        return switch (diaStr) {
            case "MONDAY" -> DiaSemana.LUNES;
            case "TUESDAY" -> DiaSemana.MARTES;
            case "WEDNESDAY" -> DiaSemana.MIERCOLES;
            case "THURSDAY" -> DiaSemana.JUEVES;
            case "FRIDAY" -> DiaSemana.VIERNES;
            case "SATURDAY" -> DiaSemana.SABADO;
            case "SUNDAY" -> DiaSemana.DOMINGO;
            default -> DiaSemana.LUNES;
        };
    }
}
