package com.kiku.kiku_backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Value("${admin.password}")
    private String adminPassword;

    private final JwtService jwtService;

    public AdminController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String password = body.get("password");
        if (password == null || !password.equals(adminPassword)) {
            return ResponseEntity.status(401).body("Invalid password");
        }
        String token = jwtService.generateToken("admin", "ADMIN");
        return ResponseEntity.ok(Map.of("token", token));
    }
}