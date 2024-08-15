package com.example.javaBackend.controller;

import com.example.javaBackend.controller.dto.UserWithPersonDto;
import com.example.javaBackend.entities.Person;
import com.example.javaBackend.entities.Role;
import com.example.javaBackend.entities.User;
import com.example.javaBackend.entities.jsonview.View;
import com.example.javaBackend.repository.RoleRepository;
import com.example.javaBackend.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

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
    @PostMapping()
    public ResponseEntity<?> createUser(@RequestBody UserWithPersonDto createUserDto) {

        Role basicRole = roleRepository.findByName(Role.Values.BASIC.name());
        Optional<User> userDb = userRepository.findUserByName(createUserDto.name());
        if (userDb.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        User newUser = new User();
        newUser.setName(createUserDto.name());
        newUser.setEmail(createUserDto.email());
        newUser.setPassword(passwordEncoder.encode(createUserDto.password()));
        newUser.setRoles(Set.of(basicRole));

        Person newPerson = new Person();
        newPerson.setName(createUserDto.person().name());
        newPerson.setCpfNumber(createUserDto.person().cpf());
        newPerson.setPhoneNumber(createUserDto.person().phone());
        newPerson.setUser(newUser);
        newUser.setPerson(newPerson);

        userRepository.save(newUser);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/hello")
    public ResponseEntity<String[]> firstPage() {
        return ResponseEntity.ok(new String[]{"HELLO"});
    }

    @GetMapping("/")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @JsonView(View.Admin.class)
    public ResponseEntity<List<User>> listAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    @JsonView(View.Basic.class)
    public ResponseEntity<List<User>> searchUserByUserNameAndEmail(
            @RequestParam(defaultValue = "") String name, @RequestParam(defaultValue = "") String email) {
        boolean isNameBlank = name.isBlank();
        boolean isEmailBlank = email.isBlank();
        List<User> users = new ArrayList<>();

        if (isNameBlank && isEmailBlank) {
            users = userRepository.findAll();
            return ResponseEntity.ok(users);
        }

        if (!isNameBlank) {
            if (!isEmailBlank) {
                users = userRepository.findByNameAndEmail(name, email);
                return ResponseEntity.ok(users);
            } else {
                users = userRepository.findByName(name);
                return ResponseEntity.ok(users);
            }
        }

        users = userRepository.findByEmail(email);
        return ResponseEntity.ok(users);

    }

    @Transactional
    @PutMapping()
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    public ResponseEntity<User> updateUser(@RequestBody UserWithPersonDto userUpdateDto, JwtAuthenticationToken token) {

        Optional<User> tokenUser = userRepository.findById(UUID.fromString(token.getName()));
        if (tokenUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Optional<User> userDb = userRepository.findUserByName(userUpdateDto.name());
        if (userDb.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (tokenUser.get().getId().equals(userDb.get().getId())) {
            userDb.get().setName(userUpdateDto.name());
            userDb.get().setEmail(userUpdateDto.email());
            userDb.get().setPassword(passwordEncoder.encode(userUpdateDto.password()));
            userRepository.save(userDb.get());
            return ResponseEntity.ok().build();
        }

        boolean isAdmin = tokenUser.get().getRoles()
                .stream().anyMatch(role -> role.getId().equals(Role.Values.ADMIN.getId()));
        if (isAdmin) {
            userDb.get().setName(userUpdateDto.name());
            userDb.get().setEmail(userUpdateDto.email());
            userDb.get().setPassword(passwordEncoder.encode(userUpdateDto.password()));
            userRepository.save(userDb.get());
            return ResponseEntity.ok().build();
        } else throw new ResponseStatusException(HttpStatus.NOT_FOUND);

    }

    @Transactional
    @DeleteMapping()
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @RequestParam String name
            , JwtAuthenticationToken token) {

        Optional<User> tokenUser = userRepository.findById(UUID.fromString(token.getName()));

        if (tokenUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        boolean isAdmin = tokenUser.get().getRoles()
                .stream().anyMatch(role -> role.getId().equals(Role.Values.ADMIN.getId()));
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<User> userDelete = userRepository.findUserByName(name);
        if(userDelete.isPresent()){
            userRepository.deleteById(userDelete.get().getId());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
