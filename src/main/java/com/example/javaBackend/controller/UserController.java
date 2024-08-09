package com.example.javaBackend.controller;

import com.example.javaBackend.controller.dto.CreateUserDto;
import com.example.javaBackend.controller.dto.UserDto;
import com.example.javaBackend.entities.Person;
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
        newUser.setEmail(createUserDto.email());
        newUser.setPassword(passwordEncoder.encode(createUserDto.password()));
        newUser.setRoles(Set.of(basicRole));

        Person newPerson = new Person();
        newPerson.setName(createUserDto.person().name());
        newPerson.setCpfNumber(createUserDto.person().cpf());
        newPerson.setPhoneNumber(createUserDto.person().phone());
        newUser.setPerson(newPerson);

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
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    public ResponseEntity<User> updateUser(@RequestBody CreateUserDto userUpdateDto, JwtAuthenticationToken token){

        Optional<User> tokenUser = userRepository.findById(UUID.fromString(token.getName()));
        if(tokenUser.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Optional<User> userDb = userRepository.findByName(userUpdateDto.name());
        if(userDb.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if(tokenUser.get().getId().equals(userDb.get().getId())){
            userDb.get().setName(userUpdateDto.name());
            userDb.get().setEmail(userUpdateDto.email());
            userDb.get().setPassword(passwordEncoder.encode(userUpdateDto.password()));
            userRepository.save(userDb.get());
            return ResponseEntity.ok().build();
        }

        boolean isAdmin = tokenUser.get().getRoles()
                .stream().anyMatch(role -> role.getId().equals(Role.Values.ADMIN.getId()));
        if (isAdmin){
            userDb.get().setName(userUpdateDto.name());
            userDb.get().setEmail(userUpdateDto.email());
            userDb.get().setPassword(passwordEncoder.encode(userUpdateDto.password()));
            userRepository.save(userDb.get());
            return ResponseEntity.ok().build();
        }
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND);

    }

    @Transactional
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") UUID id, JwtAuthenticationToken token) {

        Optional<User> tokenUser = userRepository.findById(UUID.fromString(token.getName()));

        if(tokenUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        boolean isAdmin = tokenUser.get().getRoles()
                .stream().anyMatch(role -> role.getId().equals(Role.Values.ADMIN.getId()));

        if (isAdmin || tokenUser.get().getId().equals(id)) {
            userRepository.deleteById(id);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok().build();
    }

    private void checkIfAllowed(UserDto userDto, User tokenUser) {
        boolean isAdmin = tokenUser.getRoles()
                .stream().anyMatch(role -> role.getId().equals(Role.Values.ADMIN.getId()));
        if(isAdmin || tokenUser.getId().equals(userDto.id())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

}
