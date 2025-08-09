package com.example.techiedating.service;

import com.example.techiedating.dto.AuthenticationRequest;
import com.example.techiedating.dto.AuthenticationResponse;
import com.example.techiedating.dto.RegisterRequest;
import com.example.techiedating.dto.UserResponse;
import com.example.techiedating.model.Role;
import com.example.techiedating.model.User;
import com.example.techiedating.repository.UserRepository;
import com.example.techiedating.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserResponse register(RegisterRequest request) {
        // Create a new user with USER role by default
        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .active(true)
                .createdAt(Instant.now())
                .build();
        
        // Save the user to the database
        user = userRepository.save(user);
        
        // Convert roles to role names
        Set<String> roleNames = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
                
        return UserResponse.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .active(user.isActive())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt() : Instant.now())
                .roles(roleNames)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Authenticate the user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        
        // Get the user from the database
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();
        
        // Generate JWT and refresh tokens
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }
}
