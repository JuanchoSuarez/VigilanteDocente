package com.juan.vigidocente.rest;

import com.juan.vigidocente.model.Docente;
import com.juan.vigidocente.model.RolDocente;
import com.juan.vigidocente.repository.DocenteRepository;
import com.juan.vigidocente.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints públicos de autenticación.
 * - POST /api/auth/login    → valida credenciales y devuelve un JWT
 * - POST /api/auth/registro → crea cuenta nueva (rol DOCENTE o COORDINADOR) con password BCrypt
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final DocenteRepository docenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String email    = credenciales.get("email");
        String password = credenciales.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Email y contraseña son requeridos"));
        }

        try {
            // Spring Security valida email + password (con BCrypt) y nos da el usuario autenticado
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email.trim().toLowerCase(), password)
            );

            Docente docente = (Docente) auth.getPrincipal();
            String token = jwtService.generateToken(docente);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "docente", docente   // password viene con @JsonIgnore, no se filtra
            ));

        } catch (DisabledException e) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Usuario inactivo"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Credenciales incorrectas"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Error de autenticación"));
        }
    }

    /**
     * Registro de nueva cuenta.
     * La contraseña se hashea con BCrypt antes de guardarse.
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
                .password(passwordEncoder.encode(password))   // ⬅️ BCrypt
                .telefono("")
                .rol(rol)
                .activo(true)
                .build();

        Docente guardado = docenteRepository.save(nuevo);
        return ResponseEntity.status(201).body(guardado);
    }
}