package com.juan.vigidocente.repository;

import com.juan.vigidocente.model.EstadoReasignacion;
import com.juan.vigidocente.model.Reasignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReasignacionRepository extends JpaRepository<Reasignacion, Long> {

    // --- Queries derivados por nombre (ya existían) ---
    List<Reasignacion> findByEstado(EstadoReasignacion estado);
    List<Reasignacion> findByDocenteOriginalId(Long docenteId);
    List<Reasignacion> findByTurnoId(Long turnoId);

    // --- Queries JPQL con @Query (nuevos) ---

    // Carga la reasignación con su Turno, Docente original y Docente reemplazo (@ManyToOne)
    @Query("SELECT r FROM Reasignacion r " +
            "JOIN FETCH r.turno t JOIN FETCH t.zona " +
            "JOIN FETCH r.docenteOriginal " +
            "LEFT JOIN FETCH r.docenteReemplazo " +
            "WHERE r.id = :id")
    Reasignacion findByIdConRelaciones(@Param("id") Long id);

    // Reasignaciones pendientes con toda la info para responder rápido
    @Query("SELECT r FROM Reasignacion r " +
            "JOIN FETCH r.turno t JOIN FETCH t.zona " +
            "JOIN FETCH r.docenteOriginal " +
            "LEFT JOIN FETCH r.docenteReemplazo " +
            "WHERE r.estado = 'PENDIENTE' ORDER BY r.fechaHoraSolicitud ASC")
    List<Reasignacion> findPendientesConRelaciones();

    // Historial completo de reasignaciones de un docente (como original o como reemplazo)
    @Query("SELECT r FROM Reasignacion r " +
            "JOIN FETCH r.turno t JOIN FETCH t.zona " +
            "JOIN FETCH r.docenteOriginal " +
            "LEFT JOIN FETCH r.docenteReemplazo " +
            "WHERE r.docenteOriginal.id = :docenteId OR r.docenteReemplazo.id = :docenteId " +
            "ORDER BY r.fechaHoraSolicitud DESC")
    List<Reasignacion> findHistorialPorDocente(@Param("docenteId") Long docenteId);

    // Reasignaciones de un turno con todos sus docentes cargados
    @Query("SELECT r FROM Reasignacion r " +
            "JOIN FETCH r.docenteOriginal " +
            "LEFT JOIN FETCH r.docenteReemplazo " +
            "WHERE r.turno.id = :turnoId")
    List<Reasignacion> findByTurnoIdConDocentes(@Param("turnoId") Long turnoId);

    // Conteo de reasignaciones por estado (para analítica)
    @Query("SELECT r.estado, COUNT(r) FROM Reasignacion r GROUP BY r.estado")
    List<Object[]> countPorEstado();
}