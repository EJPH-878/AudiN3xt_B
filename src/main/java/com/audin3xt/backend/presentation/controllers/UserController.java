package com.audin3xt.backend.presentation.controllers;

import com.audin3xt.backend.domain.models.User;
import com.audin3xt.backend.domain.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        System.out.println("Usuarios encontrados en BD: " + users.size());
        return users;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (user.getEmail().equals("admin@n3xt.com")) {
            user.setRol("ADMIN");
        } else if (user.getRol() == null || user.getRol().isEmpty()) {
            user.setRol("AUDITOR");
        }
        System.out.println("Contraseña sin encriptar: " + user.getPasswordHash()); 
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    // --- NUEVO: EDITAR USUARIO ---
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            // Actualizamos los campos permitidos (ej. email y rol)
            existingUser.setEmail(userDetails.getEmail());
            existingUser.setRol(userDetails.getRol());
            
            // Si mandan una nueva contraseña, la encriptamos. Si no, dejamos la anterior.
            if (userDetails.getPasswordHash() != null && !userDetails.getPasswordHash().isEmpty()) {
                existingUser.setPasswordHash(passwordEncoder.encode(userDetails.getPasswordHash()));
            }
            
            User updatedUser = userRepository.save(existingUser);
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // --- NUEVO: ELIMINAR USUARIO ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}