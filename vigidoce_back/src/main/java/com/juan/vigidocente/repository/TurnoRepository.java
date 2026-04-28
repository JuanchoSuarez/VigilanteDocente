package com.juan.vigidocente.repository;

import com.juan.vigidocente.model.EstadoTurno;
import com.juan.vigidocente.model.TipoFranja;
import com.juan.vigidocente.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    // --- Queries derivados por nombre (ya existían) ---
    List<Turno> findByFecha(LocalDate fecha);
    List<Turno> findByEstado(EstadoTurno estado);
    List<Turno> findByDocenteId(Long docenteId);
    List<Turno> findByZonaId(Long zonaId);

    // --- Queries JPQL con @Query (nuevos) ---

    // Carga el turno con su Docente y Zona (@ManyToOne) y sus Incidentes (@OneToMany)
    @Query("SELECT DISTINCT t FROM Turno t " +
            "JOIN FETCH t.docente " +
            "JOIN FETCH t.zona " +
            "LEFT JOIN FETCH t.incidentes " +
            "WHERE t.id = :id")
    Turno findByIdConRelaciones(@Param("id") Long id);

    // Turnos pendientes de hoy con sus registros de vigilancia (@OneToMany)
    @Query("SELECT DISTINCT t FROM Turno t " +
            "JOIN FETCH t.docente " +
            "JOIN FETCH t.zona " +
            "LEFT JOIN FETCH t.registrosVigilancia " +
            "WHERE t.estado = 'PENDIENTE' AND t.fecha = CURRENT_DATE")
    List<Turno> findTurnosPendientesHoyConRegistros();

    // Turnos de una zona con sus reasignaciones (@OneToMany)
    @Query("SELECT DISTINCT t FROM Turno t " +
            "JOIN FETCH t.docente " +
            "JOIN FETCH t.zona z " +
            "LEFT JOIN FETCH t.reasignaciones " +
            "WHERE z.id = :zonaId")
    List<Turno> findByZonaIdConReasignaciones(@Param("zonaId") Long zonaId);

    // Turnos de un docente en un rango de fechas con sus incidentes (@OneToMany)
    @Query("SELECT DISTINCT t FROM Turno t " +
            "JOIN FETCH t.zona " +
            "LEFT JOIN FETCH t.incidentes " +
            "WHERE t.docente.id = :docenteId AND t.fecha BETWEEN :desde AND :hasta")
    List<Turno> findByDocenteIdYRangoFecha(@Param("docenteId") Long docenteId,
                                           @Param("desde") LocalDate desde,
                                           @Param("hasta") LocalDate hasta);

    // Turnos por franja con todos sus datos relacionados
    @Query("SELECT DISTINCT t FROM Turno t " +
            "JOIN FETCH t.docente " +
            "JOIN FETCH t.zona " +
            "LEFT JOIN FETCH t.incidentes " +
            "WHERE t.tipoFranja = :franja")
    List<Turno> findByFranjaConRelaciones(@Param("franja") TipoFranja franja);
}