package com.juan.vigidocente.service;

import com.juan.vigidocente.exception.RecursoNoEncontradoException;
import com.juan.vigidocente.model.Incidente;
import com.juan.vigidocente.model.SeveridadIncidente;
import com.juan.vigidocente.model.TipoIncidente;
import com.juan.vigidocente.repository.IncidenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidenteService {

    private final IncidenteRepository incidenteRepository;

    public List<Incidente> listarTodos() {
        return incidenteRepository.findAll();
    }

    public Incidente buscarPorId(Long id) {
        Incidente incidente = incidenteRepository.findByIdConRelaciones(id);
        if (incidente == null) {
            throw new RecursoNoEncontradoException("Incidente no encontrado: " + id);
        }
        return incidente;
    }

    @Transactional
    public Incidente guardar(Incidente incidente) {
        return incidenteRepository.save(incidente);
    }

    @Transactional
    public void eliminar(Long id) {
        incidenteRepository.deleteById(id);
    }

    public List<Incidente> buscarPorTurno(Long turnoId) {
        return incidenteRepository.findByTurnoIdConZona(turnoId);
    }

    public List<Incidente> buscarPorZona(Long zonaId) {
        return incidenteRepository.findByZonaIdConTurno(zonaId);
    }

    public List<Incidente> buscarPorTipo(TipoIncidente tipo) {
        return incidenteRepository.findByTipo(tipo);
    }

    public List<Incidente> buscarPorSeveridad(SeveridadIncidente severidad) {
        return incidenteRepository.findBySeveridad(severidad);
    }
}