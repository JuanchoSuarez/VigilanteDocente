package com.juan.vigidocente.integration.registro;

import com.juan.vigidocente.BaseIntegrationTest;
import com.juan.vigidocente.model.MetodoRegistro;
import com.juan.vigidocente.model.RegistroVigilancia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Check-in de vigilancia (caso de uso central del proyecto: "verificación de presencia").
 * Solo el rol DOCENTE puede crear registros de vigilancia.
 */
class RegistroVigilanciaIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("POST /api/registros con rol DOCENTE crea check-in (201)")
    void docenteHaceCheckIn() throws Exception {
        RegistroVigilancia registro = RegistroVigilancia.builder()
                .turno(turno)
                .docente(docente)
                .zona(zona)
                .fechaHoraCheckIn(LocalDateTime.now())
                .metodoRegistro(MetodoRegistro.QR)
                .recorridoRealizado(false)
                .calificacionLimpieza(2)
                .build();

        mockMvc.perform(post("/api/registros")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenDocente))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registro)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.metodoRegistro").value("QR"))
                .andExpect(jsonPath("$.calificacionLimpieza").value(2));
    }

    @Test
    @DisplayName("POST /api/registros con rol COORDINADOR devuelve 403 (solo DOCENTE crea check-ins)")
    void coordinadorNoPuedeHacerCheckIn() throws Exception {
        RegistroVigilancia registro = RegistroVigilancia.builder()
                .turno(turno).docente(docente).zona(zona)
                .fechaHoraCheckIn(LocalDateTime.now())
                .metodoRegistro(MetodoRegistro.PIN)
                .recorridoRealizado(false)
                .calificacionLimpieza(1)
                .build();

        mockMvc.perform(post("/api/registros")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenCoord))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registro)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/registros sin token devuelve 403")
    void sinTokenNoPuedeHacerCheckIn() throws Exception {
        mockMvc.perform(post("/api/registros")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/registros con cualquier rol autenticado devuelve 200")
    void listarRegistrosEstaPermitidoParaTodos() throws Exception {
        mockMvc.perform(get("/api/registros")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenDocente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/registros")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenCoord)))
                .andExpect(status().isOk());
    }
}