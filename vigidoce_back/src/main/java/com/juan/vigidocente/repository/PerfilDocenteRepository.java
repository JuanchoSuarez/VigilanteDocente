package com.juan.vigidocente.repository;

import com.juan.vigidocente.model.PerfilDocente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PerfilDocenteRepository extends JpaRepository<PerfilDocente, Long> {

    // --- Query derivado por nombre (ya existía) ---
    Optional<PerfilDocente> findByDocenteId(Long docenteId);

    // --- Queries JPQL con @Query (nuevos) ---

    // Carga el perfil con su Docente (@OneToOne) en una sola consulta
    @Query("SELECT p FROM PerfilDocente p JOIN FETCH p.docente WHERE p.id = :id")
    Optional<PerfilDocente> findByIdConDocente(@Param("id") Long id);

    // Perfil de un docente con todos sus datos (navega @OneToOne)
    @Query("SELECT p FROM PerfilDocente p JOIN FETCH p.docente d WHERE d.id = :docenteId")
    Optional<PerfilDocente> findByDocenteIdConDocente(@Param("docenteId") Long docenteId);

    // Ranking de perfiles por puntos de gamificación con docente cargado
    @Query("SELECT p FROM PerfilDocente p JOIN FETCH p.docente d WHERE d.activo = true ORDER BY p.puntosGamificacion DESC")
    List<PerfilDocente> findRankingConDocente();

    // Perfiles con reconocimiento trimestral asignado
    @Query("SELECT p FROM PerfilDocente p JOIN FETCH p.docente WHERE p.reconocimientoTrimestral IS NOT NULL")
    List<PerfilDocente> findConReconocimiento();
}