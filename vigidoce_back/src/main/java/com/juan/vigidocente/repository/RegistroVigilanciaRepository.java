package com.juan.vigidocente.repository;

import com.juan.vigidocente.model.RegistroVigilancia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegistroVigilanciaRepository extends JpaRepository<RegistroVigilancia, Long> {

    // --- Queries derivados por nombre (ya existían) ---
    List<RegistroVigilancia> findByDocenteId(Long docenteId);
    List<RegistroVigilancia> findByTurnoId(Long turnoId);
    List<RegistroVigilancia> findByZonaId(Long zonaId);

    // --- Queries JPQL con @Query (nuevos) ---

    // Carga el registro con su Turno, Docente y Zona (@ManyToOne) en una sola consulta
    @Query("SELECT r FROM RegistroVigilancia r " +
            "JOIN FETCH r.turno JOIN FETCH r.docente JOIN FETCH r.zona " +
            "WHERE r.id = :id")
    RegistroVigilancia findByIdConRelaciones(@Param("id") Long id);

    // Registros de un docente con el turno y zona cargados
    @Query("SELECT r FROM RegistroVigilancia r " +
            "JOIN FETCH r.turno t JOIN FETCH r.zona " +
            "WHERE r.docente.id = :docenteId ORDER BY r.fechaHoraCheckIn DESC")
    List<RegistroVigilancia> findByDocenteIdConTurno(@Param("docenteId") Long docenteId);

    // Registros donde el docente SÍ hizo el recorrido (para gamificación)
    @Query("SELECT r FROM RegistroVigilancia r " +
            "JOIN FETCH r.docente JOIN FETCH r.zona " +
            "WHERE r.recorridoRealizado = true")
    List<RegistroVigilancia> findConRecorridoRealizado();

    // Registros de una zona con su docente y turno (para el tablero del coordinador)
    @Query("SELECT r FROM RegistroVigilancia r " +
            "JOIN FETCH r.docente JOIN FETCH r.turno " +
            "WHERE r.zona.id = :zonaId ORDER BY r.fechaHoraCheckIn DESC")
    List<RegistroVigilancia> findByZonaIdConDocente(@Param("zonaId") Long zonaId);

    // Promedio de calificación de limpieza por zona (para analítica)
    @Query("SELECT r.zona.nombre, AVG(r.calificacionLimpieza) FROM RegistroVigilancia r " +
            "WHERE r.calificacionLimpieza IS NOT NULL GROUP BY r.zona.nombre")
    List<Object[]> promedioLimpiezaPorZona();
}