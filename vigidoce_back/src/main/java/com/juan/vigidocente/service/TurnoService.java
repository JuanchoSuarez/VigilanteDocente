package com.juan.vigidocente.service;

import com.juan.vigidocente.model.EstadoTurno;
import com.juan.vigidocente.model.TipoFranja;
import com.juan.vigidocente.model.Turno;
import com.juan.vigidocente.model.FranjaHorario;
import com.juan.vigidocente.model.DiaSemana;
import com.juan.vigidocente.repository.TurnoRepository;
import com.juan.vigidocente.repository.FranjaHorarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final FranjaHorarioRepository franjaHorarioRepository;

    public List<Turno> listarTodos() {
        return turnoRepository.findAll();
    }

    public Turno buscarPorId(Long id) {
        return turnoRepository.findByIdConRelaciones(id);
    }

    @Transactional
    public Turno guardar(Turno turno) {
        return turnoRepository.save(turno);
    }

    @Transactional
    public void eliminar(Long id) {
        turnoRepository.deleteById(id);
    }

    public List<Turno> buscarPorFecha(LocalDate fecha) {
        return turnoRepository.findByFecha(fecha);
    }

    public List<Turno> buscarPorEstado(EstadoTurno estado) {
        return turnoRepository.findByEstado(estado);
    }

    public List<Turno> buscarPorDocente(Long docenteId) {
        return turnoRepository.findByDocenteId(docenteId);
    }

    public List<Turno> buscarPorDocenteYFecha(Long docenteId, LocalDate fecha) {
        return turnoRepository.findByDocenteIdAndFecha(docenteId, fecha);
    }

    public List<Turno> buscarPorZonaConReasignaciones(Long zonaId) {
        return turnoRepository.findByZonaIdConReasignaciones(zonaId);
    }

    public List<Turno> buscarPendientesHoy() {
        return turnoRepository.findTurnosPendientesHoyConRegistros();
    }

    public List<Turno> buscarPorDocenteYRango(Long docenteId, LocalDate desde, LocalDate hasta) {
        return turnoRepository.findByDocenteIdYRangoFecha(docenteId, desde, hasta);
    }

    public List<Turno> buscarPorFranja(TipoFranja franja) {
        return turnoRepository.findByFranjaConRelaciones(franja);
    }

    @Transactional
    public List<Turno> generarDesdeHorario() {
        LocalDate hoy = LocalDate.now();
        String diaStr = hoy.getDayOfWeek().name();
        DiaSemana diaSemana = switch (diaStr) {
            case "MONDAY" -> DiaSemana.LUNES;
            case "TUESDAY" -> DiaSemana.MARTES;
            case "WEDNESDAY" -> DiaSemana.MIERCOLES;
            case "THURSDAY" -> DiaSemana.JUEVES;
            case "FRIDAY" -> DiaSemana.VIERNES;
            case "SATURDAY" -> DiaSemana.SABADO;
            case "SUNDAY" -> DiaSemana.DOMINGO;
            default -> DiaSemana.LUNES;
        };

        List<FranjaHorario> franjas = franjaHorarioRepository.findByDiaSemanaAndActivo(diaSemana, true);
        java.util.List<Turno> creados = new java.util.ArrayList<>();

        for (FranjaHorario f : franjas) {
            boolean existe = turnoRepository.existsByDocenteIdAndZonaIdAndFechaAndHoraInicio(
                    f.getDocente().getId(), f.getZona().getId(), hoy, f.getHoraInicio()
            );

            if (!existe) {
                Turno t = Turno.builder()
                        .docente(f.getDocente())
                        .zona(f.getZona())
                        .fecha(hoy)
                        .horaInicio(f.getHoraInicio())
                        .horaFin(f.getHoraFin())
                        .tipoFranja(f.getTipoFranja())
                        .estado(EstadoTurno.PENDIENTE)
                        .build();
                creados.add(turnoRepository.save(t));
            }
        }
        return creados;
    }
}