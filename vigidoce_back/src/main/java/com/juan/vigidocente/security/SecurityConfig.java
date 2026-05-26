package com.juan.vigidocente.security;

import com.juan.vigidocente.repository.DocenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración central de Spring Security.
 * - Define BCrypt como encoder de contraseñas
 * - Carga al Docente desde la BD usando el email
 * - Define qué rutas son públicas y cuáles requieren rol
 * - Inyecta el JwtAuthFilter antes del filtro estándar de username/password
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final DocenteRepository docenteRepository;

    // ====== Encoder de contraseñas ======
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ====== UserDetailsService: cómo cargar al usuario desde la BD ======
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> docenteRepository.findByEmail(email)
                .map(d -> (org.springframework.security.core.userdetails.UserDetails) d)
                .orElseThrow(() -> new UsernameNotFoundException("Docente no encontrado: " + email));
    }

    // ====== Provider: conecta el UserDetailsService con el encoder ======
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ====== AuthenticationManager: lo usa AuthController para validar credenciales ======
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ====== CORS: para que el frontend Angular en :4200 pueda hablarle al backend ======
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of("http://localhost:4200"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setExposedHeaders(List.of("Authorization"));
        cors.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    // ====== Cadena de filtros: reglas de autorización por endpoint ======
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(c -> c.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // === Públicos ===
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // === DOCENTES ===
                .requestMatchers(HttpMethod.GET,    "/api/docentes/**").hasAnyRole("COORDINADOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST,   "/api/docentes/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT,    "/api/docentes/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/docentes/**").hasRole("ADMINISTRADOR")

                // === ZONAS ===
                .requestMatchers(HttpMethod.GET,    "/api/zonas/**").authenticated()
                .requestMatchers(HttpMethod.POST,   "/api/zonas/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT,    "/api/zonas/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/zonas/**").hasRole("ADMINISTRADOR")

                // === TURNOS ===
                .requestMatchers(HttpMethod.GET,    "/api/turnos/**").authenticated()
                .requestMatchers(HttpMethod.POST,   "/api/turnos/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT,    "/api/turnos/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/turnos/**").hasRole("ADMINISTRADOR")

                // === REGISTROS DE VIGILANCIA (check-in) ===
                .requestMatchers(HttpMethod.GET,  "/api/registros/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/registros/**").hasRole("DOCENTE")

                // === INCIDENTES ===
                .requestMatchers(HttpMethod.GET,    "/api/incidentes/**").authenticated()
                .requestMatchers(HttpMethod.POST,   "/api/incidentes/**").hasRole("DOCENTE")
                .requestMatchers(HttpMethod.PUT,    "/api/incidentes/**").hasAnyRole("COORDINADOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/incidentes/**").hasAnyRole("COORDINADOR", "ADMINISTRADOR")

                // === REASIGNACIONES ===
                .requestMatchers(HttpMethod.GET,    "/api/reasignaciones/**").authenticated()
                .requestMatchers(HttpMethod.POST,   "/api/reasignaciones/**").hasRole("DOCENTE")
                .requestMatchers(HttpMethod.PATCH,  "/api/reasignaciones/**").hasAnyRole("COORDINADOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT,    "/api/reasignaciones/**").hasAnyRole("COORDINADOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/reasignaciones/**").hasAnyRole("COORDINADOR", "ADMINISTRADOR")

                // === PERFILES, HORARIOS, FRANJAS ===
                .requestMatchers("/api/perfiles/**").authenticated()
                .requestMatchers(HttpMethod.GET,    "/api/franjas/**", "/api/horarios/**").authenticated()
                .requestMatchers(HttpMethod.POST,   "/api/franjas/**", "/api/horarios/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT,    "/api/franjas/**", "/api/horarios/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/franjas/**", "/api/horarios/**").hasRole("ADMINISTRADOR")

                // === SERVIDOR (utilidad del sistema) ===
                .requestMatchers("/api/servidor/**").hasRole("ADMINISTRADOR")

                // === Todo lo demás requiere autenticación ===
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}