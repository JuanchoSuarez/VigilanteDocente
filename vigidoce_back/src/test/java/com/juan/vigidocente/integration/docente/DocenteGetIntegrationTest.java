package com.juan.vigidocente.integration.docente;

import com.juan.vigidocente.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración GET → /api/docentes
 * Verifica autorización por rol:
 *   - Sin token → 403
 *   - Con token DOCENTE → 403 (la matriz solo permite COORDINADOR y ADMINISTRADOR)
 *   - Con token COORDINADOR → 200
 *   - Con token ADMINISTRADOR → 200 + lista
 */
class DocenteGetIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /api/docentes sin token devuelve 403")
    void sinTokenForbidden() throws Exception {
        mockMvc.perform(get("/api/docentes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/docentes con rol DOCENTE devuelve 403")
    void docenteNoAutorizado() throws Exception {
        mockMvc.perform(get("/api/docentes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenDocente)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/docentes con rol ADMINISTRADOR devuelve 200 + lista")
    void adminListaDocentes() throws Exception {
        mockMvc.perform(get("/api/docentes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(3)));
    }

    @Test
    @DisplayName("GET /api/docentes con rol COORDINADOR devuelve 200")
    void coordinadorListaDocentes() throws Exception {
        mockMvc.perform(get("/api/docentes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenCoord)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}