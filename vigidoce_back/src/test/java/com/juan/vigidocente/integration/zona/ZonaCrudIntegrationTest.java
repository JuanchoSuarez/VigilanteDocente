package com.juan.vigidocente.integration.zona;

import com.juan.vigidocente.BaseIntegrationTest;
import com.juan.vigidocente.model.TipoZona;
import com.juan.vigidocente.model.Zona;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CRUD de zonas con autorización por rol.
 * - Cualquier autenticado puede LEER.
 * - Solo ADMINISTRADOR puede CREAR/ACTUALIZAR/ELIMINAR.
 */
class ZonaCrudIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /api/zonas con rol DOCENTE devuelve 200 (todos los autenticados pueden leer)")
    void docenteListaZonas() throws Exception {
        mockMvc.perform(get("/api/zonas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenDocente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/zonas con rol ADMIN crea zona (201)")
    void adminCreaZona() throws Exception {
        Zona nueva = Zona.builder()
                .nombre("Cancha Nueva")
                .descripcion("Cancha para tests")
                .tipo(TipoZona.PATIO)
                .capacidadMaxima(50)
                .activa(true)
                .build();

        mockMvc.perform(post("/api/zonas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Cancha Nueva"))
                .andExpect(jsonPath("$.capacidadMaxima").value(50));
    }

    @Test
    @DisplayName("POST /api/zonas con rol DOCENTE devuelve 403")
    void docenteNoPuedeCrearZona() throws Exception {
        Zona nueva = Zona.builder()
                .nombre("Prohibida").descripcion("x")
                .tipo(TipoZona.PATIO).capacidadMaxima(10).activa(true).build();

        mockMvc.perform(post("/api/zonas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenDocente))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/zonas/{id} con rol ADMIN actualiza la zona")
    void adminActualizaZona() throws Exception {
        Zona actualizada = Zona.builder()
                .id(zona.getId())
                .nombre("Patio Renombrado")     // ⬅️ cambio
                .descripcion(zona.getDescripcion())
                .tipo(zona.getTipo())
                .capacidadMaxima(250)            // ⬅️ cambio
                .activa(true)
                .build();

        mockMvc.perform(put("/api/zonas/" + zona.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Patio Renombrado"))
                .andExpect(jsonPath("$.capacidadMaxima").value(250));
    }

    @Test
    @DisplayName("DELETE /api/zonas/{id} con rol ADMIN elimina la zona (204)")
    void adminEliminaZona() throws Exception {
        // Creamos una zona aislada (sin turnos) para no chocar con FK
        Zona desechable = zonaRepository.save(Zona.builder()
                .nombre("Borrar").descripcion("temp")
                .tipo(TipoZona.PATIO).capacidadMaxima(1).activa(true).build());

        mockMvc.perform(delete("/api/zonas/" + desechable.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin)))
                .andExpect(status().isNoContent());

        assertFalse(zonaRepository.existsById(desechable.getId()));
    }
}