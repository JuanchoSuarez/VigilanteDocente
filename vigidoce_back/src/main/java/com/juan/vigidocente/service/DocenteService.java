package com.juan.vigidocente.service;

import com.juan.vigidocente.model.Docente;
import com.juan.vigidocente.model.RolDocente;
import com.juan.vigidocente.repository.DocenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocenteService {

    private final DocenteRepository docenteRepository;

    public List<Docente> listarTodos() {
        return docenteRepository.findAll();
    }

    public Docente buscarPorId(Long id) {
        return docenteRepository.findById(id).orElseThrow();
    }

    public Docente buscarPorIdConPerfil(Long id) {
        return docenteRepository.findByIdConPerfil(id);
    }

    public Docente buscarPorIdConRegistros(Long id) {
        return docenteRepository.findByIdConRegistros(id);
    }

    @Transactional
    public Docente guardar(Docente docente) {
        return docenteRepository.save(docente);
    }

    @Transactional
    public void eliminar(Long id) {
        docenteRepository.deleteById(id);
    }

    public List<Docente> buscarActivos() {
        return docenteRepository.findByActivo(true);
    }

    public List<Docente> buscarActivosConTurnos() {
        return docenteRepository.findActivosConTurnos();
    }

    public List<Docente> buscarPorRol(RolDocente rol) {
        return docenteRepository.findByRol(rol);
    }

    public List<Docente> buscarPorRolConPerfil(RolDocente rol) {
        return docenteRepository.findByRolConPerfil(rol);
    }

    public List<Docente> obtenerRankingGamificacion() {
        return docenteRepository.findRankingGamificacion();
    }
}