package com.juan.vigidocente.controller;

import com.juan.vigidocente.model.EstadoReasignacion;
import com.juan.vigidocente.model.Reasignacion;
import com.juan.vigidocente.service.DocenteService;
import com.juan.vigidocente.service.ReasignacionService;
import com.juan.vigidocente.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reasignaciones")
@RequiredArgsConstructor
public class ReasignacionController {

    private final ReasignacionService reasignacionService;
    private final TurnoService turnoService;
    private final DocenteService docenteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("reasignaciones", reasignacionService.listarTodos());
        return "reasignaciones/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("reasignacion", new Reasignacion());
        model.addAttribute("turnos", turnoService.listarTodos());
        model.addAttribute("docentes", docenteService.listarTodos());
        model.addAttribute("estados", EstadoReasignacion.values());
        return "reasignaciones/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Reasignacion reasignacion) {
        reasignacionService.guardar(reasignacion);
        return "redirect:/reasignaciones";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("reasignacion", reasignacionService.buscarPorId(id));
        model.addAttribute("turnos", turnoService.listarTodos());
        model.addAttribute("docentes", docenteService.listarTodos());
        model.addAttribute("estados", EstadoReasignacion.values());
        return "reasignaciones/formulario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        reasignacionService.eliminar(id);
        return "redirect:/reasignaciones";
    }
}