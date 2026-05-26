package com.juan.vigidocente.integration.reasignacion;

import com.juan.vigidocente.BaseIntegrationTest;
import com.juan.vigidocente.model.EstadoReasignacion;
import com.juan.vigidocente.model.Reasignacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Caminos adicionales del flujo de reasignación:
 * - Solicitar (POST /solicitar)
 * - Rechazar (PATCH /{id}/rechazar)
 * - Intentar aceptar una ya aceptada (estado inválido)
 * - Eliminar con rol DOCENTE (prohibido) vs COORDINADOR (permitido)
 */
class ReasignacionFlujoIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("POST /api/reasignaciones/solicitar con DOCENTE crea reasignación PENDIENTE (201)")
    void docenteSolicitaReasignacion() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "turnoId",   turno.getId(),
                "docenteId", docente.getId(),
                "motivo",    "Cita médica"
        ));

        mockMvc.perform(post("/api/reasignaciones/solicitar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenDocente))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.motivo").value("Cita médica"));
    }

    @Test
    @DisplayName("PATCH /api/reasignaciones/{id}/rechazar con COORDINADOR cambia estado a RECHAZADA")
    void coordinadorRechazaReasignacion() throws Exception {
        mockMvc.perform(patch("/api/reasignaciones/" + reasignacion.getId() + "/rechazar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenCoord))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));

        Reasignacion enBd = reasignacionRepository.findById(reasignacion.getId()).orElseThrow();
        assertEquals(EstadoReasignacion.RECHAZADA, enBd.getEstado());
        assertNotNull(enBd.getFechaHoraRespuesta());
    }

    @Test
    @DisplayName("PATCH aceptar una reasignación ya ACEPTADA devuelve 4xx (estado inválido)")    void noSePuedeAceptarDosVeces() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "docenteReemplazoId", admin.getId()
        ));

        // Primera aceptación → OK
        mockMvc.perform(patch("/api/reasignaciones/" + reasignacion.getId() + "/aceptar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenCoord))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());

        // Segunda → falla porque ya no está PENDIENTE
        // El service lanza DatosInvalidosException; sin un @ControllerAdvice se traduce a 500.
        mockMvc.perform(patch("/api/reasignaciones/" + reasignacion.getId() + "/aceptar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenCoord))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("DELETE /api/reasignaciones/{id} con rol DOCENTE devuelve 403")
    void docenteNoPuedeEliminarReasignacion() throws Exception {
        mockMvc.perform(delete("/api/reasignaciones/" + reasignacion.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenDocente)))
                .andExpect(status().isForbidden());

        assertTrue(reasignacionRepository.existsById(reasignacion.getId()));
    }

    @Test
    @DisplayName("DELETE /api/reasignaciones/{id} con rol COORDINADOR elimina (204)")
    void coordinadorEliminaReasignacion() throws Exception {
        mockMvc.perform(delete("/api/reasignaciones/" + reasignacion.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenCoord)))
                .andExpect(status().isNoContent());

        assertFalse(reasignacionRepository.existsById(reasignacion.getId()));
    }
}