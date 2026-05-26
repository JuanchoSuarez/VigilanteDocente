package com.juan.vigidocente.integration.turno;

import com.juan.vigidocente.BaseIntegrationTest;
import com.juan.vigidocente.model.EstadoTurno;
import com.juan.vigidocente.model.Turno;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración PUT → /api/turnos/{id}
 * Verifica:
 *   - Que un ADMINISTRADOR puede actualizar un turno (cambiar estado a EN_CURSO)
 *   - Que un DOCENTE NO puede (autorización por rol)
 */
class TurnoPutIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("PUT /api/turnos/{id} con rol ADMINISTRADOR actualiza el turno (200)")
    void adminActualizaTurno() throws Exception {
        // Cambiamos el estado del turno sembrado
        Turno actualizado = Turno.builder()
                .id(turno.getId())
                .docente(turno.getDocente())
                .zona(turno.getZona())
                .fecha(turno.getFecha())
                .horaInicio(turno.getHoraInicio())
                .horaFin(turno.getHoraFin())
                .estado(EstadoTurno.EN_CURSO)   // ⬅️ cambio
                .tipoFranja(turno.getTipoFranja())
                .build();

        String body = objectMapper.writeValueAsString(actualizado);

        mockMvc.perform(put("/api/turnos/" + turno.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(turno.getId()))
                .andExpect(jsonPath("$.estado").value("EN_CURSO"));
    }

    @Test
    @DisplayName("PUT /api/turnos/{id} con rol DOCENTE devuelve 403 (no autorizado)")
    void docenteNoPuedeActualizarTurno() throws Exception {
        String body = objectMapper.writeValueAsString(turno);

        mockMvc.perform(put("/api/turnos/" + turno.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenDocente))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());
    }
}