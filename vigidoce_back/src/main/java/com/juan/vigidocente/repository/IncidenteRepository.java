package com.juan.vigidocente.repository;

import com.juan.vigidocente.model.Incidente;
import com.juan.vigidocente.model.SeveridadIncidente;
import com.juan.vigidocente.model.TipoIncidente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IncidenteRepository extends JpaRepository<Incidente, Long> {

    List<Incidente> findByTurnoId(Long turnoId);
    List<Incidente> findByZonaId(Long zonaId);
    List<Incidente> findByTipo(TipoIncidente tipo);
    List<Incidente> findBySeveridad(SeveridadIncidente severidad);

    @Query("SELECT i FROM Incidente i " +
            "JOIN FETCH i.turno JOIN FETCH i.zona " +
            "WHERE i.id = :id")
    Incidente findByIdConRelaciones(@Param("id") Long id);

    @Query("SELECT i FROM Incidente i " +
            "JOIN FETCH i.zona " +
            "WHERE i.turno.id = :turnoId ORDER BY i.fechaHora DESC")
    List<Incidente> findByTurnoIdConZona(@Param("turnoId") Long turnoId);

    @Query("SELECT i FROM Incidente i " +
            "JOIN FETCH i.turno " +
            "WHERE i.zona.id = :zonaId ORDER BY i.fechaHora DESC")
    List<Incidente> findByZonaIdConTurno(@Param("zonaId") Long zonaId);
}