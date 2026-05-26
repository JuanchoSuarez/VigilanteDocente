package com.juan.vigidocente.integration.auth;

import com.juan.vigidocente.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración POST → /api/auth/login
 * Endpoint complejo: valida credenciales con BCrypt y genera JWT.
 */
class LoginPostIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("POST /api/auth/login con credenciales válidas devuelve 200 + token + docente")
    void loginExitoso() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", "admin@test.com",
                "password", "1234"
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.docente.email").value("admin@test.com"))
                .andExpect(jsonPath("$.docente.rol").value("ADMINISTRADOR"))
                // ⚠️ password NUNCA debe filtrarse (gracias al @JsonIgnore en Docente)
                .andExpect(jsonPath("$.docente.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/login con password incorrecta devuelve 401")
    void loginPasswordIncorrecta() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", "admin@test.com",
                "password", "passwordIncorrecta"
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}