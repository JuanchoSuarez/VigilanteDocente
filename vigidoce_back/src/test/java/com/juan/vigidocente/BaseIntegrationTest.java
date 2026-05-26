package com.juan.vigidocente;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.juan.vigidocente.model.*;
import com.juan.vigidocente.repository.*;
import com.juan.vigidocente.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Clase base para todos los tests de integración.
 *
 * - Levanta el contexto completo de Spring Boot con perfil "test" (usa H2 en memoria).
 * - Provee un MockMvc para simular requests HTTP sin levantar un servidor real.
 * - Antes de cada test, limpia la BD y siembra datos consistentes:
 *     - 1 admin, 1 coordinador, 1 docente (con contraseña BCrypt "1234")
 *     - 1 zona, 1 turno, 1 incidente, 1 reasignación pendiente
 * - Expone tokens JWT válidos para cada rol (tokenAdmin / tokenCoord / tokenDocente)
 *   para que cada test pueda hacer peticiones autenticadas con el rol que necesite.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected JwtService jwtService;
    @Autowired protected PasswordEncoder passwordEncoder;

    @Autowired protected DocenteRepository docenteRepository;
    @Autowired protected ZonaRepository zonaRepository;
    @Autowired protected TurnoRepository turnoRepository;
    @Autowired protected IncidenteRepository incidenteRepository;
    @Autowired protected ReasignacionRepository reasignacionRepository;
    @Autowired protected RegistroVigilanciaRepository registroVigilanciaRepository;
    @Autowired protected PerfilDocenteRepository perfilDocenteRepository;
    @Autowired protected HorarioRepository horarioRepository;
    @Autowired protected FranjaHorarioRepository franjaHorarioRepository;

    // Entidades sembradas, accesibles desde los tests hijos
    protected Docente admin;
    protected Docente coordinador;
    protected Docente docente;
    protected Zona zona;
    protected Turno turno;
    protected Incidente incidente;
    protected Reasignacion reasignacion;

    // Tokens JWT por rol
    protected String tokenAdmin;
    protected String tokenCoord;
    protected String tokenDocente;

    @BeforeEach
    protected void setUpDatosDePrueba() {
        // Limpieza en orden (hijos antes que padres por FK)
        reasignacionRepository.deleteAll();
        registroVigilanciaRepository.deleteAll();
        incidenteRepository.deleteAll();
        franjaHorarioRepository.deleteAll();
        turnoRepository.deleteAll();
        horarioRepository.deleteAll();
        perfilDocenteRepository.deleteAll();
        zonaRepository.deleteAll();
        docenteRepository.deleteAll();

        // === DOCENTES (uno por rol) ===
        admin = docenteRepository.save(Docente.builder()
                .nombre("Admin").apellido("Test")
                .email("admin@test.com")
                .password(passwordEncoder.encode("1234"))
                .telefono("3000000001")
                .rol(RolDocente.ADMINISTRADOR)
                .activo(true)
                .build());

        coordinador = docenteRepository.save(Docente.builder()
                .nombre("Coord").apellido("Test")
                .email("coord@test.com")
                .password(passwordEncoder.encode("1234"))
                .telefono("3000000002")
                .rol(RolDocente.COORDINADOR)
                .activo(true)
                .build());

        docente = docenteRepository.save(Docente.builder()
                .nombre("Docente").apellido("Test")
                .email("docente@test.com")
                .password(passwordEncoder.encode("1234"))
                .telefono("3000000003")
                .rol(RolDocente.DOCENTE)
                .activo(true)
                .build());

        // === ZONA ===
        zona = zonaRepository.save(Zona.builder()
                .nombre("Patio Test")
                .descripcion("Zona para tests")
                .tipo(TipoZona.PATIO)
                .capacidadMaxima(100)
                .activa(true)
                .build());

        // === TURNO ===
        turno = turnoRepository.save(Turno.builder()
                .docente(docente)
                .zona(zona)
                .fecha(LocalDate.now())
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(10, 30))
                .estado(EstadoTurno.PENDIENTE)
                .tipoFranja(TipoFranja.RECREO)
                .build());

        // === INCIDENTE ===
        incidente = incidenteRepository.save(Incidente.builder()
                .turno(turno)
                .zona(zona)
                .tipo(TipoIncidente.SEGURIDAD_FISICA)
                .severidad(SeveridadIncidente.S1_LEVE)
                .descripcion("Incidente de prueba")
                .fechaHora(LocalDateTime.now())
                .build());

        // === REASIGNACIÓN (pendiente, lista para que un coord la acepte) ===
        reasignacion = reasignacionRepository.save(Reasignacion.builder()
                .turno(turno)
                .docenteOriginal(docente)
                .docenteReemplazo(null)
                .motivo("Motivo de prueba")
                .fechaHoraSolicitud(LocalDateTime.now())
                .estado(EstadoReasignacion.PENDIENTE)
                .build());

        // === TOKENS JWT ===
        tokenAdmin   = jwtService.generateToken(admin);
        tokenCoord   = jwtService.generateToken(coordinador);
        tokenDocente = jwtService.generateToken(docente);
    }

    /** Helper: construye el header "Bearer ..." para pasar al MockMvc */
    protected String bearer(String token) {
        return "Bearer " + token;
    }
}