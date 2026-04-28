package com.juan.vigidocente.repository;

import com.juan.vigidocente.model.TipoZona;
import com.juan.vigidocente.model.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ZonaRepository extends JpaRepository<Zona, Long> {

    // --- Queries derivados por nombre (ya existían) ---
    List<Zona> findByActiva(Boolean activa);
    List<Zona> findByTipo(TipoZona tipo);

    // --- Queries JPQL con @Query (nuevos) ---

    // Zonas activas con sus turnos cargados (navega la relación @OneToMany)
    @Query("SELECT DISTINCT z FROM Zona z LEFT JOIN FETCH z.turnos WHERE z.activa = true")
    List<Zona> findActivasConTurnos();

    // Zonas que tienen al menos un turno pendiente hoy
    @Query("SELECT DISTINCT z FROM Zona z JOIN z.turnos t WHERE t.estado = 'PENDIENTE' AND t.fecha = CURRENT_DATE")
    List<Zona> findZonasConTurnosPendientesHoy();

    // Zonas por tipo con sus turnos cargados
    @Query("SELECT DISTINCT z FROM Zona z LEFT JOIN FETCH z.turnos WHERE z.tipo = :tipo")
    List<Zona> findByTipoConTurnos(@Param("tipo") TipoZona tipo);

    // Conteo de turnos por zona (para analítica)
    @Query("SELECT z.nombre, COUNT(t) FROM Zona z LEFT JOIN z.turnos t GROUP BY z.id, z.nombre")
    List<Object[]> countTurnosPorZona();
}