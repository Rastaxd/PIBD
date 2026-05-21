package org.dorixon.springlab4.controller;

import org.dorixon.springlab4.auth.Credentials;
import org.dorixon.springlab4.auth.Tokens;
import org.dorixon.springlab4.model.Student;
import org.dorixon.springlab4.service.AuthService;
import org.dorixon.springlab4.validation.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;
    private final ValidationService<Credentials> credentialsValidationService;

    @PostMapping("/register")
    public ResponseEntity<Tokens> register(@RequestBody Student student) {
        Credentials creds = new Credentials(student.getEmail(), student.getPassword());
        credentialsValidationService.validate(creds);
        return ResponseEntity.ok(authService.register(creds, student));
    }

    @PostMapping("/login")
    public ResponseEntity<Tokens> login(@RequestBody Credentials credentials) {
        credentialsValidationService.validate(credentials);
        return ResponseEntity.ok(authService.login(credentials));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Tokens> refresh(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String refreshToken = authorizationHeader.substring(7);
            return ResponseEntity.ok(authService.refreshToken(refreshToken));
        }
        return ResponseEntity.badRequest().build();
    }
}
