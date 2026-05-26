package com.juan.vigidocente.integration.auth;

import com.juan.vigidocente.BaseIntegrationTest;
import com.juan.vigidocente.model.Docente;
import com.juan.vigidocente.model.RolDocente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para POST /api/auth/registro
 * Cubre: registro exitoso, email duplicado, intento de registrar admin, datos faltantes,
 * y verifica que el password se almacene HASHEADO (no en texto plano).
 */
class RegistroIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("POST /api/auth/registro con datos válidos crea un DOCENTE (201) con password hasheada")
    void registroExitosoDocente() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nombre",   "Nuevo",
                "apellido", "Usuario",
                "email",    "nuevo@test.com",
                "password", "miPass123",
                "rol",      "DOCENTE"
        ));

        mockMvc.perform(post("/api/auth/registro")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("nuevo@test.com"))
                .andExpect(jsonPath("$.rol").value("DOCENTE"))
                // El password JAMÁS debe filtrarse en la respuesta
                .andExpect(jsonPath("$.password").doesNotExist());

        // Verificamos contra la BD que la contraseña quedó HASHEADA
        Docente guardado = docenteRepository.findByEmail("nuevo@test.com").orElseThrow();
        assertNotEquals("miPass123", guardado.getPassword(),
                "El password NO debe almacenarse en texto plano");
        assertTrue(guardado.getPassword().startsWith("$2"),
                "El password debe estar hasheado con BCrypt (empieza por $2)");
        assertTrue(passwordEncoder.matches("miPass123", guardado.getPassword()),
                "El hash debe corresponder al password original");
    }

    @Test
    @DisplayName("POST /api/auth/registro con email ya existente devuelve 409")
    void registroEmailDuplicado() throws Exception {
        // "admin@test.com" ya existe (lo crea el BeforeEach de BaseIntegrationTest)
        String body = objectMapper.writeValueAsString(Map.of(
                "nombre",   "Otro",
                "apellido", "Admin",
                "email",    "admin@test.com",
                "password", "lo-que-sea",
                "rol",      "DOCENTE"
        ));

        mockMvc.perform(post("/api/auth/registro")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/auth/registro con rol ADMINISTRADOR es rechazado (403)")
    void noSePuedeRegistrarComoAdmin() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nombre",   "Intruso",
                "apellido", "Malicioso",
                "email",    "intruso@test.com",
                "password", "pass",
                "rol",      "ADMINISTRADOR"
        ));

        mockMvc.perform(post("/api/auth/registro")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());

        // No debe haberse creado
        assertFalse(docenteRepository.findByEmail("intruso@test.com").isPresent());
    }

    @Test
    @DisplayName("POST /api/auth/registro con campos faltantes devuelve 400")
    void registroConCamposFaltantes() throws Exception {
        // Falta password y rol
        Map<String, String> body = new HashMap<>();
        body.put("nombre", "Falta");
        body.put("apellido", "Datos");
        body.put("email", "incompleto@test.com");

        mockMvc.perform(post("/api/auth/registro")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}