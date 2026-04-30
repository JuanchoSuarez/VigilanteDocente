package com.juan.vigidocente.repository;

import com.juan.vigidocente.model.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Long> {
    List<Horario> findByActivo(Boolean activo);
}
