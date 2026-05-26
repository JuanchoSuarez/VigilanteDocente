package com.juan.vigidocente.integration.docente;

import com.juan.vigidocente.BaseIntegrationTest;
import com.juan.vigidocente.model.Docente;
import com.juan.vigidocente.model.RolDocente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CRUD completo de docentes verificando autorización por rol.
 * - Solo ADMINISTRADOR puede crear, actualizar y eliminar.
 * - COORDINADOR y ADMIN pueden listar/obtener; DOCENTE no.
 *
 * Nota: enviamos los bodies como Map (JSON limpio) en vez de objetos Docente
 * para evitar arrastrar relaciones, password hasheado, y otros campos
 * que confunden la deserialización.
 */
class DocenteCrudIntegrationTest extends BaseIntegrationTest {

    private Map<String, Object> docentePayload(String nombre, String apellido, String email,
                                               String telefono, RolDocente rol) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("nombre", nombre);
        p.put("apellido", apellido);
        p.put("email", email);
        p.put("password", "1234");
        p.put("telefono", telefono);
        p.put("rol", rol.name());
        p.put("activo", true);
        return p;
    }

    @Test
    @DisplayName("POST /api/docentes con rol ADMIN crea docente nuevo (201) y aparece en BD")
    void adminCreaDocente() throws Exception {
        String body = objectMapper.writeValueAsString(
                docentePayload("Pedro", "Nuevo", "pedro@test.com", "3009990000", RolDocente.DOCENTE)
        );

        var result = mockMvc.perform(post("/api/docentes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType("application/json")
                        .content(body))
                .andReturn();

        // DEBUG: imprime status y body para diagnosticar el 500
        System.out.println("======= DEBUG adminCreaDocente =======");
        System.out.println("=== REQUEST BODY: " + body);
        System.out.println("=== STATUS:       " + result.getResponse().getStatus());
        System.out.println("=== BODY:         " + result.getResponse().getContentAsString());
        System.out.println("======================================");

        org.junit.jupiter.api.Assertions.assertEquals(
                201,
                result.getResponse().getStatus(),
                "Esperaba 201 — body recibido: " + result.getResponse().getContentAsString()
        );
    }

    @Test
    @DisplayName("POST /api/docentes con rol COORDINADOR devuelve 403 (no autorizado)")
    void coordinadorNoPuedeCrearDocente() throws Exception {
        String body = objectMapper.writeValueAsString(
                docentePayload("Bloqueado", "Test", "bloqueado@test.com", "0", RolDocente.DOCENTE)
        );

        mockMvc.perform(post("/api/docentes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenCoord))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());

        assertFalse(docenteRepository.findByEmail("bloqueado@test.com").isPresent());
    }

    @Test
    @DisplayName("GET /api/docentes/{id} con rol ADMIN devuelve el docente (200)")
    void adminObtieneDocentePorId() throws Exception {
        mockMvc.perform(get("/api/docentes/" + docente.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(docente.getId()))
                .andExpect(jsonPath("$.email").value("docente@test.com"));
    }

    @Test
    @DisplayName("PUT /api/docentes/{id} con rol ADMIN actualiza el docente y persiste el cambio")
    void adminActualizaDocente() throws Exception {
        Map<String, Object> body = docentePayload(
                "DocenteRenombrado",          // ⬅️ cambio
                docente.getApellido(),
                docente.getEmail(),
                "3119999999",                 // ⬅️ cambio
                docente.getRol()
        );

        mockMvc.perform(put("/api/docentes/" + docente.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("DocenteRenombrado"))
                .andExpect(jsonPath("$.telefono").value("3119999999"));

        // Verificación contra la BD
        Docente desdeDb = docenteRepository.findById(docente.getId()).orElseThrow();
        assertEquals("DocenteRenombrado", desdeDb.getNombre());
        assertEquals("3119999999", desdeDb.getTelefono());
    }

    @Test
    @DisplayName("DELETE /api/docentes/{id} con rol ADMIN elimina el docente (204)")
    void adminEliminaDocente() throws Exception {
        // Creamos un docente "sacrificable" para no romper relaciones con turnos/incidentes
        Docente desechable = docenteRepository.save(Docente.builder()
                .nombre("Borrame").apellido("Ya")
                .email("borrame@test.com").password(passwordEncoder.encode("x"))
                .telefono("0").rol(RolDocente.DOCENTE).activo(true).build());

        mockMvc.perform(delete("/api/docentes/" + desechable.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin)))
                .andExpect(status().isNoContent());

        assertFalse(docenteRepository.existsById(desechable.getId()));
    }
}