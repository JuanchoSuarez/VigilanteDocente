package com.juan.vigidocente.service;

import com.juan.vigidocente.exception.DatosInvalidosException;
import com.juan.vigidocente.exception.RecursoNoEncontradoException;
import com.juan.vigidocente.model.*;
import com.juan.vigidocente.repository.DocenteRepository;
import com.juan.vigidocente.repository.RegistroVigilanciaRepository;
import com.juan.vigidocente.repository.TurnoRepository;
import com.juan.vigidocente.repository.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistroVigilanciaService {

    private final RegistroVigilanciaRepository registroVigilanciaRepository;
    private final TurnoRepository turnoRepository;
    private final DocenteRepository docenteRepository;
    private final ZonaRepository zonaRepository;

    public List<RegistroVigilancia> listarTodos() {
        return registroVigilanciaRepository.findAll();
    }

    public RegistroVigilancia buscarPorId(Long id) {
        return registroVigilanciaRepository.findByIdConRelaciones(id);
    }

    @Transactional
    public RegistroVigilancia guardar(RegistroVigilancia registro) {
        return registroVigilanciaRepository.save(registro);
    }

    @Transactional
    public void eliminar(Long id) {
        registroVigilanciaRepository.deleteById(id);
    }

    public List<RegistroVigilancia> buscarPorDocente(Long docenteId) {
        return registroVigilanciaRepository.findByDocenteIdConTurno(docenteId);
    }

    public List<RegistroVigilancia> buscarPorTurno(Long turnoId) {
        return registroVigilanciaRepository.findByTurnoId(turnoId);
    }

    public List<RegistroVigilancia> buscarPorZonaConDocente(Long zonaId) {
        return registroVigilanciaRepository.findByZonaIdConDocente(zonaId);
    }

    public List<RegistroVigilancia> buscarConRecorridoRealizado() {
        return registroVigilanciaRepository.findConRecorridoRealizado();
    }

    public List<Object[]> obtenerPromedioLimpiezaPorZona() {
        return registroVigilanciaRepository.promedioLimpiezaPorZona();
    }

    @Transactional
    public RegistroVigilancia registrarCheckInQr(Long turnoId, Long docenteId, Long zonaId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado: " + turnoId));
        Docente docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Docente no encontrado: " + docenteId));
        Zona zona = zonaRepository.findById(zonaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Zona no encontrada: " + zonaId));

        RegistroVigilancia registro = RegistroVigilancia.builder()
                .turno(turno)
                .docente(docente)
                .zona(zona)
                .fechaHoraCheckIn(LocalDateTime.now())
                .metodoRegistro(MetodoRegistro.QR)
                .recorridoRealizado(false)
                .build();

        turno.setEstado(EstadoTurno.EN_CURSO);
        turnoRepository.save(turno);

        return registroVigilanciaRepository.save(registro);
    }

    @Transactional
    public RegistroVigilancia confirmarRecorrido(Long registroId) {
        RegistroVigilancia registro = registroVigilanciaRepository.findByIdConRelaciones(registroId);
        if (registro == null) {
            throw new RecursoNoEncontradoException("Registro no encontrado: " + registroId);
        }
        registro.setRecorridoRealizado(true);
        return registroVigilanciaRepository.save(registro);
    }

    @Transactional
    public RegistroVigilancia registrarCheckOut(Long registroId, Integer calificacionLimpieza) {
        RegistroVigilancia registro = registroVigilanciaRepository.findByIdConRelaciones(registroId);
        if (registro == null) {
            throw new RecursoNoEncontradoException("Registro no encontrado: " + registroId);
        }
        if (calificacionLimpieza != null && (calificacionLimpieza < 1 || calificacionLimpieza > 4)) {
            throw new DatosInvalidosException("La calificación de limpieza debe estar entre 1 y 4");
        }
        registro.setFechaHoraCheckOut(LocalDateTime.now());
        registro.setCalificacionLimpieza(calificacionLimpieza);

        Turno turno = registro.getTurno();
        turno.setEstado(EstadoTurno.CERRADO);
        turnoRepository.save(turno);

        return registroVigilanciaRepository.save(registro);
    }
}