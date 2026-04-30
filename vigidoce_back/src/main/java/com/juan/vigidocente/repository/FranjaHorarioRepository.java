package com.juan.vigidocente.repository;

import com.juan.vigidocente.model.DiaSemana;
import com.juan.vigidocente.model.FranjaHorario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FranjaHorarioRepository extends JpaRepository<FranjaHorario, Long> {
    List<FranjaHorario> findByActivo(Boolean activo);
    List<FranjaHorario> findByDiaSemanaAndActivo(DiaSemana diaSemana, Boolean activo);
    List<FranjaHorario> findByDocenteIdAndDiaSemanaAndActivo(Long docenteId, DiaSemana diaSemana, Boolean activo);
    List<FranjaHorario> findByZonaId(Long zonaId);
}
