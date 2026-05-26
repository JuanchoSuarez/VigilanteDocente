package com.juan.vigidocente.integration.incidente;

import com.juan.vigidocente.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de integración DELETE → /api/incidentes/{id}
 * Solo COORDINADOR o ADMINISTRADOR pueden borrar incidentes.
 */
class IncidenteDeleteIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("DELETE /api/incidentes/{id} con rol ADMINISTRADOR borra el incidente (204) y desaparece de la BD")
    void adminEliminaIncidente() throws Exception {
        assertTrue(incidenteRepository.existsById(incidente.getId()), "Pre: el incidente existe");

        mockMvc.perform(delete("/api/incidentes/" + incidente.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin)))
                .andExpect(status().isNoContent());

        assertFalse(incidenteRepository.existsById(incidente.getId()),
                "Post: el incidente ya no existe en la BD");
    }

    @Test
    @DisplayName("DELETE /api/incidentes/{id} con rol DOCENTE devuelve 403")
    void docenteNoPuedeEliminarIncidente() throws Exception {
        mockMvc.perform(delete("/api/incidentes/" + incidente.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenDocente)))
                .andExpect(status().isForbidden());

        // El incidente sigue existiendo
        assertTrue(incidenteRepository.existsById(incidente.getId()));
    }
}