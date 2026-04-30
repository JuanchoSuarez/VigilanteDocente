package com.juan.vigidocente.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/servidor")
public class ServidorRestController {

    @GetMapping("/fecha-hora")
    public Map<String, String> getFechaHora() {
        Map<String, String> response = new HashMap<>();
        LocalDate fecha = LocalDate.now();
        LocalTime hora = LocalTime.now();
        response.put("fecha", fecha.toString());
        response.put("hora", hora.withNano(0).toString());
        // Ajuste de Locale o manual
        String diaStr = fecha.getDayOfWeek().name();
        String diaSemana = switch (diaStr) {
            case "MONDAY" -> "LUNES";
            case "TUESDAY" -> "MARTES";
            case "WEDNESDAY" -> "MIERCOLES";
            case "THURSDAY" -> "JUEVES";
            case "FRIDAY" -> "VIERNES";
            case "SATURDAY" -> "SABADO";
            case "SUNDAY" -> "DOMINGO";
            default -> diaStr;
        };
        response.put("diaSemana", diaSemana);
        return response;
    }
}
