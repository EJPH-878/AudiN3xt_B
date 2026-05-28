package com.audin3xt.backend.infrastructure.controllers;

import com.audin3xt.backend.application.dtos.LoginRequest;
import com.audin3xt.backend.application.dtos.LoginResponse;
import com.audin3xt.backend.domain.models.User;
import com.audin3xt.backend.domain.repositories.UserRepository;
import com.audin3xt.backend.infrastructure.security.JwtService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "https://audi-n3xt-f-3cog.vercel.app", allowCredentials = "true")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
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

        // 2. CONFIGURACIÓN CORRECTA PARA PRODUCCIÓN (SameSite=None y Secure)
        String cookieHeader = String.format(
            "jwt=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=None", 
            token, 60 * 60 * 24
        );
        
        // 3. Adjuntamos la cabecera manualmente
        response.addHeader("Set-Cookie", cookieHeader);

        return ResponseEntity.ok(new LoginResponse(token, user.getEmail(), user.getRol()));
    }
}