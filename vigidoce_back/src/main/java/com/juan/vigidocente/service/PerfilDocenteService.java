package com.juan.vigidocente.service;

import com.juan.vigidocente.model.PerfilDocente;
import com.juan.vigidocente.repository.PerfilDocenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PerfilDocenteService {

    private final PerfilDocenteRepository perfilDocenteRepository;

    public List<PerfilDocente> listarTodos() {
        return perfilDocenteRepository.findAll();
    }

    public PerfilDocente buscarPorId(Long id) {
        return perfilDocenteRepository.findByIdConDocente(id).orElseThrow();
    }

    public PerfilDocente buscarPorDocenteId(Long docenteId) {
        return perfilDocenteRepository.findByDocenteIdConDocente(docenteId).orElseThrow();
    }

    @Transactional
    public PerfilDocente guardar(PerfilDocente perfil) {
        return perfilDocenteRepository.save(perfil);
    }

    @Transactional
    public void eliminar(Long id) {
        perfilDocenteRepository.deleteById(id);
    }

    public List<PerfilDocente> obtenerRankingConDocente() {
        return perfilDocenteRepository.findRankingConDocente();
    }

    public List<PerfilDocente> buscarConReconocimiento() {
        return perfilDocenteRepository.findConReconocimiento();
    }
}