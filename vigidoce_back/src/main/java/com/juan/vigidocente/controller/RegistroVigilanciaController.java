package com.juan.vigidocente.controller;

import com.juan.vigidocente.model.MetodoRegistro;
import com.juan.vigidocente.model.RegistroVigilancia;
import com.juan.vigidocente.service.DocenteService;
import com.juan.vigidocente.service.RegistroVigilanciaService;
import com.juan.vigidocente.service.TurnoService;
import com.juan.vigidocente.service.ZonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/registros")
@RequiredArgsConstructor
public class RegistroVigilanciaController {

    private final RegistroVigilanciaService registroVigilanciaService;
    private final TurnoService turnoService;
    private final DocenteService docenteService;
    private final ZonaService zonaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("registros", registroVigilanciaService.listarTodos());
        return "registros/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("registro", new RegistroVigilancia());
        model.addAttribute("turnos", turnoService.listarTodos());
        model.addAttribute("docentes", docenteService.listarTodos());
        model.addAttribute("zonas", zonaService.listarTodos());
        model.addAttribute("metodos", MetodoRegistro.values());
        return "registros/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute RegistroVigilancia registro) {
        registroVigilanciaService.guardar(registro);
        return "redirect:/registros";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("registro", registroVigilanciaService.buscarPorId(id));
        model.addAttribute("turnos", turnoService.listarTodos());
        model.addAttribute("docentes", docenteService.listarTodos());
        model.addAttribute("zonas", zonaService.listarTodos());
        model.addAttribute("metodos", MetodoRegistro.values());
        return "registros/formulario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        registroVigilanciaService.eliminar(id);
        return "redirect:/registros";
    }
}