package com.juan.vigidocente.batch;

import com.juan.vigidocente.model.*;
import com.juan.vigidocente.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final DocenteRepository docenteRepository;
    private final ZonaRepository zonaRepository;
    private final TurnoRepository turnoRepository;
    private final IncidenteRepository incidenteRepository;
    private final ReasignacionRepository reasignacionRepository;
    private final RegistroVigilanciaRepository registroVigilanciaRepository;
    private final PerfilDocenteRepository perfilDocenteRepository;
    private final HorarioRepository horarioRepository;
    private final FranjaHorarioRepository franjaHorarioRepository;

    @Override
    public void run(String... args) throws Exception {

        if (docenteRepository.count() == 0) {

        System.out.println("🚀 Iniciando carga de datos...");

        // --- DOCENTES ---
        System.out.println("📋 Cargando docentes...");
        Docente d1 = docenteRepository.save(Docente.builder()
                .nombre("Carlos").apellido("García")
                .email("carlos.garcia@colegio.edu.co").password("1234")
                .telefono("3001234567").rol(RolDocente.DOCENTE).activo(true).build());

        Docente d2 = docenteRepository.save(Docente.builder()
                .nombre("María").apellido("López")
                .email("maria.lopez@colegio.edu.co").password("1234")
                .telefono("3009876543").rol(RolDocente.DOCENTE).activo(true).build());

        Docente d3 = docenteRepository.save(Docente.builder()
                .nombre("Andrés").apellido("Martínez")
                .email("andres.martinez@colegio.edu.co").password("1234")
                .telefono("3105551234").rol(RolDocente.COORDINADOR).activo(true).build());

        Docente d4 = docenteRepository.save(Docente.builder()
                .nombre("Laura").apellido("Rodríguez")
                .email("laura.rodriguez@colegio.edu.co").password("1234")
                .telefono("3207654321").rol(RolDocente.DOCENTE).activo(true).build());

        Docente d5 = docenteRepository.save(Docente.builder()
                .nombre("Juan").apellido("Pérez")
                .email("juan.perez@colegio.edu.co").password("1234")
                .telefono("3154443322").rol(RolDocente.ADMINISTRADOR).activo(true).build());

        Docente d6 = docenteRepository.save(Docente.builder()
                .nombre("Sofía").apellido("Torres")
                .email("sofia.torres@colegio.edu.co").password("1234")
                .telefono("3112223344").rol(RolDocente.DOCENTE).activo(true).build());

        Docente d7 = docenteRepository.save(Docente.builder()
                .nombre("Miguel").apellido("Herrera")
                .email("miguel.herrera@colegio.edu.co").password("1234")
                .telefono("3223334455").rol(RolDocente.DOCENTE).activo(true).build());

        System.out.println("✅ " + docenteRepository.count() + " docentes cargados");

        // --- PERFILES DOCENTE (OneToOne) ---
        System.out.println("📋 Cargando perfiles docente...");
        perfilDocenteRepository.save(PerfilDocente.builder()
                .docente(d1).especialidad("Matemáticas")
                .aniosExperiencia(8).puntosGamificacion(150)
                .reconocimientoTrimestral("Mejor puntualidad Q1").build());

        perfilDocenteRepository.save(PerfilDocente.builder()
                .docente(d2).especialidad("Ciencias Naturales")
                .aniosExperiencia(5).puntosGamificacion(120)
                .reconocimientoTrimestral(null).build());

        perfilDocenteRepository.save(PerfilDocente.builder()
                .docente(d3).especialidad("Coordinación Académica")
                .aniosExperiencia(12).puntosGamificacion(200)
                .reconocimientoTrimestral("Mejor gestión Q1").build());

        perfilDocenteRepository.save(PerfilDocente.builder()
                .docente(d4).especialidad("Lengua Castellana")
                .aniosExperiencia(3).puntosGamificacion(90)
                .reconocimientoTrimestral(null).build());

        perfilDocenteRepository.save(PerfilDocente.builder()
                .docente(d5).especialidad("Administración")
                .aniosExperiencia(15).puntosGamificacion(180)
                .reconocimientoTrimestral("Mayor contribución preventiva").build());

        perfilDocenteRepository.save(PerfilDocente.builder()
                .docente(d6).especialidad("Educación Física")
                .aniosExperiencia(6).puntosGamificacion(110)
                .reconocimientoTrimestral(null).build());

        perfilDocenteRepository.save(PerfilDocente.builder()
                .docente(d7).especialidad("Historia")
                .aniosExperiencia(4).puntosGamificacion(75)
                .reconocimientoTrimestral(null).build());

        System.out.println("✅ " + perfilDocenteRepository.count() + " perfiles cargados");

        // --- ZONAS ---
        System.out.println("📋 Cargando zonas...");
        Zona z1 = zonaRepository.save(Zona.builder()
                .nombre("Patio Central").descripcion("Zona principal de recreo")
                .tipo(TipoZona.PATIO).capacidadMaxima(200).activa(true).build());

        Zona z2 = zonaRepository.save(Zona.builder()
                .nombre("Cafetería").descripcion("Zona de almuerzo estudiantil")
                .tipo(TipoZona.CAFETERIA).capacidadMaxima(150).activa(true).build());

        Zona z3 = zonaRepository.save(Zona.builder()
                .nombre("Corredor Norte").descripcion("Corredor entre bloques A y B")
                .tipo(TipoZona.CORREDOR).capacidadMaxima(80).activa(true).build());

        Zona z4 = zonaRepository.save(Zona.builder()
                .nombre("Entrada Principal").descripcion("Acceso principal al colegio")
                .tipo(TipoZona.ENTRADA).capacidadMaxima(100).activa(true).build());

        Zona z5 = zonaRepository.save(Zona.builder()
                .nombre("Patio Sur").descripcion("Zona de recreo secundaria")
                .tipo(TipoZona.PATIO).capacidadMaxima(120).activa(true).build());

        Zona z6 = zonaRepository.save(Zona.builder()
                .nombre("Corredor Sur").descripcion("Corredor bloque C")
                .tipo(TipoZona.CORREDOR).capacidadMaxima(60).activa(true).build());

        System.out.println("✅ " + zonaRepository.count() + " zonas cargadas");

        // --- HORARIOS Y FRANJAS ---
        System.out.println("📋 Cargando horarios y franjas...");
        Horario horarioPrincipal = horarioRepository.save(Horario.builder()
                .nombre("Cronograma Semanal 2026")
                .descripcion("Horario general de vigilancia")
                .activo(true)
                .build());

        String diaStr = LocalDate.now().getDayOfWeek().name();
        DiaSemana diaHoy = switch (diaStr) {
            case "MONDAY" -> DiaSemana.LUNES;
            case "TUESDAY" -> DiaSemana.MARTES;
            case "WEDNESDAY" -> DiaSemana.MIERCOLES;
            case "THURSDAY" -> DiaSemana.JUEVES;
            case "FRIDAY" -> DiaSemana.VIERNES;
            case "SATURDAY" -> DiaSemana.SABADO;
            case "SUNDAY" -> DiaSemana.DOMINGO;
            default -> DiaSemana.LUNES;
        };

        franjaHorarioRepository.save(FranjaHorario.builder()
                .horario(horarioPrincipal).docente(d1).zona(z1)
                .diaSemana(diaHoy).horaInicio(LocalTime.of(10, 0)).horaFin(LocalTime.of(10, 30))
                .tipoFranja(TipoFranja.RECREO).activo(true).build());

        franjaHorarioRepository.save(FranjaHorario.builder()
                .horario(horarioPrincipal).docente(d2).zona(z2)
                .diaSemana(diaHoy).horaInicio(LocalTime.of(12, 0)).horaFin(LocalTime.of(13, 0))
                .tipoFranja(TipoFranja.ALMUERZO).activo(true).build());

        franjaHorarioRepository.save(FranjaHorario.builder()
                .horario(horarioPrincipal).docente(d4).zona(z3)
                .diaSemana(diaHoy).horaInicio(LocalTime.of(10, 0)).horaFin(LocalTime.of(10, 30))
                .tipoFranja(TipoFranja.RECREO).activo(true).build());

        franjaHorarioRepository.save(FranjaHorario.builder()
                .horario(horarioPrincipal).docente(d6).zona(z4)
                .diaSemana(diaHoy).horaInicio(LocalTime.of(12, 0)).horaFin(LocalTime.of(13, 0))
                .tipoFranja(TipoFranja.ALMUERZO).activo(true).build());

        System.out.println("✅ " + franjaHorarioRepository.count() + " franjas cargadas");

        // --- TURNOS ---
        System.out.println("📋 Cargando turnos...");
        Turno t1 = turnoRepository.save(Turno.builder()
                .docente(d1).zona(z1).fecha(LocalDate.now().minusDays(2))
                .horaInicio(LocalTime.of(10, 0)).horaFin(LocalTime.of(10, 30))
                .estado(EstadoTurno.CERRADO).tipoFranja(TipoFranja.RECREO).build());

        Turno t2 = turnoRepository.save(Turno.builder()
                .docente(d2).zona(z2).fecha(LocalDate.now().minusDays(2))
                .horaInicio(LocalTime.of(12, 0)).horaFin(LocalTime.of(13, 0))
                .estado(EstadoTurno.CERRADO).tipoFranja(TipoFranja.ALMUERZO).build());

        Turno t3 = turnoRepository.save(Turno.builder()
                .docente(d4).zona(z3).fecha(LocalDate.now().minusDays(1))
                .horaInicio(LocalTime.of(10, 0)).horaFin(LocalTime.of(10, 30))
                .estado(EstadoTurno.CERRADO).tipoFranja(TipoFranja.RECREO).build());

        Turno t4 = turnoRepository.save(Turno.builder()
                .docente(d6).zona(z4).fecha(LocalDate.now().minusDays(1))
                .horaInicio(LocalTime.of(12, 0)).horaFin(LocalTime.of(13, 0))
                .estado(EstadoTurno.CERRADO).tipoFranja(TipoFranja.ALMUERZO).build());

        Turno t5 = turnoRepository.save(Turno.builder()
                .docente(d1).zona(z5).fecha(LocalDate.now())
                .horaInicio(LocalTime.of(10, 0)).horaFin(LocalTime.of(10, 30))
                .estado(EstadoTurno.PENDIENTE).tipoFranja(TipoFranja.RECREO).build());

        Turno t6 = turnoRepository.save(Turno.builder()
                .docente(d7).zona(z2).fecha(LocalDate.now())
                .horaInicio(LocalTime.of(12, 0)).horaFin(LocalTime.of(13, 0))
                .estado(EstadoTurno.PENDIENTE).tipoFranja(TipoFranja.ALMUERZO).build());

        Turno t7 = turnoRepository.save(Turno.builder()
                .docente(d3).zona(z6).fecha(LocalDate.now().plusDays(1))
                .horaInicio(LocalTime.of(10, 0)).horaFin(LocalTime.of(10, 30))
                .estado(EstadoTurno.PENDIENTE).tipoFranja(TipoFranja.RECREO).build());

        Turno t8 = turnoRepository.save(Turno.builder()
                .docente(d2).zona(z1).fecha(LocalDate.now().plusDays(1))
                .horaInicio(LocalTime.of(12, 0)).horaFin(LocalTime.of(13, 0))
                .estado(EstadoTurno.PENDIENTE).tipoFranja(TipoFranja.ALMUERZO).build());

        System.out.println("✅ " + turnoRepository.count() + " turnos cargados");

        // --- INCIDENTES ---
        System.out.println("📋 Cargando incidentes...");
        incidenteRepository.save(Incidente.builder()
                .turno(t1).zona(z1)
                .tipo(TipoIncidente.SEGURIDAD_FISICA)
                .severidad(SeveridadIncidente.S1_LEVE)
                .descripcion("Estudiante cayó durante el recreo, sin lesiones graves")
                .fechaHora(LocalDateTime.now().minusDays(2).withHour(10).withMinute(15))
                .build());

        incidenteRepository.save(Incidente.builder()
                .turno(t2).zona(z2)
                .tipo(TipoIncidente.CONVIVENCIA)
                .severidad(SeveridadIncidente.S2_SEGUIMIENTO)
                .descripcion("Conflicto verbal entre estudiantes en cafetería")
                .fechaHora(LocalDateTime.now().minusDays(2).withHour(12).withMinute(30))
                .observacionEstudiante("Curso 8B").build());

        incidenteRepository.save(Incidente.builder()
                .turno(t3).zona(z3)
                .tipo(TipoIncidente.USO_ESPACIO)
                .severidad(SeveridadIncidente.S1_LEVE)
                .descripcion("Mal uso de mobiliario en corredor norte")
                .fechaHora(LocalDateTime.now().minusDays(1).withHour(10).withMinute(20))
                .build());

        incidenteRepository.save(Incidente.builder()
                .turno(t4).zona(z4)
                .tipo(TipoIncidente.SEGURIDAD_FISICA)
                .severidad(SeveridadIncidente.S3_ATENCION_INMEDIATA)
                .descripcion("Estudiante con golpe fuerte requiere enfermería")
                .fechaHora(LocalDateTime.now().minusDays(1).withHour(12).withMinute(45))
                .observacionEstudiante("Curso 10A").build());

        incidenteRepository.save(Incidente.builder()
                .turno(t1).zona(z1)
                .tipo(TipoIncidente.OBSERVACION_SOCIAL)
                .severidad(SeveridadIncidente.S2_SEGUIMIENTO)
                .descripcion("Estudiante aislado del grupo, posible situación de exclusión")
                .fechaHora(LocalDateTime.now().minusDays(2).withHour(10).withMinute(25))
                .observacionEstudiante("Curso 7C").build());

        System.out.println("✅ " + incidenteRepository.count() + " incidentes cargados");

        // --- REGISTROS DE VIGILANCIA ---
        System.out.println("📋 Cargando registros de vigilancia...");
        registroVigilanciaRepository.save(RegistroVigilancia.builder()
                .turno(t1).docente(d1).zona(z1)
                .fechaHoraCheckIn(LocalDateTime.now().minusDays(2).withHour(10).withMinute(2))
                .fechaHoraCheckOut(LocalDateTime.now().minusDays(2).withHour(10).withMinute(30))
                .metodoRegistro(MetodoRegistro.QR)
                .recorridoRealizado(true).calificacionLimpieza(2).build());

        registroVigilanciaRepository.save(RegistroVigilancia.builder()
                .turno(t2).docente(d2).zona(z2)
                .fechaHoraCheckIn(LocalDateTime.now().minusDays(2).withHour(12).withMinute(5))
                .fechaHoraCheckOut(LocalDateTime.now().minusDays(2).withHour(13).withMinute(0))
                .metodoRegistro(MetodoRegistro.PIN)
                .recorridoRealizado(true).calificacionLimpieza(1).build());

        registroVigilanciaRepository.save(RegistroVigilancia.builder()
                .turno(t3).docente(d4).zona(z3)
                .fechaHoraCheckIn(LocalDateTime.now().minusDays(1).withHour(10).withMinute(8))
                .fechaHoraCheckOut(LocalDateTime.now().minusDays(1).withHour(10).withMinute(30))
                .metodoRegistro(MetodoRegistro.QR)
                .recorridoRealizado(false).calificacionLimpieza(3).build());

        registroVigilanciaRepository.save(RegistroVigilancia.builder()
                .turno(t4).docente(d6).zona(z4)
                .fechaHoraCheckIn(LocalDateTime.now().minusDays(1).withHour(12).withMinute(1))
                .fechaHoraCheckOut(LocalDateTime.now().minusDays(1).withHour(13).withMinute(0))
                .metodoRegistro(MetodoRegistro.NFC)
                .recorridoRealizado(true).calificacionLimpieza(1).build());

        System.out.println("✅ " + registroVigilanciaRepository.count() + " registros cargados");

        // --- REASIGNACIONES ---
        System.out.println("📋 Cargando reasignaciones...");
        reasignacionRepository.save(Reasignacion.builder()
                .turno(t5).docenteOriginal(d1).docenteReemplazo(d6)
                .motivo("Incapacidad médica")
                .fechaHoraSolicitud(LocalDateTime.now().minusHours(5))
                .fechaHoraRespuesta(LocalDateTime.now().minusHours(4))
                .estado(EstadoReasignacion.ACEPTADA).build());

        reasignacionRepository.save(Reasignacion.builder()
                .turno(t6).docenteOriginal(d7).docenteReemplazo(null)
                .motivo("Reunión institucional urgente")
                .fechaHoraSolicitud(LocalDateTime.now().minusHours(2))
                .fechaHoraRespuesta(null)
                .estado(EstadoReasignacion.PENDIENTE).build());

        reasignacionRepository.save(Reasignacion.builder()
                .turno(t3).docenteOriginal(d4).docenteReemplazo(d2)
                .motivo("Diligencia personal")
                .fechaHoraSolicitud(LocalDateTime.now().minusDays(1).withHour(9).withMinute(0))
                .fechaHoraRespuesta(LocalDateTime.now().minusDays(1).withHour(9).withMinute(30))
                .estado(EstadoReasignacion.ACEPTADA).build());

        reasignacionRepository.save(Reasignacion.builder()
                .turno(t7).docenteOriginal(d3).docenteReemplazo(d7)
                .motivo("Actividad académica")
                .fechaHoraSolicitud(LocalDateTime.now().withHour(8).withMinute(0))
                .fechaHoraRespuesta(LocalDateTime.now().withHour(8).withMinute(45))
                .estado(EstadoReasignacion.RECHAZADA).build());

        System.out.println("✅ " + reasignacionRepository.count() + " reasignaciones cargadas");

        System.out.println("🎉 ¡Carga completa de datos finalizada exitosamente!");
        }

        // --- PASSWORD UPDATE (if empty) ---
        for (Docente d : docenteRepository.findAll()) {
            if (d.getPassword() == null || d.getPassword().isEmpty()) {
                d.setPassword("1234");
                docenteRepository.save(d);
            }
        }
    }
}