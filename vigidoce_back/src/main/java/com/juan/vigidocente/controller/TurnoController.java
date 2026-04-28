package com.juan.vigidocente.controller;

import com.juan.vigidocente.model.EstadoTurno;
import com.juan.vigidocente.model.TipoFranja;
import com.juan.vigidocente.model.Turno;
import com.juan.vigidocente.service.DocenteService;
import com.juan.vigidocente.service.TurnoService;
import com.juan.vigidocente.service.ZonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;
    private final DocenteService docenteService;
    private final ZonaService zonaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("turnos", turnoService.listarTodos());
        return "turnos/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("turno", new Turno());
        model.addAttribute("docentes", docenteService.listarTodos());
        model.addAttribute("zonas", zonaService.listarTodos());
        model.addAttribute("estados", EstadoTurno.values());
        model.addAttribute("franjas", TipoFranja.values());
        return "turnos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Turno turno) {
        turnoService.guardar(turno);
        return "redirect:/turnos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("turno", turnoService.buscarPorId(id));
        model.addAttribute("docentes", docenteService.listarTodos());
        model.addAttribute("zonas", zonaService.listarTodos());
        model.addAttribute("estados", EstadoTurno.values());
        model.addAttribute("franjas", TipoFranja.values());
        return "turnos/formulario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        turnoService.eliminar(id);
        return "redirect:/turnos";
    }
}