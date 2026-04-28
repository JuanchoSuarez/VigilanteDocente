package com.juan.vigidocente.controller;

import com.juan.vigidocente.model.Docente;
import com.juan.vigidocente.model.RolDocente;
import com.juan.vigidocente.service.DocenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/docentes")
@RequiredArgsConstructor
public class DocenteController {

    private final DocenteService docenteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("docentes", docenteService.listarTodos());
        return "docentes/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("docente", new Docente());
        model.addAttribute("roles", RolDocente.values());
        return "docentes/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Docente docente) {
        docenteService.guardar(docente);
        return "redirect:/docentes";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("docente", docenteService.buscarPorIdConPerfil(id));
        model.addAttribute("roles", RolDocente.values());
        return "docentes/formulario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        docenteService.eliminar(id);
        return "redirect:/docentes";
    }
}