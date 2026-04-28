package com.juan.vigidocente.service;

import com.juan.vigidocente.model.EstadoTurno;
import com.juan.vigidocente.model.TipoFranja;
import com.juan.vigidocente.model.Turno;
import com.juan.vigidocente.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;

    public List<Turno> listarTodos() {
        return turnoRepository.findAll();
    }

    public Turno buscarPorId(Long id) {
        return turnoRepository.findByIdConRelaciones(id);
    }

    @Transactional
    public Turno guardar(Turno turno) {
        return turnoRepository.save(turno);
    }

    @Transactional
    public void eliminar(Long id) {
        turnoRepository.deleteById(id);
    }

    public List<Turno> buscarPorFecha(LocalDate fecha) {
        return turnoRepository.findByFecha(fecha);
    }

    public List<Turno> buscarPorEstado(EstadoTurno estado) {
        return turnoRepository.findByEstado(estado);
    }

    public List<Turno> buscarPorDocente(Long docenteId) {
        return turnoRepository.findByDocenteId(docenteId);
    }

    public List<Turno> buscarPorZonaConReasignaciones(Long zonaId) {
        return turnoRepository.findByZonaIdConReasignaciones(zonaId);
    }

    public List<Turno> buscarPendientesHoy() {
        return turnoRepository.findTurnosPendientesHoyConRegistros();
    }

    public List<Turno> buscarPorDocenteYRango(Long docenteId, LocalDate desde, LocalDate hasta) {
        return turnoRepository.findByDocenteIdYRangoFecha(docenteId, desde, hasta);
    }

    public List<Turno> buscarPorFranja(TipoFranja franja) {
        return turnoRepository.findByFranjaConRelaciones(franja);
    }
}