package com.example.javaBackend.controller;

import com.example.javaBackend.controller.dto.CreateUserDto;
import com.example.javaBackend.entities.Role;
import com.example.javaBackend.entities.User;
import com.example.javaBackend.repository.RoleRepository;
import com.example.javaBackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(
            UserRepository userRepository
            , RoleRepository roleRepository
            , BCryptPasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @PostMapping("/users")
    public ResponseEntity<User> newUser(@RequestBody CreateUserDto createUserDto) {

        Role basicRole = roleRepository.findByName(Role.Values.BASIC.name());
        Optional<User> userDb = userRepository.findByName(createUserDto.name());

        if (userDb.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        User newUser = new User();
        newUser.setName(createUserDto.name());
        newUser.setPassword(passwordEncoder.encode(createUserDto.password()));
        newUser.setRoles(Set.of(basicRole));

        userRepository.save(newUser);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/hello")
    public ResponseEntity<String[]> firstPage(){
        return ResponseEntity.ok(new String[]{"HELLO"});
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    public ResponseEntity<List<User>> listAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{name}")
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    public ResponseEntity<List<User>> searchUserByUserName(@PathVariable("name") String name) {
        List<User> users = userRepository.findByNameStartingWith(name);
        return ResponseEntity.ok(users);
    }

    @Transactional
    @PutMapping("/users")
    public ResponseEntity<User> updateUser(@RequestBody CreateUserDto createUserDto,JwtAuthenticationToken token){

        User tokenUser = userRepository.findById(UUID.fromString(token.getName()));
        tokenUser.setName(createUserDto.name());
        tokenUser.setPassword(createUserDto.password());
        userRepository.save(tokenUser);
        return ResponseEntity.ok().build();
    }

    @Transactional
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long userId, JwtAuthenticationToken token) {

        User tokenUser = userRepository.findById(UUID.fromString(token.getName()));

        boolean isAdmin = tokenUser.getRoles()
                .stream().anyMatch(role -> role.getId().equals(Role.Values.ADMIN.getId()));

        if (isAdmin || tokenUser.getId().equals(userId)) {
            userRepository.deleteById(userId);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok().build();
    }

}
