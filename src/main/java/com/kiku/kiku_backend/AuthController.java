package com.kiku.kiku_backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthController(UserService userService, JwtService jwtService, EmailService emailService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @PostMapping("/magic-link")
    public ResponseEntity<?> requestMagicLink(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        userService.findOrCreateUser(email);
        String token = userService.generateMagicToken(email);
        String magicLink = "http://localhost:5173/auth?token=" + token;
        try {
            emailService.sendMagicLinkEmail(email, magicLink);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send magic link email"));
        }
        return ResponseEntity.ok(Map.of("message", "Magic link sent"));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyMagicLink(@RequestParam String token) {
        User user = userService.validateMagicToken(token);
        String jwt = jwtService.generateToken(user.getEmail());
        return ResponseEntity.ok(Map.of("token", jwt));
    }

    @PostMapping("/otp/request")
public ResponseEntity<?> requestOtp(@RequestBody Map<String, String> body) {
    String email = body.get("email");
    try {
        userService.generateOtp(email);
        return ResponseEntity.ok(Map.of("message", "OTP sent"));
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
}

@PostMapping("/otp/verify")
public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
    String email = body.get("email");
    String otp = body.get("otp");
    try {
        User user = userService.validateOtp(email, otp);
        String jwt = jwtService.generateToken(user.getEmail());
        return ResponseEntity.ok(Map.of("token", jwt));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
}
