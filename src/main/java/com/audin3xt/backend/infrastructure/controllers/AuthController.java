package com.audin3xt.backend.infrastructure.controllers;

import com.audin3xt.backend.application.dtos.LoginRequest;
import com.audin3xt.backend.application.dtos.LoginResponse;
import com.audin3xt.backend.domain.models.User;
import com.audin3xt.backend.domain.repositories.UserRepository;
import com.audin3xt.backend.infrastructure.security.JwtService;

// Nuevas importaciones necesarias para las cookies
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
// CAMBIO CRÍTICO DE SEGURIDAD: Especificamos el origen y permitimos credenciales (cookies)
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); 

    public AuthController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    // Agregamos HttpServletResponse a los parámetros para poder inyectar la cookie
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        
        User user = userRepository.findByEmail(request.email())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"Usuario no encontrado\"}");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"Contraseña incorrecta\"}");
        }

        // 1. Generamos el token
        String token = jwtService.generarToken(user);

        // 2. CREAMOS LA COOKIE
        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true); // ¡Regla de oro cumplida! Prohíbe a JavaScript leer el token
        jwtCookie.setSecure(false);  // Ponlo en 'false' mientras uses localhost (HTTP). Cambia a 'true' cuando subas a producción (HTTPS).
        jwtCookie.setPath("/");      // Permite que la cookie viaje a cualquier endpoint de tu API
        jwtCookie.setMaxAge(60 * 60 * 24); // Tiempo de vida de la cookie: 1 día (en segundos)
        
        // 3. Adjuntamos la cookie a la respuesta que va al navegador
        response.addCookie(jwtCookie);

        // Seguimos devolviendo tu LoginResponse para no romper tu frontend, 
        // pero ahora la seguridad real viaja silenciosamente en la cookie.
        return ResponseEntity.ok(new LoginResponse(token, user.getEmail(), user.getRol()));
    }
}