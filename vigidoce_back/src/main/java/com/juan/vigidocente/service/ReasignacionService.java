package com.juan.vigidocente.service;

import com.juan.vigidocente.exception.DatosInvalidosException;
import com.juan.vigidocente.exception.RecursoNoEncontradoException;
import com.juan.vigidocente.model.*;
import com.juan.vigidocente.repository.DocenteRepository;
import com.juan.vigidocente.repository.ReasignacionRepository;
import com.juan.vigidocente.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReasignacionService {

    private final ReasignacionRepository reasignacionRepository;
    private final TurnoRepository turnoRepository;
    private final DocenteRepository docenteRepository;

    public List<Reasignacion> listarTodos() {
        return reasignacionRepository.findAll();
    }

    public Reasignacion buscarPorId(Long id) {
        return reasignacionRepository.findByIdConRelaciones(id);
    }

    @Transactional
    public Reasignacion guardar(Reasignacion reasignacion) {
        return reasignacionRepository.save(reasignacion);
    }

    @Transactional
    public void eliminar(Long id) {
        reasignacionRepository.deleteById(id);
    }

    public List<Reasignacion> buscarPorEstado(EstadoReasignacion estado) {
        return reasignacionRepository.findByEstado(estado);
    }

    public List<Reasignacion> buscarPendientes() {
        return reasignacionRepository.findPendientesConRelaciones();
    }

    public List<Reasignacion> buscarHistorialPorDocente(Long docenteId) {
        return reasignacionRepository.findHistorialPorDocente(docenteId);
    }

    public List<Reasignacion> buscarPorTurnoConDocentes(Long turnoId) {
        return reasignacionRepository.findByTurnoIdConDocentes(turnoId);
    }

    public List<Object[]> obtenerConteoPorEstado() {
        return reasignacionRepository.countPorEstado();
    }

    @Transactional
    public Reasignacion solicitarReasignacion(Long turnoId, Long docenteId, String motivo) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado: " + turnoId));
        Docente docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Docente no encontrado: " + docenteId));

        Reasignacion reasignacion = Reasignacion.builder()
                .turno(turno)
                .docenteOriginal(docente)
                .motivo(motivo)
                .fechaHoraSolicitud(LocalDateTime.now())
                .estado(EstadoReasignacion.PENDIENTE)
                .build();

        return reasignacionRepository.save(reasignacion);
    }

    @Transactional
    public Reasignacion aceptarReasignacion(Long reasignacionId, Long docenteReemplazoId) {
        Reasignacion reasignacion = reasignacionRepository.findByIdConRelaciones(reasignacionId);
        if (reasignacion == null) {
            throw new RecursoNoEncontradoException("Reasignación no encontrada: " + reasignacionId);
        }
        if (reasignacion.getEstado() != EstadoReasignacion.PENDIENTE) {
            throw new DatosInvalidosException("Solo se pueden aceptar reasignaciones en estado PENDIENTE");
        }

        Docente reemplazo = docenteRepository.findById(docenteReemplazoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Docente reemplazo no encontrado: " + docenteReemplazoId));

        reasignacion.setDocenteReemplazo(reemplazo);
        reasignacion.setEstado(EstadoReasignacion.ACEPTADA);
        reasignacion.setFechaHoraRespuesta(LocalDateTime.now());

        Turno turno = reasignacion.getTurno();
        turno.setDocente(reemplazo);
        turnoRepository.save(turno);

        return reasignacionRepository.save(reasignacion);
    }

    @Transactional
    public Reasignacion rechazarReasignacion(Long reasignacionId) {
        Reasignacion reasignacion = reasignacionRepository.findByIdConRelaciones(reasignacionId);
        if (reasignacion == null) {
            throw new RecursoNoEncontradoException("Reasignación no encontrada: " + reasignacionId);
        }
        if (reasignacion.getEstado() != EstadoReasignacion.PENDIENTE) {
            throw new DatosInvalidosException("Solo se pueden rechazar reasignaciones en estado PENDIENTE");
        }

        reasignacion.setEstado(EstadoReasignacion.RECHAZADA);
        reasignacion.setFechaHoraRespuesta(LocalDateTime.now());

        return reasignacionRepository.save(reasignacion);
    }
}