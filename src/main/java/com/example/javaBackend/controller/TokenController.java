package com.example.javaBackend.controller;

import com.example.javaBackend.controller.dto.LoginRequest;
import com.example.javaBackend.controller.dto.LoginResponse;
import com.example.javaBackend.entities.User;
import com.example.javaBackend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.javaBackend.entities.Role;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class TokenController {

    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    public TokenController(JwtEncoder jwtEncoder, UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        Optional<User> user = userRepository.findByName(loginRequest.name());

        if (user.isEmpty() || !user.get().isLoginCorrect(loginRequest, bCryptPasswordEncoder)) {
            throw new BadCredentialsException("user or password is invalid !");
        }

        String scope = user.get().getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(" "));

        final Instant instant = Instant.now();
        final Long expiryTime = 300L;

        JwtClaimsSet claim = JwtClaimsSet.builder()
                .issuer("mybackend")
                .subject(user.get().getId().toString())
                .issuedAt(instant)
                .expiresAt(instant.plusSeconds(expiryTime))
                .claim("scope",scope)
                .build();


        String jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claim)).getTokenValue();

        return ResponseEntity.ok(new LoginResponse(jwtValue, expiryTime));

    }

}
