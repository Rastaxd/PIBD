package org.dorixon.springlab4.service;

import lombok.RequiredArgsConstructor;
import org.dorixon.springlab4.auth.Credentials;
import org.dorixon.springlab4.auth.Tokens;
import org.dorixon.springlab4.model.Role;
import org.dorixon.springlab4.model.Student;
import org.dorixon.springlab4.repository.RoleRepository;
import org.dorixon.springlab4.repository.StudentRepository;
import org.dorixon.springlab4.security.JwtService;
import org.dorixon.springlab4.validation.ValidationService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ValidationService<Student> studentValidationService;

    public Tokens register(Credentials credentials, Student studentDetails) {
        studentDetails.setEmail(credentials.email());
        studentDetails.setPassword(passwordEncoder.encode(credentials.password()));
        
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        studentDetails.setRoles(roles);

        studentValidationService.validate(studentDetails);
        
        Student savedUser = studentRepository.save(studentDetails);
        
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);
        
        return new Tokens(accessToken, refreshToken);
    }

    public Tokens login(Credentials credentials) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(credentials.email(), credentials.password())
        );

        Student user = studentRepository.findByEmail(credentials.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new Tokens(accessToken, refreshToken);
    }

    public Tokens refreshToken(String refreshToken) {
        String userEmail = jwtService.extractUsernameFromRefresh(refreshToken);
        if (userEmail != null) {
            Student user = studentRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (jwtService.isRefreshTokenValid(refreshToken, user)) {
                String accessToken = jwtService.generateAccessToken(user);
                String newRefreshToken = jwtService.generateRefreshToken(user);
                return new Tokens(accessToken, newRefreshToken);
            }
        }
        throw new RuntimeException("Refresh token is invalid");
    }
}
