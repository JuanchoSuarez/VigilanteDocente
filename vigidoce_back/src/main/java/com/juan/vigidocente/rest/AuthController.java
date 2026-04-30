package com.juan.vigidocente.rest;

import com.juan.vigidocente.model.Docente;
import com.juan.vigidocente.model.RolDocente;
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
        String email    = credenciales.get("email");
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

    /**
     * Registro de nueva cuenta.
     * Contraseñas almacenadas en texto plano (sin BCrypt) para consistencia con el login actual.
     * Body: { nombre, apellido, email, password, rol }
     */
    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Map<String, String> datos) {
        String nombre   = datos.get("nombre");
        String apellido = datos.get("apellido");
        String email    = datos.get("email");
        String password = datos.get("password");
        String rolStr   = datos.get("rol");

        if (nombre == null || apellido == null || email == null || password == null || rolStr == null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Todos los campos son requeridos"));
        }

        if (docenteRepository.existsByEmail(email)) {
            return ResponseEntity.status(409).body(Map.of("mensaje", "Este correo ya está registrado"));
        }

        RolDocente rol;
        try {
            rol = RolDocente.valueOf(rolStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Rol inválido. Use DOCENTE o COORDINADOR"));
        }

        if (rol == RolDocente.ADMINISTRADOR) {
            return ResponseEntity.status(403).body(Map.of("mensaje", "El rol ADMINISTRADOR solo puede asignarlo un admin existente"));
        }

        Docente nuevo = Docente.builder()
                .nombre(nombre.trim())
                .apellido(apellido.trim())
                .email(email.trim().toLowerCase())
                .password(password)
                .telefono("")
                .rol(rol)
                .activo(true)
                .build();

        Docente guardado = docenteRepository.save(nuevo);
        return ResponseEntity.status(201).body(guardado);
    }
}
