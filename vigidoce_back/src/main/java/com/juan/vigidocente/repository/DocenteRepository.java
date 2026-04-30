package com.juan.vigidocente.repository;

import com.juan.vigidocente.model.Docente;
import com.juan.vigidocente.model.RolDocente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocenteRepository extends JpaRepository<Docente, Long> {

    // --- Queries derivados por nombre (ya existían) ---
    List<Docente> findByRol(RolDocente rol);
    List<Docente> findByActivo(Boolean activo);
    java.util.Optional<Docente> findByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);

    // --- Queries JPQL con @Query (nuevos) ---

    // Carga el docente con su perfil (@OneToOne) en una sola consulta
    @Query("SELECT d FROM Docente d LEFT JOIN FETCH d.perfil WHERE d.id = :id")
    Docente findByIdConPerfil(@Param("id") Long id);

    // Docentes activos con sus turnos (@OneToMany)
    @Query("SELECT DISTINCT d FROM Docente d LEFT JOIN FETCH d.turnos WHERE d.activo = true")
    List<Docente> findActivosConTurnos();

    // Docentes de un rol con su perfil (@OneToOne)
    @Query("SELECT d FROM Docente d LEFT JOIN FETCH d.perfil WHERE d.rol = :rol AND d.activo = true")
    List<Docente> findByRolConPerfil(@Param("rol") RolDocente rol);

    // Docentes con sus registros de vigilancia (@OneToMany)
    @Query("SELECT DISTINCT d FROM Docente d LEFT JOIN FETCH d.registrosVigilancia WHERE d.id = :id")
    Docente findByIdConRegistros(@Param("id") Long id);

    // Ranking de docentes por puntos de gamificación (navega @OneToOne hacia PerfilDocente)
    @Query("SELECT d FROM Docente d JOIN FETCH d.perfil p WHERE d.activo = true ORDER BY p.puntosGamificacion DESC")
    List<Docente> findRankingGamificacion();
}