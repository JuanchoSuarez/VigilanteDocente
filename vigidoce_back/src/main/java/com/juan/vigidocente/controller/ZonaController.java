package com.juan.vigidocente.controller;

import com.juan.vigidocente.model.TipoZona;
import com.juan.vigidocente.model.Zona;
import com.juan.vigidocente.service.ZonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/zonas")
@RequiredArgsConstructor
public class ZonaController {

    private final ZonaService zonaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("zonas", zonaService.listarTodos());
        return "zonas/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("zona", new Zona());
        model.addAttribute("tiposZona", TipoZona.values());
        return "zonas/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Zona zona) {
        zonaService.guardar(zona);
        return "redirect:/zonas";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("zona", zonaService.buscarPorId(id));
        model.addAttribute("tiposZona", TipoZona.values());
        return "zonas/formulario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        zonaService.eliminar(id);
        return "redirect:/zonas";
    }
}