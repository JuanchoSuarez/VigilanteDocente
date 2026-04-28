package com.juan.vigidocente.rest;

import com.juan.vigidocente.model.Docente;
import com.juan.vigidocente.repository.DocenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final DocenteRepository docenteRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String email = credenciales.get("email");
        String password = credenciales.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Email y contraseña son requeridos"));
        }

        Optional<Docente> docente = docenteRepository.findByEmailAndPassword(email, password);

        if (docente.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Credenciales incorrectas"));
        }

        if (!docente.get().getActivo()) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Usuario inactivo"));
        }

        return ResponseEntity.ok(docente.get());
    }
}
