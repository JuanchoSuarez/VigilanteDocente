package com.juan.vigidocente.service;

import com.juan.vigidocente.model.TipoZona;
import com.juan.vigidocente.model.Zona;
import com.juan.vigidocente.repository.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZonaService {

    private final ZonaRepository zonaRepository;

    public List<Zona> listarTodos() {
        return zonaRepository.findAll();
    }

    public Zona buscarPorId(Long id) {
        return zonaRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Zona guardar(Zona zona) {
        return zonaRepository.save(zona);
    }

    @Transactional
    public void eliminar(Long id) {
        zonaRepository.deleteById(id);
    }

    public List<Zona> buscarActivas() {
        return zonaRepository.findByActiva(true);
    }

    public List<Zona> buscarActivasConTurnos() {
        return zonaRepository.findActivasConTurnos();
    }

    public List<Zona> buscarConTurnosPendientesHoy() {
        return zonaRepository.findZonasConTurnosPendientesHoy();
    }

    public List<Zona> buscarPorTipo(TipoZona tipo) {
        return zonaRepository.findByTipo(tipo);
    }

    public List<Zona> buscarPorTipoConTurnos(TipoZona tipo) {
        return zonaRepository.findByTipoConTurnos(tipo);
    }

    public List<Object[]> obtenerConteoTurnosPorZona() {
        return zonaRepository.countTurnosPorZona();
    }
}