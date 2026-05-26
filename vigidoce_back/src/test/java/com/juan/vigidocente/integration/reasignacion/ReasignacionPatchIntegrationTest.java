package com.juan.vigidocente.integration.reasignacion;

import com.juan.vigidocente.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración PATCH → /api/reasignaciones/{id}/aceptar
 * Endpoint complejo: cambia el estado de la reasignación a ACEPTADA
 * y asigna al docente reemplazo. Solo COORDINADOR o ADMINISTRADOR pueden hacerlo.
 */
class ReasignacionPatchIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("PATCH /api/reasignaciones/{id}/aceptar con rol COORDINADOR acepta la reasignación (200)")
    void coordinadorAceptaReasignacion() throws Exception {
        // El coordinador acepta y asigna al "admin" (puede ser cualquier docente)
        String body = objectMapper.writeValueAsString(Map.of(
                "docenteReemplazoId", admin.getId()
        ));

        mockMvc.perform(patch("/api/reasignaciones/" + reasignacion.getId() + "/aceptar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenCoord))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reasignacion.getId()))
                .andExpect(jsonPath("$.estado").value("ACEPTADA"))
                .andExpect(jsonPath("$.docenteReemplazo.id").value(admin.getId()));
    }

    @Test
    @DisplayName("PATCH /api/reasignaciones/{id}/aceptar con rol DOCENTE devuelve 403")
    void docenteNoPuedeAceptarReasignacion() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "docenteReemplazoId", admin.getId()
        ));

        mockMvc.perform(patch("/api/reasignaciones/" + reasignacion.getId() + "/aceptar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenDocente))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());
    }
}