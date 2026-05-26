package com.juan.vigidocente.system;

import com.fasterxml.jackson.databind.JsonNode;
import com.juan.vigidocente.BaseIntegrationTest;
import com.juan.vigidocente.model.EstadoReasignacion;
import com.juan.vigidocente.model.Reasignacion;
import com.juan.vigidocente.model.Turno;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PRUEBA DE SISTEMA (end-to-end) — Caso de uso más complejo del sistema:
 * "Reasignación de turno por impedimento del docente original".
 *
 * Este flujo encadena:
 *   1. Login del DOCENTE original (POST /api/auth/login)
 *   2. El docente solicita reasignación (POST /api/reasignaciones/solicitar)
 *   3. Login del COORDINADOR (POST /api/auth/login)
 *   4. El coordinador lista las reasignaciones pendientes (GET /api/reasignaciones/pendientes)
 *   5. El coordinador acepta la reasignación con un docente reemplazo (PATCH /api/reasignaciones/{id}/aceptar)
 *   6. Se verifica que la reasignación quedó ACEPTADA + tiene reemplazo asignado
 *   7. Se verifica que el TURNO pasó a estar asignado al docente reemplazo
 *
 * IMPORTANTE: este test SOBREESCRIBE el @BeforeEach de la clase base con un método vacío,
 * porque necesitamos que la BD persista entre pasos del flujo (no que se resiembre cada vez).
 * Los datos se siembran UNA SOLA VEZ al inicio con @BeforeAll.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReasignacionEndToEndSystemTest extends BaseIntegrationTest {

    // Estado compartido entre pasos del flujo
    private String tokenDocenteOriginal;
    private String tokenCoordinador;
    private Long reasignacionCreadaId;
    private Long turnoOriginalId;
    private Long docenteOriginalId;
    private Long docenteReemplazoId;

    /**
     * SOBREESCRIBE el @BeforeEach de la clase base: aquí lo dejamos vacío
     * para que NO se resemilla la BD entre pasos del flujo.
     */
    @Override
    protected void setUpDatosDePrueba() {
        // intencionalmente vacío — el seed se hace una sola vez en @BeforeAll
    }

    /**
     * Se ejecuta una sola vez al inicio de la clase: siembra los datos
     * llamando manualmente al método de la clase base.
     */
    @BeforeAll
    void seedUnaSolaVez() {
        super.setUpDatosDePrueba();
        // Capturamos los IDs aquí; los demás pasos los usan
        turnoOriginalId    = turno.getId();
        docenteOriginalId  = docente.getId();
        docenteReemplazoId = admin.getId();
    }

    @Test
    @Order(1)
    @DisplayName("Paso 1: El docente original hace login y obtiene un JWT")
    void paso1_loginDocenteOriginal() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", "docente@test.com",
                "password", "1234"
        ));

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.docente.rol").value("DOCENTE"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        tokenDocenteOriginal = json.get("token").asText();

        assertNotNull(tokenDocenteOriginal);
        assertFalse(tokenDocenteOriginal.isBlank());
    }

    @Test
    @Order(2)
    @DisplayName("Paso 2: El docente solicita reasignación para su turno (POST /reasignaciones/solicitar)")
    void paso2_docenteSolicitaReasignacion() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "turnoId",   turnoOriginalId,
                "docenteId", docenteOriginalId,
                "motivo",    "Cita médica urgente"
        ));

        MvcResult result = mockMvc.perform(post("/api/reasignaciones/solicitar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenDocenteOriginal))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.motivo").value("Cita médica urgente"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        reasignacionCreadaId = json.get("id").asLong();

        assertNotNull(reasignacionCreadaId);
    }

    @Test
    @Order(3)
    @DisplayName("Paso 3: El coordinador hace login y obtiene un JWT")
    void paso3_loginCoordinador() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", "coord@test.com",
                "password", "1234"
        ));

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.docente.rol").value("COORDINADOR"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        tokenCoordinador = json.get("token").asText();

        assertNotNull(tokenCoordinador);
    }

    @Test
    @Order(4)
    @DisplayName("Paso 4: El coordinador lista las reasignaciones pendientes y ve la nueva")
    void paso4_coordinadorVeReasignacionPendiente() throws Exception {
        mockMvc.perform(get("/api/reasignaciones/pendientes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenCoordinador)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.id == " + reasignacionCreadaId + ")]").exists());
    }

    @Test
    @Order(5)
    @DisplayName("Paso 5: El coordinador acepta la reasignación asignando al docente reemplazo (PATCH /.../aceptar)")
    void paso5_coordinadorAceptaReasignacion() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "docenteReemplazoId", docenteReemplazoId
        ));

        mockMvc.perform(patch("/api/reasignaciones/" + reasignacionCreadaId + "/aceptar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenCoordinador))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reasignacionCreadaId))
                .andExpect(jsonPath("$.estado").value("ACEPTADA"))
                .andExpect(jsonPath("$.docenteReemplazo.id").value(docenteReemplazoId));
    }

    @Test
    @Order(6)
    @DisplayName("Paso 6: Verificación final — la reasignación quedó ACEPTADA en la BD")
    void paso6_verificarReasignacionEnBD() {
        Reasignacion r = reasignacionRepository.findById(reasignacionCreadaId).orElseThrow();

        assertEquals(EstadoReasignacion.ACEPTADA, r.getEstado(),
                "La reasignación debe estar en estado ACEPTADA");
        assertNotNull(r.getDocenteReemplazo(),
                "Debe tener un docente reemplazo asignado");
        assertEquals(docenteReemplazoId, r.getDocenteReemplazo().getId(),
                "El reemplazo debe ser el docente que el coordinador asignó");
        assertNotNull(r.getFechaHoraRespuesta(),
                "Debe registrarse la fecha/hora de respuesta del coordinador");
    }

    @Test
    @Order(7)
    @DisplayName("Paso 7: Verificación final — el TURNO quedó asignado al docente reemplazo")
    void paso7_verificarTurnoReasignadoEnBD() {
        Turno t = turnoRepository.findById(turnoOriginalId).orElseThrow();

        assertEquals(docenteReemplazoId, t.getDocente().getId(),
                "El turno debe estar ahora asignado al docente reemplazo, no al original");
    }
}