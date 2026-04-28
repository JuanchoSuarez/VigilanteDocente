package com.juan.vigidocente.controller;

import com.juan.vigidocente.model.Incidente;
import com.juan.vigidocente.model.SeveridadIncidente;
import com.juan.vigidocente.model.TipoIncidente;
import com.juan.vigidocente.service.IncidenteService;
import com.juan.vigidocente.service.TurnoService;
import com.juan.vigidocente.service.ZonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/incidentes")
@RequiredArgsConstructor
public class IncidenteController {

    private final IncidenteService incidenteService;
    private final TurnoService turnoService;
    private final ZonaService zonaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("incidentes", incidenteService.listarTodos());
        return "incidentes/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("incidente", new Incidente());
        model.addAttribute("turnos", turnoService.listarTodos());
        model.addAttribute("zonas", zonaService.listarTodos());
        model.addAttribute("tipos", TipoIncidente.values());
        model.addAttribute("severidades", SeveridadIncidente.values());
        return "incidentes/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Incidente incidente) {
        incidenteService.guardar(incidente);
        return "redirect:/incidentes";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("incidente", incidenteService.buscarPorId(id));
        model.addAttribute("turnos", turnoService.listarTodos());
        model.addAttribute("zonas", zonaService.listarTodos());
        model.addAttribute("tipos", TipoIncidente.values());
        model.addAttribute("severidades", SeveridadIncidente.values());
        return "incidentes/formulario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        incidenteService.eliminar(id);
        return "redirect:/incidentes";
    }
}